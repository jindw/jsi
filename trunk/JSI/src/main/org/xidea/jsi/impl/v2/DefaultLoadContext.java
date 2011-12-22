package org.xidea.jsi.impl.v2;

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
public class DefaultLoadContext implements JSILoadContext {
	protected Map<String, ScriptStatus> scriptStatusMap = new HashMap<String, ScriptStatus>();
	protected List<String> loadList = new ArrayList<String>();
	protected Map<String, ScriptLoader> loadMap = new HashMap<String, ScriptLoader>();
	protected Map<String, String> exportMap = new HashMap<String, String>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.JSILoadContext#loadScript(org.xidea.jsi.JSIPackage,
	 *      java.lang.String, java.lang.String, boolean)
	 */
	public void loadScript(JSIPackage pkg, final String path,
			final String objectName, final boolean export) {
		final String id = pkg.getName().replace('.', '/') + "/" + path;
		if (export) {
			if (objectName == null) {
				org.xidea.jsi.ScriptLoader loader = pkg.getLoaderMap()
						.get(path);
				for (String var : loader.getLocalVars()) {
					exportMap.put(var, pkg.getName());
				}
			} else {
				int pos = objectName.indexOf(".");
				// 命名空间也算
				exportMap.put(pos < 0 ? objectName : objectName.substring(0, pos), pkg
						.getName());
			}
		}

		loadMap.put(id, pkg.getLoaderMap().get(path));
		ScriptStatus status = scriptStatusMap.get(id);
		if (status == null) {
			scriptStatusMap.put(id, status = new ScriptStatus());
		}
		pkg.initialize();
		List<JSIDependence> list = ((DefaultPackage) pkg).getDependenceMap()
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
						&& (dependenceThisObjectName == null || objectName == null || objectName
								.equals(dependenceThisObjectName))) {
					((DefaultDependence) dependence).load(this);
					if (status.isLoaded(objectName)) {
						return;
					}
				}
			}
			if (status.load(objectName)) {
				return;
			}
			if (!loadList.contains(id)) {
				loadList.add(id);
			}
			loadAfter(objectName, list);
		}
	}

	protected void loadAfter(final String objectName, List<JSIDependence> list) {
		for (JSIDependence dependence : list) {
			String dependenceThisObjectName = dependence
					.getThisObjectName();
			if (dependence.isAfterLoad()
					&& (dependenceThisObjectName == null || objectName == null || objectName
							.equals(dependenceThisObjectName))) {
				((DefaultDependence) dependence).load(this);
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
			if(entry == null){
				throw new IllegalStateException("找不到："+file+" 对应的Loader");
			}
			result.add(entry);
		}
		return result;
	}

}
