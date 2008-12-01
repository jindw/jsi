package org.xidea.jsi;

import java.util.Collection;
import java.util.Map;


/**
 * 装载单元接口， 装载单元属于JSIPackage对象的数据，可在多个LoadContext中共享。
 * 实现中需要注意线程安全设计。
 * @scope Application
 * @author jindw
 */
public interface ScriptLoader { 

	public abstract JSIPackage getPackage();

	public abstract String getName();

	public abstract String getPackageName();

	public abstract String getPath();

	public abstract Map<String, String> getDependenceVarMap();

	public abstract Collection<String> getDependenceVars();

	public abstract Collection<String> getLocalVars();

	public abstract String getSource();

}