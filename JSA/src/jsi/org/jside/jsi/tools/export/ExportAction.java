package org.jside.jsi.tools.export;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jside.jsi.tools.JSAToolkit;
import org.jside.jsi.tools.JavaScriptCompressor;
import org.xidea.el.impl.CommandParser;
import org.xidea.el.json.JSONEncoder;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIRuntime;
import org.xidea.jsi.ScriptLoader;
import org.xidea.jsi.impl.DefaultLoadContext;
import org.xidea.jsi.impl.RuntimeSupport;

public class ExportAction extends ExportBase {
	private static final Log log = LogFactory.getLog(ExportAction.class);
	private static final Pattern templateExp = Pattern
			.compile("\\bnew\\s+Template\\s*\\(");
	private JSILoadContext exportContext = new DefaultLoadContext();
	private JSILoadContext bootCachedContext = new DefaultLoadContext();
	private Map<String, String> scriptCacheMap = new HashMap<String, String>();
	private JavaScriptCompressor compressor = JSAToolkit.getInstance()
			.createJavaScriptCompressor();
	private boolean textCompression;
	/**
	 * 包括报名
	 */
	private List<String> allImported;
	private Map<String, String> exportData;
	private List<ScriptLoader> exportList;

	public ExportAction(String[] args) {
		CommandParser.setup(this, args);
		compressor.setCompressorConfig(config);
		this.reset();
		for (File f : sources) {
			this.addSource(f);
		}
		this.addSource(scriptBase);
		this.addLib(scriptBase);
		for (File f : libs) {
			this.addLib(f);
		}
		this.allImported = new ArrayList<String>();
		if (exports != null) {
			allImported.addAll(Arrays.asList(exports));
		}
		if (bootPackage != null) {
			for (String packageName : bootPackage) {
				allImported.add(packageName + ":");
			}
		}
		if (bootCached != null) {
			for (String path : bootCached) {
				allImported.add(path);
			}
		}
		this.textCompression = config.isTextCompression();
		config.setTextCompression(false);

		if (exports != null) {
			for (String path : exports) {
				$import(path, exportContext);
			}
		}
		if (bootCached != null) {
			for (String path : bootCached) {
				$import(path, bootCachedContext);
			}
		}

	}

	public void execute() throws Exception {
		compressRoot();
		if (outputExported != null) {
			ExportUtil.writeToExported(this);
		}

		if (outputBoot != null) {
			ExportUtil.writeToBoot(bootCachedContext,this);
		}
		if (outputPackage != null) {
			ExportUtil.wirteToPackage(exportContext,this);
		}
		if (outputJAR != null || outputPreload != null) {
			final LinkedHashMap<String, String> exportResultMap = requirePackageFile();
			compressText(exportResultMap);
			if (outputPreload != null) {
				ExportUtil.wirteToPreload(exportResultMap,this);
			}
			if (outputJAR != null) {
				ExportUtil.wirteToJARResult(exportResultMap,this);
			}

		}
	}

	void compressText(LinkedHashMap<String, String> exportResultMap) {
		for (Map.Entry<String, String> entry : exportResultMap.entrySet()) {
			entry.setValue(compressText(entry.getValue()));
		}
	}

	String compressText(String exportedText) {
		if (textCompression) {
			config.setTextCompression(true);
			exportedText = compressor.compress(exportedText, null);
			config.setTextCompression(false);
		}
		return exportedText;
	}
	//TODO:一旦template 实现被提前压缩，很容易导致模板不能被正确编译
	public String getCacheText(String pkgName, String scriptName) {
		return loadText(pkgName, scriptName);
	}
	public String loadText(String pkgName, String scriptName) {
		String path = toPath(pkgName, scriptName);
		String result = scriptCacheMap.get(path);
		if (result == null) {
			result = super.loadText(pkgName, scriptName);// /htmlUnitExporter.loadText(path);
			scriptCacheMap.put(path, result);
		}
		return result;
	}
	private String toPath(String pkgName, String scriptName) {
		String path = pkgName == null ? scriptName : pkgName.replace('.', '/')
				+ '/' + scriptName;
		return path;
	}

