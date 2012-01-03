package org.xidea.jsi.impl.v2;

import java.util.List;
import java.util.Map;

import org.xidea.jsi.ScriptLoader;




/**
 * 包对象，属于JSIRoot实例的数据，
 * 该对象需要在多个LoadContext中共享数据。
 * 实现中需要注意线程安全设计。
 * @scope Application
 * @see org.xidea.jsi.impl.v2.DefaultPackage
 * @author jindw
 */
public interface JSIPackage {

	public static final String PACKAGE_FILE_NAME = "__package__.js";

	public abstract void initialize();

	public abstract String getName();

	public void setImplementation(String implementation);
	
	/**
	 * @param scriptName
	 * @param objectNames
	 * @param beforeLoadDependences String||Collection
	 * @param afterLoadDependences String||Collection
	 * 
	 * @see DefaultPackage#addScript(String, Object, Object, Object)
	 */
	public void addScript(String scriptName, Object objectNames,
			Object beforeLoadDependences, Object afterLoadDependences);
	
	/**
	 * @param thisPath
	 * @param targetPath String||Collection
	 * @param afterLoad
	 * @see DefaultPackage#addDependence(String, Object, boolean)
	 */
	public void addDependence(String thisPath, Object targetPath,
			boolean afterLoad);
	
	public abstract String getImplementation();

	public abstract Map<String, List<String>> getScriptObjectMap();

	public abstract Map<String, String> getObjectScriptMap();

	/**
	 * thisFile -> DependenceList
	 * @return
	 */
	public abstract Map<String, List<JSIDependence>> getDependenceMap();
	/**
	 * 此处的loaderMap与JSI脚本中的loaderMap不同。loaderMap在包初始化时就一稳定
	 * @return
	 */
	public abstract Map<String, ScriptLoader> getLoaderMap();

	/**
	 * 获取包内脚本资源的源代码，不要在这里缓存源码！！要做缓存的话，请在JSIRoot中做
	 * @param scriptName
	 * @return
	 */
	public abstract String loadText(String scriptName);


}