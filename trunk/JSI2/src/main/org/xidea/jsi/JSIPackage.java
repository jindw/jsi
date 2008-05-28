package org.xidea.jsi;

import java.util.List;
import java.util.Map;




/**
 * 包对象，属于JSIRoot实例的数据，
 * 该对象需要在多个LoadContext中共享数据。
 * 实现中需要注意线程安全设计。
 * @scope Application
 * @see org.xidea.jsi.impl.DefaultJSIPackage
 * @author jindw
 */
public interface JSIPackage {

	public static final String PACKAGE_FILE_NAME = "__package__.js";

	public abstract void initialize();

	public abstract String getName();

	public void setImplementation(String implementation);
	
	public void addScript(String scriptName, Object objectNames,
			Object beforeLoadDependences, Object afterLoadDependences);
	
	public void addDependence(String thisPath, Object targetPath,
			boolean afterLoad);
	
	public abstract String getImplementation();

	
	
	
	public abstract Map<String, List<String>> getScriptObjectMap();

	public abstract Map<String, String> getObjectScriptMap();

	public abstract Map<String, List<JSIDependence>> getDependenceMap();
	/**
	 * 此处的loaderMap与JSI脚本中的loaderMap不同。loaderMap在包初始化时就一稳定
	 * @return
	 */
	public abstract Map<String, ScriptLoader> getLoaderMap();

	public abstract String loadText(String scriptName);


}