	static JSIRuntime  jsp = RuntimeSupport.create();
	protected void compressRoot() {
		for (ScriptLoader loader : exportContext.getScriptList()) {
			loader.getSource();
			getCacheText(loader.getPackage().getName(),loader.getName());
		}
		for (ScriptLoader loader : bootCachedContext.getScriptList()) {
			loader.getSource();
			getCacheText(loader.getPackage().getName(),loader.getName());
		}
		getCacheText(null, "boot.js");
		Set<String> paths = scriptCacheMap.keySet();
		Object exporter = null;
		for (String path : paths.toArray(new String[0])) {
			String content = scriptCacheMap.get(path);
			if (templateExp.matcher(content).find()) {
				if (exporter == null) {
					exporter = jsp.eval(this.getClass().getResource(
								"load-text.js"));
					jsp.invoke(exporter, "setup", this);
				}
				Object result = jsp.invoke(exporter, "loadText", path);
				//System.out.println(result instanceof String);
				if (result instanceof String) {
					content = (String) result;
					scriptCacheMap.put(path, content);
				}

			}
			content = compressor.compress(content, null);
			scriptCacheMap.put(path, content);
		}
	}





	private LinkedHashMap<String, String> requirePackageFile() {
		final Map<String, String> exportResultMap = requiredExportData();
		HashSet<String> pkgFlagSet = new HashSet<String>();
		LinkedHashMap<String, String> exportResultMap2 = new LinkedHashMap<String, String>();
		for (String path : exportResultMap.keySet()) {
			String pkgPath = ExportText.toPackagePath(path);
			if (!pkgFlagSet.contains(pkgPath)) {
				pkgFlagSet.add(pkgPath);
				exportResultMap2.put(pkgPath, getCacheText(null, pkgPath));
			}
			exportResultMap2.put(path, exportResultMap.get(path));
		}
		return exportResultMap2;
	}

	/**
	 * 獲得刪掉bootCached之後的導出數據
	 * 
	 * @return
	 */
	protected Map<String, String> requiredExportData() {
		if (exportData == null) {
			List<ScriptLoader> list = getExportList();
			Map<String, String> exportMap = exportContext.getExportMap();
			Map<String, String> exportResultMap = new JSAExportorAdaptor(
					config, getInternalPrefix(), preserve).export(list, exportMap);

			this.exportData = exportResultMap;
		}
		return exportData;
	}

	private List<ScriptLoader> getExportList() {
		if (this.exportList == null) {
			ArrayList<ScriptLoader> list = new ArrayList<ScriptLoader>();
			HashSet<String> removeSet = new HashSet<String>();
			for (ScriptLoader sl : bootCachedContext.getScriptList()) {
				removeSet.add(sl.getPath());
			}
			for (ScriptLoader sl : exportContext.getScriptList()) {
				if (!removeSet.contains(sl.getPath())) {
					list.add(sl);
				}
			}
			this.exportList = list;
		}
		return exportList;
	}

	

	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0) {
			args = new String[] {
					"-scriptBase","d:/workspace/JSI2/web/scripts",
					"-sources","d:/workspace/Lite2/web/scripts",
					"-exports","org.xidea.jsidoc.templateMap","-outputExported","d:/workspace/JSA/build/dest/xplus-page-init.js"
					//"-scriptBase","D:\\workspace\\Lite2/web/scripts","-exports",
					//"org.xidea.lite.util:*","-outputPackage","D:\\workspace\\Lite2/build/dest/jslite/lite/util/__package__.js",
					//"-outputExported","D:\\workspace\\Lite2/build/dest/jslite/lite/util/impl.js"
							//"-scriptBase","D:\\workspace\\Lite2/web/scripts","-config.ascii","false","-config.features","org.xidea.lite:Compile","org.xidea.jsi.boot:$log","-exports","org.xidea.el:*","org.xidea.lite:*","org.xidea.lite.impl:*","org.xidea.lite.parse:*","-outputExported","D:\\workspace\\Lite2/build/dest/jslite/template.js"
			// "-scriptBase","D:\\workspace\\Lite/web/scripts","-config.ascii","false","-config.features","org.xidea.lite:Compile","-config.features",":Debug","-exports","org.xidea.lite:Template","org.xidea.lite:XMLParser","org.xidea.lite.demo:TestCase","-outputExported","D:\\workspace\\Lite/build/dest/jslite/demo.js"
			};
		}
		if (log.isDebugEnabled()) {
			log.debug("env:" + System.getenv());
		}
		if (log.isDebugEnabled()) {
			log.debug("prop:" + System.getProperties());
		}
		log.info("Args:" + JSONEncoder.encode(args));
		try {
			ExportAction action = new ExportAction(args);
			action.execute();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
	}

	public List<String> getAllImported() {
		return allImported;
	}
}
