package org.xidea.jsi.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIDependence;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.ScriptLoader;

public class DefaultPackage implements JSIPackage {
	private static final Log log = LogFactory.getLog(DefaultPackage.class);
	private JSIRoot root;
	private String name;
	private String implementation;
	private Map<String, List<String>> scriptObjectMap = new HashMap<String, List<String>>();
	private Map<String, String> objectScriptMap = new HashMap<String, String>();
	private Map<String, ScriptLoader> loaderMap = new HashMap<String, ScriptLoader>();
	private List<List<Object>> unparsedDependenceList = new ArrayList<List<Object>>();
	private Map<String, List<JSIDependence>> dependenceMap = new HashMap<String, List<JSIDependence>>();

	public DefaultPackage(JSIRoot root, String name) {
		this.root = root;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIPackage2#initialize()
	 */
	public void initialize() {
		if (unparsedDependenceList != null) {
			// parse unparsedDependenceList
			parseDependence();
			unparsedDependenceList = null;
			this.scriptObjectMap = Collections
					.unmodifiableMap(this.scriptObjectMap);
			this.objectScriptMap = Collections
					.unmodifiableMap(this.objectScriptMap);
			this.loaderMap = Collections.unmodifiableMap(this.loaderMap);
			this.dependenceMap = Collections
					.unmodifiableMap(this.dependenceMap);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIPackage2#addScript(java.lang.String,
	 *      java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public void addScript(String scriptName, Object objectNames,
			Object beforeLoadDependences, Object afterLoadDependences) {
		try {
			checkState();
			loaderMap
					.put(scriptName, new DefaultScriptLoader(this, scriptName));
			List<String> objects = new ArrayList<String>();
			if (objectNames instanceof String) {
				objects.add((String) objectNames);
			} else if (objectNames instanceof Collection) {
				for (Iterator<Object> it2 = ((Collection<Object>) objectNames)
						.iterator(); it2.hasNext();) {
					objects.add((String) it2.next());
				}
			}
			List<String> savedObjects = scriptObjectMap.get(scriptName);
			if (savedObjects == null) {
				savedObjects = new ArrayList<String>();
				scriptObjectMap.put(scriptName, savedObjects);
			}
			for (Iterator<String> it2 = objects.iterator(); it2.hasNext();) {
				String item = it2.next();
				if (objectScriptMap.containsKey(item)) {
					log.error("重复的脚本元素定义:" + item);
				}
				objectScriptMap.put(item, scriptName);
				item = item.replace("\\..*$", "");
				if (!savedObjects.contains(item)) {
					savedObjects.add(item);
				}
			}
			if (beforeLoadDependences != null) {
				this.addDependence(scriptName, beforeLoadDependences, false);
			}
			if (afterLoadDependences != null) {
				this.addDependence(scriptName, afterLoadDependences, true);
			}
		} catch (RuntimeException e) {
			log.error("添加脚本异常:" + this.name + "#addScript(" + scriptName + ","
					+ objectNames + "," + beforeLoadDependences + ","
					+ afterLoadDependences + ")", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIPackage2#addDependence(java.lang.String,
	 *      java.lang.Object, boolean)
	 */
	@SuppressWarnings("unchecked")
	public void addDependence(String thisPath, Object targetPath,
			boolean afterLoad) {
		checkState();
		if (!afterLoad) {
			String file = this.objectScriptMap.get(thisPath);
			if (file != null) {
				thisPath = file;
			}
		}
		if (targetPath instanceof Collection) {
			for (Iterator<Object> it = ((Collection<Object>) targetPath)
					.iterator(); it.hasNext();) {
				// JSI js 做了展开优化
				this.addDependence(thisPath, it.next(), afterLoad);
			}
		} else {
			List<Object> args = new ArrayList<Object>();
			args.add(thisPath);
			args.add(targetPath);
			args.add(afterLoad);
			unparsedDependenceList.add(args);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIPackage2#setImplementation(java.lang.String)
	 */
	public void setImplementation(String implementation) {
		checkState();
		if (implementation.startsWith("..")) {
			implementation = this.name + implementation;
			do {
				implementation = implementation.replace("(:?\\w+\\.\\.\\/?)*",
						"");
			} while (implementation.indexOf("..") > 0);
		} else if (implementation.startsWith(".")) {
			implementation = this.name + implementation;
		}
		this.implementation = implementation;
	}

	/**
	 * 检查状态,确保尚未初始化，依然可以修改
	 */
	private void checkState() {
		if (unparsedDependenceList == null) {
			throw new IllegalStateException("已初始化的包，不能再次修改");
		}
	}

	private void parseDependence() {
		if (unparsedDependenceList != null) {
			for (Iterator<List<Object>> iterator = unparsedDependenceList
					.iterator(); iterator.hasNext();) {
				List<Object> args = iterator.next();
				final String thisPath = (String) args.get(0);
				
				String targetPath = (String) args.get(1);
				JSIPackage targetPackage = this;
				String thisObjectName = null;
				String targetObjectName = null;
				
				final boolean afterLoad = (Boolean) args.get(2);
				final boolean allSource = "*".equals(thisPath);
				final boolean allTarget = targetPath.endsWith("*");
				try {
					if (allSource || allTarget) {
						Collection<String> thisFileMap;
						Collection<String> targetFileMap;

						if (allSource) {
							thisFileMap = this.scriptObjectMap.keySet();
						} else {
							String file = this.objectScriptMap.get(thisPath);// file
							// ->
							// thisFile
							if (file != null) {
								thisObjectName = thisPath;
							} else {
								file = thisPath;
							}
							thisFileMap = Arrays.asList(new String[] { file });
						}
						if (allTarget) {
							if (!targetPath.equals("*")) {// *x*
								targetPackage = this.root
										.findPackageByPath(targetPath);
								targetPackage = root.requirePackage(
										targetPackage.getName(), true);
							}
							targetFileMap = targetPackage.getScriptObjectMap()
									.keySet();
						} else {
							String file = this.objectScriptMap.get(targetPath);// file
							// targetFile
							if (file != null) {
								targetObjectName = targetPath;
							} else if (this.scriptObjectMap
									.containsKey(targetPath)) {
								file = targetPath;
							} else {
								targetPackage = this.root
										.findPackageByPath(targetPath);
								targetPath = targetPath.substring(targetPackage
										.getName().length() + 1);
								targetPackage = root.requirePackage(
										targetPackage.getName(), true);
								file = targetPackage.getObjectScriptMap().get(
										targetPath);
								if (file != null) {
									targetObjectName = targetPath;
								} else {
									file = targetPath;
								}
							}
							targetFileMap = Arrays
									.asList(new String[] { file });
						}
						final boolean samePackage = targetPackage.equals(this);
						for (String targetFile : targetFileMap) {
							DefaultDependence dep = new DefaultDependence(
									targetPackage, targetFile,
									targetObjectName, afterLoad);
							for (String thisFile : thisFileMap) {
								if (!(samePackage && thisFile
										.equals(targetFile))) {
									saveDependence(dep, thisFile,
											thisObjectName);
								}
							}
						}
					} else {// 单挑
						String thisFile = this.objectScriptMap.get(thisPath);
						if (thisFile != null) {
							thisObjectName = thisPath;
						} else {
							thisFile = thisPath;
						}

						String file = this.objectScriptMap.get(targetPath);
						if (file != null) {
							targetObjectName = targetPath;
						} else if (this.scriptObjectMap.containsKey(targetPath)) {
							file = targetPath;
						} else {
							targetPackage = this.root
									.findPackageByPath(targetPath);
							if (targetPackage == null) {
								log.error(targetPackage);
								throw new RuntimeException(
										"can not find Package for:"
												+ targetPath);
							}
							targetPath = targetPath.substring(targetPackage
									.getName().length() + 1);
							targetPackage = root.requirePackage(targetPackage
									.getName(), true);
							file = targetPackage.getObjectScriptMap().get(
									targetPath);
							if (file != null) {
								targetObjectName = targetPath;
							} else {
								file = targetPath;
							}
						}
						DefaultDependence dep = new DefaultDependence(
								targetPackage, file, targetObjectName,
								afterLoad);
						saveDependence(dep, thisFile, thisObjectName);
					}
				} catch (RuntimeException e) {
					log.error("包解析异常:" + this.name + "#addDependence(" + args
							+ ")", e);
				}
			}
		}
	}

	private void saveDependence(DefaultDependence dep, String thisFile,
			String thisObject) {
		List<JSIDependence> depList = this.dependenceMap.get(thisFile);
		if (depList == null) {
			dependenceMap.put(thisFile,
					depList = new ArrayList<JSIDependence>());
		}
		depList.add(dep.instanceFor(thisObject));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIPackage2#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIPackage2#getImplementation()
	 */
	public String getImplementation() {
		return implementation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIPackage2#getScriptObjectMap()
	 */
	public Map<String, List<String>> getScriptObjectMap() {
		return this.scriptObjectMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIPackage2#getObjectScriptMap()
	 */
	public Map<String, String> getObjectScriptMap() {
		return this.objectScriptMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIPackage2#getLoaderMap()
	 */
	public Map<String, ScriptLoader> getLoaderMap() {
		return loaderMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIPackage2#getDependenceMap()
	 */
	public Map<String, List<JSIDependence>> getDependenceMap() {
		return dependenceMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIPackage2#loadText(java.lang.String)
	 */
	public String loadText(String scriptName) {
		return this.root.loadText(this.name, scriptName);
	}
}
