package org.xidea.jsi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JSILoadContext {

	private Map<String, ScriptStatus> scriptStatusMap = new HashMap<String, ScriptStatus>();
	private List<String> loadList = new ArrayList<String>();
	private Map<String,ScriptLoader> loadMap = new HashMap<String,ScriptLoader>();

	public void loadScript(JSIPackage pkg, final String path, final String object) {
		String id = pkg.getName().replace('.', '/') + "/" + path;
		//ScriptLoader loader = ;
		loadMap.put(id, pkg.getLoaderMap().get(path));
		ScriptStatus status = scriptStatusMap.get(id);
		if (status == null) {
			scriptStatusMap.put(id, status = new ScriptStatus());
		}
		if (status.testObject(object)) {
			return;
		}
		pkg.initialize();
		List<JSIDependence> list = pkg.getDependenceMap().get(path);
		if (list == null) {
			if (!loadList.contains(id)) {
				loadList.add(id);
			}
		} else {
			for (JSIDependence dependence : list) {
				String dependenceObjectName = dependence.getObjectName();
				if (!dependence.isAfterLoad()
						&& (dependenceObjectName == null || object == null || object.equals(dependenceObjectName))) {
					dependence.load(this);
				}
			}
			if (!loadList.contains(id)) {
				loadList.add(id);
			}
			for (JSIDependence dependence : list) {
				String dependenceObjectName = dependence.getObjectName();
				if (dependence.isAfterLoad()
						&& (dependenceObjectName == null || object == null || object.equals(dependence
								.getObjectName()))) {
					dependence.load(this);
				}
			}
		}
	}
	public List<String> getFileList() {
		return loadList;
	}
	public String getSource(String path) {
		return loadMap.get(path).getSource();
	}
	public String export(){
		for (String file : loadList) {
			System.out.println(file);
		}
		return null;
	}
}
