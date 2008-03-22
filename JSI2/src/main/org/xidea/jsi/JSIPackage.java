package org.xidea.jsi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class JSIPackage {
	private JSIRoot root;
	private String name;
	private String implementation;
	private Map<String, List<String>> scriptObjectMap = new HashMap<String, List<String>>();
	private Map<String, String> objectScriptMap = new HashMap<String, String>();
	private Map<String, ScriptLoader> loaderMap = new HashMap<String, ScriptLoader>();
	private List<List<Object>> unparsedDependenceList = new ArrayList<List<Object>>();
	private Map<String, List<JSIDependence>> dependenceMap = new HashMap<String, List<JSIDependence>>();

	JSIPackage(JSIRoot root, String name) {
		this.root = root;
		this.name = name;
	}
	public void addScript(String scriptName, Object objectNames,
			Object beforeLoadDependences, Object afterLoadDependences) {
		loaderMap.put(scriptName, new ScriptLoader(this,scriptName));
		List<String> objects = new ArrayList<String>();
		if (objectNames instanceof String) {
			objects.add((String) objectNames);
		} else if (objectNames instanceof List) {
			for (Iterator<Object> it2 = ((List<Object>) objectNames).iterator(); it2
					.hasNext();) {
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
				root.debug("重复的脚本元素定义");
			}
			objectScriptMap.put(item, scriptName);
			item = item.replace("\\..*$", "");
			if (!savedObjects.contains(item)) {
				savedObjects.add(item);
			}
		}
		if (beforeLoadDependences != null) {
			this.addDependence(scriptName, beforeLoadDependences, true);
		}
		if (afterLoadDependences != null) {
			this.addDependence(scriptName, afterLoadDependences, false);
		}
	}

	public void addDependence(String thisPath, Object targetPath,
			boolean beforeLoad) {
		if (beforeLoad) {
			String file = this.objectScriptMap.get(thisPath);
			if (file != null) {
				thisPath = file;
			}
		}
		if (targetPath instanceof List) {
			for (Iterator<Object> it = ((List<Object>) targetPath).iterator(); it
					.hasNext();) {
				// JSI js 做了展开优化
				this.addDependence(thisPath, it.next(), beforeLoad);
			}
		} else {
			List<Object> args = new ArrayList<Object>();
			args.add(thisPath);
			args.add(targetPath);
			args.add(beforeLoad);
			unparsedDependenceList.add(args);
		}
	}

	public void initialize() {
		if (unparsedDependenceList != null) {
			// parse unparsedDependenceList
			parseDependence();
			unparsedDependenceList = null;
			this.scriptObjectMap = Collections.unmodifiableMap(this.scriptObjectMap);
			this.objectScriptMap = Collections.unmodifiableMap(this.objectScriptMap);
			this.loaderMap = Collections.unmodifiableMap(this.loaderMap);
			this.dependenceMap = Collections.unmodifiableMap(this.dependenceMap);
		}
	}

	private void parseDependence() {
		if (unparsedDependenceList != null) {
			for (Iterator<List<Object>> iterator = unparsedDependenceList
					.iterator(); iterator.hasNext();) {
				List<Object> args = iterator.next();
				String thisPath = (String) args.get(0);
				String targetPath = (String) args.get(1);
				JSIPackage targetPackage = this;
				String thisObjectName = null;
				String targetObjectName = null;
				boolean afterLoad = (Boolean) args.get(2);
				boolean samePackage = false;
				boolean allSource = "*".equals(thisPath);
				boolean allTarget = targetPath.endsWith("*");
				if (allSource || allTarget) {
					Collection<String> sourceFileMap;
					Collection<String> targetFileMap;

					if (allSource) {
						sourceFileMap = this.scriptObjectMap.keySet();
					} else {
						String file = this.objectScriptMap.get(thisPath);
						if (file != null) {
							thisObjectName = thisPath;
						} else {
							file = thisPath;
						}
						sourceFileMap = Arrays.asList(new String[] { file });
					}
					if (allTarget) {
						if (targetPath.equals("*")) {// *x*
							samePackage = true;
						} else {
							targetPackage = this.root
									.findPackageByPath(targetPath);
							targetPackage = root.requirePackage(
									targetPackage.getName(), true);
						}
						targetFileMap = targetPackage.scriptObjectMap.keySet();
					} else {
						String file = this.objectScriptMap.get(targetPath);
						if (file != null) {
							thisObjectName = targetPath;
						} else if (this.scriptObjectMap.containsKey(targetPath)) {
							file = targetPath;
						} else {
							targetPackage = this.root
									.findPackageByPath(targetPath);
							targetPath = targetPath.substring(targetPackage
									.getName().length() + 1);
							targetPackage = root.requirePackage(
									targetPackage.getName(), true);
							file = targetPackage.objectScriptMap
									.get(targetPath);
							if (file != null) {
								targetObjectName = targetPath;
							} else {
								file = targetPath;
							}
						}
						targetFileMap = Arrays.asList(new String[] { file });
					}
					for (String targetFile : targetFileMap) {
						JSIDependence dep = new JSIDependence(
								this.root, targetPackage, targetFile,
								targetObjectName, afterLoad);
						for (String sourceFile : sourceFileMap) {
							if (!(samePackage && sourceFile.equals(targetFile))) {
								saveDependence(dep, sourceFile, thisObjectName);
							}
						}
					}
				} else {
					String thisFile = this.objectScriptMap.get(thisPath);
					if (thisFile != null) {
						thisObjectName = thisPath;
					} else {
						thisFile = thisPath;
					}

					String file = this.objectScriptMap.get(targetPath);
					if (file != null) {
						thisObjectName = targetPath;
					} else if (this.scriptObjectMap.containsKey(targetPath)) {
						file = targetPath;
					} else {
						targetPackage = this.root
								.findPackageByPath(targetPath);
						targetPath = targetPath.substring(targetPackage
								.getName().length() + 1);
						targetPackage = root.requirePackage(targetPackage
								.getName(), true);
						file = targetPackage.objectScriptMap
								.get(targetPath);
						if (file != null) {
							targetObjectName = targetPath;
						} else {
							file = targetPath;
						}
					}
					JSIDependence dep = new JSIDependence(this.root,
							targetPackage, file, thisObjectName,
							afterLoad);
					saveDependence(dep, thisFile, targetObjectName);
				}

			}
		}
	}

	private void saveDependence(JSIDependence dep, String sourceFile,
			String object) {
		List<JSIDependence> depList = this.dependenceMap.get(sourceFile);
		if (depList == null) {
			dependenceMap.put(sourceFile,
					depList = new ArrayList<JSIDependence>());
		}
		depList.add(dep);
	}

	public void setImplementation(String implementation) {
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

	public String getName() {
		return name;
	}

	public String getImplementation() {
		return implementation;
	}

	public Map<String, List<String>> getScriptObjectMap() {
		return this.scriptObjectMap;
	}

	public Map<String, String> getObjectScriptMap() {
		return this.objectScriptMap;
	}

	/**
	 * 此处的loaderMap与JSI脚本中的loaderMap不同。loaderMap在包初始化时就一稳定
	 * @return
	 */
	public Map<String, ScriptLoader> getLoaderMap() {
		return loaderMap;
	}

	public Map<String, List<JSIDependence>> getDependenceMap() {
		return dependenceMap;
	}

	public String loadText(String scriptName) {
		return this.root.loadTextByPackageAndFile(this.name, scriptName);
	}
}
