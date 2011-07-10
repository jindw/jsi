package org.jside.jsi.tools.export;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jside.jsi.tools.JavaScriptAnalysisResult;
import org.jside.jsi.tools.JavaScriptCompressionAdvisor;
import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.jside.jsi.tools.util.JavaScriptConstants;
import org.xidea.jsi.ScriptLoader;

class JSAPreserveExporter extends JSAAbstractExporter {
	private static final Log log = LogFactory.getLog(JSAPreserveExporter.class);


	public JSAPreserveExporter(JavaScriptCompressorConfig compressorConfig,
			String internalPrefix, int begin) {
		super(compressorConfig, internalPrefix, begin);
	}

	protected void prepare(List<ScriptLoader> list,
			Map<String, String> exportMap) {
		this.entryList = list;
		this.exportMap = exportMap;
		this.varMap = new HashMap<String, String>();
		this.entryInfoMap = new HashMap<String, JavaScriptAnalysisResult>();
		// 先明确必须保留的变量，避免发生先斩后奏的惨剧
		for (ScriptLoader scriptLoader : list) {
			String packageName = scriptLoader.getPackage().getName();
			JavaScriptAnalysisResult info ;
			try {
				info = compressor.analyse(scriptLoader
						.getSource());
			} catch (RuntimeException e) {
				log.warn(scriptLoader.getPath());
				log.warn(scriptLoader.getSource());
				e.printStackTrace();
				throw e;
			}
			Collection<String> exportVars = scriptLoader.getLocalVars();
			Map<String, String> dependenceVarMap = scriptLoader
					.getDependenceVarMap();
			if (info.isUnsafe()) {// 保留全部本地变量
				Collection<String> localVars = info.getLocalVars();
				for (String var : localVars) {
					if (!exportVars.contains(var)) {
						String id = scriptLoader.getPath() + ":" + var;
						varMap.put(id, var);
					}
				}
			}
			for (String var : exportVars) {// 保留全部公开变量
				String id = packageName + ":" + var;
				varMap.put(id, var);
			}
			for (String var : info.getExternalVars()) {// 记录全部外部变量
				String pkgName = dependenceVarMap.get(var);
				if (pkgName == null) {
					varMap.put(":" + var, var);
					if (!JavaScriptConstants.ALL_VARIBALES.contains(var)) {
						log.warn("未知变量:" + var + "@"
								+ scriptLoader.getPath());
					}
				}
			}
			entryInfoMap.put(scriptLoader.getPath(), info);
		}
	}

	/**
	 * 對外應該不允許任何寫操作
	 * @see org.jside.jsi.tools.export.JSAAbstractExporter#compress(org.xidea.jsi.ScriptLoader)
	 */
	protected String compress(final ScriptLoader entry) {
		final int logStatus = getLogStatus(entry);
		final String[] logHit = new String[1];//oldlogid,newlogid,,loghit
		JavaScriptCompressionAdvisor compressionAdvisor = new JavaScriptCompressionAdvisor() {
			final Collection<String> localVars = entry.getLocalVars();
			final Map<String, String> dependenceMap = entry.getDependenceVarMap();
			public String getReplacedName(String oldValue,
					boolean external) {
				if(logStatus != 0 && oldValue.equals("$log")){
					return logHit[0] = newId();
				}
				if (localVars.contains(oldValue)
						|| dependenceMap.containsKey(oldValue)) {
					return oldValue;
				} else {
					JavaScriptAnalysisResult info = entryInfoMap.get(entry
							.getPath());
					if (info.getLocalVars().contains(oldValue)) {
						return newId();
					} else {
						return oldValue;
					}
				}
			}
			public String newVaribaleName() {
				throw new UnsupportedOperationException();
			}
		};
		String compressed = compressor.compress(entry.getSource(),
				compressionAdvisor);
		if(logHit[0]!=null){
			//var nid = $log.clone("xxx");
			compressed = cloneLog(entry.getPath(), compressed, "$log", logHit[0],logStatus>0);
		}
		return compressed;
	}



}

