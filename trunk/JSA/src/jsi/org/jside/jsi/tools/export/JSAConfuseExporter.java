package org.jside.jsi.tools.export;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jside.jsi.tools.JavaScriptAnalysisResult;
import org.jside.jsi.tools.JavaScriptCompressionAdvisor;
import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.jside.jsi.tools.util.JavaScriptConstants;
import org.xidea.jsi.JSIDependence;
import org.xidea.jsi.ScriptLoader;

public class JSAConfuseExporter extends JSAAbstractExporter {
	private static final Log log = LogFactory.getLog(JSAConfuseExporter.class);

	public JSAConfuseExporter(JavaScriptCompressorConfig compressorConfig,
			String internalPrefix, int begin) {
		super(compressorConfig, internalPrefix, begin);
	}

	private String requireId(String pkg, String var) {
		String id = pkg + ":" + var;

		var = varMap.get(id);
		if (var == null) {
			varMap.put(id, var = newId());
		}
		return var;
	}

	/**
	 * 保留全部外部依赖变量(不在当前导出集合)
	 * 
	 * @param list
	 * @param exportMap
	 */
	private void prepareUnExported(List<ScriptLoader> list,
			Map<String, String> exportMap) {
		HashSet<String> loaders = new HashSet<String>();

		for (ScriptLoader sl : list) {
			loaders.add(sl.getPath());
		}
		for (ScriptLoader sl : list) {
			List<JSIDependence> deps = sl.getPackage().getDependenceMap().get(
					sl.getName());
			if (deps != null) {
				for (JSIDependence dep : deps) {
					String deppkg = dep.getTargetPackage().getName();
					String depath = deppkg.replace('.', '/') + '/'
							+ dep.getTargetFileName();
					if (!loaders.contains(depath)) {// 不在当前导出集合
						String name = dep.getTargetObjectName();
						if (name == null) {
							List<String> names = dep.getTargetPackage()
									.getScriptObjectMap().get(
											dep.getTargetFileName());
							for (String name2 : names) {
								this.varMap.put(deppkg + ":" + name2, name2);
							}
						} else {
							this.varMap.put(deppkg + ":" + name, name);
						}
					}
				}
			}
		}
	}

	public void prepare(List<ScriptLoader> list, Map<String, String> exportMap) {
		this.entryList = list;
		this.exportMap = exportMap;
		this.varMap = new HashMap<String, String>();
		this.entryInfoMap = new HashMap<String, JavaScriptAnalysisResult>();
		prepareUnExported(list, exportMap);
		for (ScriptLoader scriptLoader : list) {
			try {
				JavaScriptAnalysisResult info = compressor.analyse(scriptLoader
						.getSource());

				Collection<String> publicVars = scriptLoader.getLocalVars();
				Map<String, String> dependenceVarMap = scriptLoader
						.getDependenceVarMap();
				if (info.isUnsafe()) {
					// 这里是不能重复的
					for (String var : scriptLoader.getLocalVars()) {// 保留全部内部变量
						if (!publicVars.contains(var)) {
							String id = scriptLoader.getPath() + ":" + var;
							varMap.put(id, var);
						}
					}
					for (String var : info.getExternalVars()) {// 保留全部依赖变量
						String pkgName = dependenceVarMap.get(var);
						if (pkgName != null) {
							String id = pkgName + ":" + var;
							varMap.put(id, var);
						}
					}
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
			} catch (RuntimeException e) {
				log.error("JSI混淆预处理异常："+scriptLoader.getPath()+"/"+scriptLoader.getSource());
				throw e;

			}
		}
		for (String var : exportMap.keySet()) {// 保留全部导出变量
			String id = exportMap.get(var) + ":" + var;
			varMap.put(id, var);
		}
		prepareRelatived();
	}

	/**
	 * 吧关联依赖联系起来，只要一个被固化，所有关联都被固化，发现冲突，报告冲突信息
	 */
	private void prepareRelatived() {
		HashMap<String, String> refMap = new HashMap<String, String>();
		for (ScriptLoader loader : entryList) {
			HashSet<String> refs = new HashSet<String>(loader.getLocalVars());
			refs.retainAll(loader.getDependenceVars());
			if (refs.size() > 0) {
				for (String var : refs) {
					String id = loader.getPackage().getName() + ":" + var;
					String did = loader.getDependenceVarMap().get(var) + ":"
							+ var;
					if (id.equals(did)) {
						log.warn("检查包文件去：" + id);
					} else {
						refMap.put(id, did);
					}
				}
			}
		}
		boolean notEnd = true;
		int i = 100;
		if (!refMap.isEmpty()) {
			log.info("关联依赖表：" + refMap);
		}
		while (notEnd && i-- > 0) {
			notEnd = false;
			for (String id : refMap.keySet()) {
				String replaced = varMap.get(id);
				String did = refMap.get(id);
				String dreplaced = varMap.get(did);
				if (replaced != null) {// setParent
					if (dreplaced == null) {
						varMap.put(did, replaced);
					} else if (!replaced.equals(dreplaced)) {
						log.error("导出冲突" + id + ":" + replaced + "<>" + did
								+ ":" + dreplaced);
					}
				} else if (dreplaced != null) {
					varMap.put(id, dreplaced);
				} else if (!refMap.containsKey(did)) {
					varMap.put(did, key2var(did));
					varMap.put(id, key2var(did));
				} else {
					notEnd = true;
				}
			}
		}
	}

	private String key2var(String id) {
		return id.substring(id.lastIndexOf(':') + 1);
	}

	/**
	 * 改变了 varMap 对象啊
	 */
	@Override
	protected String compress(final ScriptLoader scriptLoader) {
		final int logStatus = getLogStatus(scriptLoader);
		final String rawReplaced = logStatus == 0 ? null : requireId(
				"org.xidea.jsi", "$log");
		final String[] logHit = new String[1];// oldlogid,newlogid,,loghit
		JavaScriptCompressionAdvisor compressionAdvisor = new JavaScriptCompressionAdvisor() {

			final Collection<String> localVars = scriptLoader.getLocalVars();
			final Map<String, String> dependenceMap = scriptLoader
					.getDependenceVarMap();

			public String getReplacedName(String oldValue, boolean external) {
				if (logStatus != 0 && oldValue.equals(rawReplaced)) {
					return logHit[0] = newId();
				}
				if (localVars.contains(oldValue)) {// 公开变量
					return requireId(scriptLoader.getPackage().getName(),
							oldValue);
				} else if (dependenceMap.containsKey(oldValue)) {// 依赖变量
					String packageName = dependenceMap.get(oldValue);

					String replaced = requireId(packageName, oldValue);
					// System.out.println(replaced + packageName
					// + oldValue);
					return replaced;
				} else {
					JavaScriptAnalysisResult info = entryInfoMap
							.get(scriptLoader.getPath());
					if (info.getLocalVars().contains(oldValue)) {// 本地内部变量
						return requireId(scriptLoader.getPath(), oldValue);
					} else {// 外部变量？？？？？
						return oldValue;
					}
				}
			}

			public String newVaribaleName() {
				throw new UnsupportedOperationException();
			}

		};

		String compressed = compressor.compress(scriptLoader.getSource(),
				compressionAdvisor);

		if (logHit[0] != null) {
			// var nid = $log.clone("xxx");
			compressed = cloneLog(scriptLoader.getPath(), compressed,
					rawReplaced, logHit[0], logStatus > 0);
		}
		return compressed;
	}
}
