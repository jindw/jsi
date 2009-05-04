package org.xidea.jsi;





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

	/**
	 * 换回具体的实现包（只管获取，不做初始化）
	 * @see org.xidea.jsi.impl.AbstractRoot#requirePackage(String, boolean)
	 * @param name
	 * @param exact
	 * @return
	 */
	public abstract JSIPackage requirePackage(String name, boolean exact);

	/**
	 * @see org.xidea.jsi.impl.AbstractRoot#findPackageByPath(String)
	 * @param path
	 * @return
	 */
	public abstract JSIPackage findPackageByPath(String path);

	/**
	 * 获取脚本源文件
	 * @param packageName
	 * @param scriptName
	 * @return 资源不存在时反回null
	 */
	public abstract String loadText(String packageName,
			String scriptName);

}