package org.xidea.jsi;






/**
 * JSI实例，是一个JSI资源的表示，其下有若干JSIPackage实例。
 * 该对象需要在多个LoadContext中共享数据，并保证多个$import并发执行时，不相互影响。
 * 实现中需要注意线程安全设计。
 * @scope Application
 * @author jindw
 */
public interface JSIRoot {
	public abstract String loadText(String absPath);
}