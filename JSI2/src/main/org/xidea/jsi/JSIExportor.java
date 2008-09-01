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

	public String export(JSILoadContext context,Map<String, Object> config);
}
