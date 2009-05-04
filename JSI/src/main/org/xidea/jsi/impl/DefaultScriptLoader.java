package org.xidea.jsi.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xidea.jsi.JSIDependence;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.ScriptLoader;

/**
 * 线程安全的对象。
 * 
 * @scope Application
 * @see ScriptStatus
 * @author jindw
 */
public class DefaultScriptLoader implements ScriptLoader {
	private String path;
	private JSIPackage _package;
	private String name;
	private String source;
	private Map<String, String> dependenceVarMap;
	private Collection<String> localVars;

	// 都是一些字符串，无需同步
	// private Object lock = new Object();

	public DefaultScriptLoader(JSIPackage pkg, String name) {
		this._package = pkg;
		this.name = name;
		this.path = pkg.getName().replace('.', '/') + '/' + name;
	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.impl.ScriptEntry#getPackage()
	 */
	public JSIPackage getPackage() {
		return _package;
	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.impl.ScriptEntry#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.impl.ScriptEntry#getPath()
	 */
	public String getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.impl.ScriptEntry#getDependenceVarMap()
	 */
	public Map<String, String> getDependenceVarMap() {
		if (this.dependenceVarMap == null) {
			List<JSIDependence> list = ((DefaultPackage)_package).getDependenceMap().get(name);
			HashMap<String, String> dependenceVarMap = new HashMap<String, String>();
			if (list != null) {
				for (JSIDependence dependence : list) {
					List<String> buf = new ArrayList<String>();
					((DefaultDependence)dependence).initialize(buf);
					for (String name : buf) {
						dependenceVarMap.put(name, dependence.getTargetPackage().getName());
					}
				}
			}
			this.dependenceVarMap = dependenceVarMap;

		}
		return this.dependenceVarMap;
	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.impl.ScriptEntry#getDependenceVars()
	 */
	public Collection<String> getDependenceVars() {
		return getDependenceVarMap().keySet();
	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.impl.ScriptEntry#getLocalVars()
	 */
	public Collection<String> getLocalVars() {
		if (this.localVars == null) {
			ArrayList<String> localVars = new ArrayList<String>();
			for (String var : _package.getObjectScriptMap().keySet()) {

				if (this.name.equals(_package.getObjectScriptMap().get(var))
						&& var.indexOf(".") < 0) {
					localVars.add(var);
				}
			}
			this.localVars = localVars;
		}
		return this.localVars;
	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.impl.ScriptEntry#getSource()
	 */
	public String getSource() {
		if (source == null) {
			source = _package.loadText(this.name);
		}
		return source;
	}
	public String toString(){
		return this.path;
	}

}
