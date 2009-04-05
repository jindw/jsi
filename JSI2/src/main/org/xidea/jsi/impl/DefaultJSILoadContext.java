package org.xidea.jsi.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xidea.jsi.JSIDependence;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.ScriptLoader;

/**
 * @scope Session
 * @author jindw
 */
public class DefaultJSILoadContext implements JSILoadContext {
	private Map<String, ScriptStatus> scriptStatusMap = new HashMap<String, ScriptStatus>();
	private List<String> loadList = new ArrayList<String>();
	private Map<String, ScriptLoader> loadMap = new HashMap<String, ScriptLoader>();
	private Map<String, String> exportMap = new HashMap<String, String>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.JSILoadContext#loadScript(org.xidea.jsi.JSIPackage,
	 *      java.lang.String, java.lang.String, boolean)
	 */
	public void loadScript(JSIPackage pkg, final String path,
			final String object, final boolean export) {
		String id = pkg.getName().replace('.', '/') + "/" + path;
		if (export) {
			if (object == null) {
				org.xidea.jsi.ScriptLoader loader = pkg.getLoaderMap()
						.get(path);
				for (String var : loader.getLocalVars()) {
					exportMap.put(var, pkg.getName());
				}
			} else {
				int pos = object.indexOf(".");
				// 命名空间也算
				exportMap.put(pos < 0 ? object : object.substring(0, pos), pkg
						.getName());
			}
		}

		loadMap.put(id, pkg.getLoaderMap().get(path));
		ScriptStatus status = scriptStatusMap.get(id);
		if (status == null) {
			scriptStatusMap.put(id, status = new ScriptStatus());
		}
		pkg.initialize();
		List<JSIDependence> list = ((DefaultJSIPackage) pkg).getDependenceMap()
				.get(path);
		if (list == null) {
			if (!loadList.contains(id)) {
				loadList.add(id);
			}
		} else {
			for (JSIDependence dependence : list) {
				String dependenceThisObjectName = dependence
						.getThisObjectName();
				if (!dependence.isAfterLoad()
						&& (dependenceThisObjectName == null || object == null || object
								.equals(dependenceThisObjectName))) {
					((DefaultJSIDependence) dependence).load(this);
					if (status.isLoaded(object)) {
						return;
					}
				}
			}
			if (status.load(object)) {
				return;
			}
			if (!loadList.contains(id)) {
				loadList.add(id);
			}
			for (JSIDependence dependence : list) {
				String dependenceThisObjectName = dependence
						.getThisObjectName();
				if (dependence.isAfterLoad()
						&& (dependenceThisObjectName == null || object == null || object
								.equals(dependenceThisObjectName))) {
					((DefaultJSIDependence) dependence).load(this);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.JSILoadContext#getExportMap()
	 */
	public Map<String, String> getExportMap() {
		return exportMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.JSILoadContext#getScriptEntryList()
	 */
	public List<ScriptLoader> getScriptList() {
		ArrayList<ScriptLoader> result = new ArrayList<ScriptLoader>();
		for (String file : loadList) {
			org.xidea.jsi.ScriptLoader entry = loadMap.get(file);
			result.add(entry);
		}
		return result;
	}

}
