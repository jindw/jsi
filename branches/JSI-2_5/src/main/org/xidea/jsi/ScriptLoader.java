package org.xidea.jsi;

import java.util.Collection;
import java.util.Map;

import org.xidea.jsi.impl.DefaultScriptLoader;


/**
 * 装载单元接口， 装载单元属于JSIPackage对象的数据，可在多个LoadContext中共享。
 * 实现中需要注意线程安全设计。
 * @scope Application
 * @see DefaultScriptLoader
 * @author jindw
 */
public interface ScriptLoader { 

	/**
	 * @return real package
	 */
	public abstract JSIPackage getPackage();

	/**
	 * @return short name(without path)
	 */
	public abstract String getName();

	public abstract String getPath();

	/**
	 * varName -> targetPackageName(real package)
	 * @return
	 */
	public abstract Map<String, String> getDependenceVarMap();

	public abstract Collection<String> getDependenceVars();

	public abstract Collection<String> getLocalVars();

	public abstract String getSource();

}