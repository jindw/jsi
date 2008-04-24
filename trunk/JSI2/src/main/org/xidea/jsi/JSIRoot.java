package org.xidea.jsi;

import java.util.List;



/**
 * JSI实例，是一个JSI资源的表示，其下有若干JSIPackage实例。
 * 该对象需要在多个LoadContext中共享数据，并保证多个$import并发执行时，不相互影响。
 * 实现中需要注意线程安全设计。
 * @scope Application
 * @author jindw
 */
public interface JSIRoot {

	public abstract JSILoadContext $import(String path, JSILoadContext context);
	public abstract JSILoadContext $import(String path);

	public abstract JSIPackage requirePackage(String name, boolean exact);

	public abstract JSIPackage findPackageByPath(String path);

	public abstract void debug(String info);

	public abstract void error(String info);

	public abstract String loadText(String name,
			String scriptName);

}