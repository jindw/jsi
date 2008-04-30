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
				if (object.indexOf(".") < 0) {
					exportMap.put(object, pkg.getName());
				}
			}
		}

		loadMap.put(id, pkg.getLoaderMap().get(path));
		ScriptStatus status = scriptStatusMap.get(id);
		if (status == null) {
			scriptStatusMap.put(id, status = new ScriptStatus());
		}
		if (status.load(object)) {
			return;
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
				String dependenceObjectName = dependence.getThisObjectName();
				if (!dependence.isAfterLoad()
						&& (dependenceObjectName == null || object == null || object
								.equals(dependenceObjectName))) {
					((DefaultJSIDependence) dependence).load(this);
				}
			}
			if (!loadList.contains(id)) {
				loadList.add(id);
			}
			for (JSIDependence dependence : list) {
				String dependenceObjectName = dependence.getThisObjectName();
				if (dependence.isAfterLoad()
						&& (dependenceObjectName == null || object == null || object
								.equals(dependence.getTargetObjectName()))) {
					((DefaultJSIDependence) dependence).load(this);
				}
			}
		}
	}

	public boolean isLevelSupported(int joinLevel) {
		switch (joinLevel) {
		case JOIN_DIRECT:
		case JOIN_AS_XML:
			return true;
		case JOIN_AS_JSIDOC:
		case JOIN_WITHOUT_ALL_CONFLICTION:
		case JOIN_WITHOUT_INNER_CONFLICTION:
			return false;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.JSILoadContext#export(int)
	 */
	public String export(int joinLevel) {
		if (joinLevel == JOIN_AS_XML) {
			StringBuilder content = new StringBuilder("<script-map export='");
			boolean first = true;
			for (String object : exportMap.keySet()) {
				if (first) {
					first = false;
				} else {
					content.append(',');
				}
				content.append(exportMap.get(object));
				content.append(':');
				content.append(object);
			}
			;
			content.append("'>\n");
			HashMap<String, Object> packageFileMap = new HashMap<String, Object>();
			for (String file : loadList) {
				org.xidea.jsi.ScriptLoader entry = loadMap.get(file);
				appendEntry(content, file, entry.getSource());
				String packageName = entry.getPackageName();
				if (packageFileMap.get(packageName) == null) {
					packageFileMap.put(packageName, "");
					JSIPackage jsiPackage = entry.getPackage();
					String source = jsiPackage
							.loadText(JSIPackage.PACKAGE_FILE_NAME);
					appendEntry(content, jsiPackage.getName().replace('.', '/')
							+ '/' + JSIPackage.PACKAGE_FILE_NAME, source);
				}
			}
			content.append("</script-map>\n");
			return content.toString();
		} else if (joinLevel == 0) {
			StringBuilder result = new StringBuilder();
			for (String file : loadList) {
				org.xidea.jsi.ScriptLoader entry = loadMap.get(file);
				result.append(entry.getSource());
				result.append("\r\n;\r\n");
			}
			return result.toString();
		} else {
			throw new UnsupportedOperationException("不支持导出级别");
		}
	}

	private void appendEntry(StringBuilder content, String file, String source) {
		content.append("<script path='");
		content.append(file);
		content.append("'>");
		source = source.replaceAll("&", "&amp;").replaceAll(">", "&gt;")
				.replaceAll("<", "&lt;");
		content.append(source);
		content.append("</script>\n");
	}
	public void setPerfix(String prefix){
		;
	}
	// loadContext.setBegin(0);
	//loadContext.setLineSeparator("\r\n\r\n");
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
