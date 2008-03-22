package org.xidea.jsi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ScriptLoader {
	private JSIPackage pkg;
	private String name;
	private String source;
	private Map<String, String> dependenceVarMap;
	private Collection<String> localVars;

	public ScriptLoader(JSIPackage pkg, String name) {
		this.pkg = pkg;
		this.name = name;
	}

	public JSIPackage getPackage() {
		return pkg;
	}

	public String getName() {
		pkg.getScriptObjectMap().get(this.name);
		return name;
	}

	public Map<String, String> getDependenceVarMap() {
		if (this.dependenceVarMap == null) {
			List<JSIDependence> list = pkg.getDependenceMap().get(name);
			HashMap<String, String> dependenceVarMap = new HashMap<String, String>();
			if (list != null) {
				for (JSIDependence dependence : list) {
					List<String> buf = new ArrayList<String>();
					dependence.initialize(buf);
					for (String name : buf) {
						dependenceVarMap.put(name, pkg.getName());
					}
				}
			}
			this.dependenceVarMap = dependenceVarMap;
		}
		return this.dependenceVarMap;
	}

	public Collection<String> getDependenceVars() {
		return getDependenceVarMap().keySet();
	}

	public Collection<String> getLocalVars() {
		if (this.localVars == null) {
			ArrayList<String> localVars = new ArrayList<String>();
			for (String name : pkg.getObjectScriptMap().keySet()) {
				if (name.indexOf(".") < 0) {
					localVars.add(name);
				}
			}
			this.localVars = localVars;
		}
		return this.localVars;
	}

	public String getSource() {
		if (source == null) {
			source = pkg.loadText(this.name);
		}
		return source;
	}
}
