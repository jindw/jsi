package org.xidea.jsi;


/**
 * 工具类，需要设计为线程安全
 * @author jindw
 */
public interface JSIExportor {

	public String export(JSILoadContext context);
}
