package org.xidea.jsi;

import java.util.Map;

/**
 * 工具类，需要设计为线程安全
 * @author jindw
 */
public interface JSIExportor {
	public static final String KEY_LINE_SEPARATOR = "#lineSeparator";//"\r\n\r\n";
	public static final String KEY_INTERNAL_PREFIX = "#internalPrefix";//"__$"
	public static final String KEY_PRESERVE_IMPORTABLE = "#preserveImportable";
	/**
	 * 混淆隔离支持的导出合并实现
	 */
	public static final String TYPE_CONFUSE = "confuse";
	/**
	 * 创建问题报告实现
	 */
	public static final String TYPE_REPORT = "report";
	/**
	 * 简单的直接导出合并实现，一般用于运行时合并
	 */
	public static final String TYPE_SIMPLE = "simple";
	/**
	 * 创建XML数据打包实现 创建问题报告实现
	 */
	public static final String TYPE_XML = "xml";

	public String export(JSILoadContext context,Map<String, String> config);
}
