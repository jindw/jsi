package org.jside.jsi.tools.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jside.jsi.tools.JSAToolkit;
import org.jside.jsi.tools.JavaScriptAnalysisResult;
import org.jside.jsi.tools.JavaScriptCompressor;
import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.jside.jsi.tools.util.IDGenerator;
import org.xidea.el.json.JSONEncoder;
import org.xidea.jsi.JSIDependence;
import org.xidea.jsi.ScriptLoader;

abstract class JSAAbstractExporter {
	protected JavaScriptCompressor compressor = JSAToolkit.getInstance()
			.createJavaScriptCompressor();
	JavaScriptCompressorConfig compressorConfig;
	protected List<ScriptLoader> entryList;
	protected Map<String, String> exportMap;
	protected Map<String, String> varMap;
	protected Map<String, JavaScriptAnalysisResult> entryInfoMap;
	protected final IDGenerator idGenerator;

	private LinkedHashMap<String, String> resultMap = new LinkedHashMap<String, String>();

	public JavaScriptCompressor getJavaScriptCompressor() {
		return compressor;
	}

	public JSAAbstractExporter(JavaScriptCompressorConfig compressorConfig,
			String internalPrefix, int begin) {
		this.compressorConfig = compressorConfig;
		compressor.setCompressorConfig(compressorConfig);
		idGenerator = new IDGenerator(internalPrefix, begin, null);
	}

	/**
	 * @see JSAPreserveExporter#export(List, Map)
	 * @param list
	 * @param exportMap
	 * @return
	 */
	public LinkedHashMap<String, String> export(List<ScriptLoader> list,
			Map<String, String> exportMap) {
		if (resultMap.isEmpty()) {
			this.prepare(list, exportMap);
			for (final ScriptLoader entry : entryList) {
				String compressed = compress(entry);
				this.resultMap.put(entry.getPath(), compressed);
			}
		} else {
			throw new IllegalStateException("一个对象只能导出一次");
		}
		return resultMap;
	}

	protected abstract void prepare(List<ScriptLoader> list,
			Map<String, String> exportMap);

	protected abstract String compress(final ScriptLoader entry);

	public String getWarn() {
		if (new HashSet<String>(this.varMap.values()).size() < this.varMap
				.size()) {
			HashMap<String, String> tempMap = new HashMap<String, String>();
			HashMap<String, List<String>> conflictMap = new HashMap<String, List<String>>();
			for (String id : varMap.keySet()) {
				String var = varMap.get(id);
				if (tempMap.containsKey(var)) {// 冲突
					if (!conflictMap.containsKey(var)) {
						conflictMap.put(var, new ArrayList<String>());
					}
					conflictMap.get(var).add(id);
				} else {
					tempMap.put(var, id);
				}
			}
			for (String var : conflictMap.keySet()) {
				List<String> values = conflictMap.get(var);
				values.add(tempMap.get(var));
			}

			return "/** ===== 冲突问题  ===== **/\r\n/*" + conflictMap + "*/";

		} else {
			return null;
		}
	}

	protected String cloneLog(final String path,final String compressed,
			String oldLog, String newLog,boolean afterLoad) {
		String start = null;
		if(compressed.indexOf(newLog+".")>0){
			String prefix = newLog.replaceAll("[\\$]", "\\\\\\$");
			
			if(!prefix.startsWith("\\$")){
				prefix = "\\b"+prefix;
			}
			Pattern pat = Pattern.compile(prefix+"\\.(?:trace|debug|info|warn|error|fatal)\\(");
			if(pat.matcher(compressed).find()){

				String logReplace = "var " + newLog + "=" + oldLog + ".clone("
						+ JSONEncoder.encode(path) + ")";
				if(afterLoad){
					return compressed + "\n"+logReplace;
				}else{
					if (compressed.startsWith("/*")) {
						int p = compressed.indexOf("*/");
						if (p > 0) {
							start = compressed.substring(0, p + 2);
							return start + logReplace + compressed.substring(p + 2);
						}else{
							return logReplace + compressed;
						}
					}else{
						return logReplace + compressed;
					}
				}
			}
		}
		return compressed;
	}
	/**
	 * -1 beforeload
	 * -0 no
	 * 1 afterload
	 * @param entry
	 * @return
	 */
	protected int getLogStatus(final ScriptLoader entry) {
		List<JSIDependence> list = entry.getPackage().getDependenceMap().get(entry.getName());
		int rtv = 0;//no hit
		if (list != null) {
			for (JSIDependence dependence : list) {
				String pkg = dependence.getTargetPackage().getName();
				if("org.xidea.jsi".equals(pkg)){
					String name = dependence.getTargetObjectName();
					if(name == null || "$log".equals(name)){
						if(dependence.isAfterLoad()){
							rtv = 1;
						}else{
							return -1;//before load
						}
					}
				}
			}
		}
		return rtv;
	}

	protected String newId() {
		String id = idGenerator.newId();
		while (varMap.containsValue(id)) {// var 就够了，新产生的id自己不会冲突
			id = idGenerator.newId();
		}
		return id;
	}

}