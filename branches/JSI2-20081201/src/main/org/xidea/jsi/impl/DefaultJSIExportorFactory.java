package org.xidea.jsi.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.ScriptLoader;

/**
 * @author jindw
 */
public class DefaultJSIExportorFactory {
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
	private String type = TYPE_SIMPLE;

	public DefaultJSIExportorFactory(String type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 */
	public JSIExportor createExplorter() {
		if (TYPE_SIMPLE.equals(type)) {
			return createSimpleExplorter();
		} else if (TYPE_CONFUSE.equals(type)) {
			return createConfuseExplorter();
		} else if (TYPE_XML.equals(type)) {
			return createXMLExplorter();
		} else if (TYPE_REPORT.equals(type)) {
			return createReportExplorter();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIExportorFactory#createXMLExplorter()
	 */
	public JSIExportor createXMLExplorter() {
		return new XMLExporter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIExportorFactory#createSimpleExplorter()
	 */
	public JSIExportor createSimpleExplorter() {
		return new SimpleExporter();
	}

	/**
	 * @see org.xidea.jsi.impl.JSIExportorFactory#createConfuseExplorter(java.lang.String,
	 *      java.lang.String, boolean)
	 */
	public JSIExportor createConfuseExplorter() {
		return null;// throw new UnsupportedOperationException("不支持导出方式");
	}

	public JSIExportor createReportExplorter() {
		return null;// throw new UnsupportedOperationException("不支持导出方式");
	}
}

class SimpleExporter implements JSIExportor {

	public String export(JSILoadContext context) {
		StringBuilder result = new StringBuilder();
		List<ScriptLoader> scriptList = context.getScriptList();
		for (ScriptLoader entry : scriptList) {
			result.append(entry.getSource());
			result.append("\r\n;\r\n");
		}
		return result.toString();
	}

}

class XMLExporter implements JSIExportor {

	public String export(JSILoadContext context) {
		StringBuilder content = new StringBuilder(
				"<properties>\n<entry key='#export'>");
		boolean first = true;
		Map<String, String> exportMap = context.getExportMap();
		List<ScriptLoader> scriptList = context.getScriptList();
		for (String object : exportMap.keySet()) {
			if (first) {
				first = false;
			} else {
				content.append(',');
			}
			content.append(exportMap.get(object));
			content.append(':');
			content.append(object);
		}
		;
		content.append("</entry>\n");
		HashMap<String, Object> packageFileMap = new HashMap<String, Object>();
		for (ScriptLoader entry : scriptList) {
			appendEntry(content, entry.getPath(), entry.getSource());
			String packageName = entry.getPackageName();
			if (packageFileMap.get(packageName) == null) {
				packageFileMap.put(packageName, "");
				JSIPackage jsiPackage = entry.getPackage();
				String source = jsiPackage
						.loadText(JSIPackage.PACKAGE_FILE_NAME);
				appendEntry(content, jsiPackage.getName().replace('.', '/')
						+ '/' + JSIPackage.PACKAGE_FILE_NAME, source);
			}
		}
		content.append("</properties>\n");
		return content.toString();
	}

	private void appendEntry(StringBuilder content, String path, String source) {
		content.append("<entry key='");
		content.append(path);
		content.append("'>");
		if (source.indexOf("]]>") < 0) {

			content.append("<![CDATA[");
			content.append(source);
			content.append("]]>");
		} else {
			source = source.replaceAll("&", "&amp;").replaceAll(">", "&gt;")
					.replaceAll("<", "&lt;");
			content.append(source);
		}
		content.append("</entry>\n");
	}

}