package org.xidea.jsi.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIExportorFactory;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.ScriptLoader;

/**
 * @author jindw
 */
public class DefaultJSIExportorFactory implements JSIExportorFactory {

	/* (non-Javadoc)
	 * @see org.xidea.jsi.impl.JSIExportorFactory#createJSIDocExplorter()
	 */
	public JSIExportor createJSIDocExplorter() {
		throw new UnsupportedOperationException("不支持导出方式");
	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.impl.JSIExportorFactory#createXMLExplorter()
	 */
	public JSIExportor createXMLExplorter() {
		return new XMLExporter();
	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.impl.JSIExportorFactory#createSimpleExplorter()
	 */
	public JSIExportor createSimpleExplorter() {
		return new SimpleExporter();
	}

	/**
	 * @see org.xidea.jsi.JSIExportorFactory#createExplorter(java.lang.String, int, java.lang.String, boolean)
	 */
	public JSIExportor createExplorter(String internalPrefix, int startIndex,
			String lineSeparator, boolean confuseUnimported) {
		throw new UnsupportedOperationException("不支持导出方式");
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
		StringBuilder content = new StringBuilder("<script-map export='");
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
		content.append("'>\n");
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
		content.append("</script-map>\n");
		return content.toString();
	}

	private void appendEntry(StringBuilder content, String path, String source) {
		content.append("<script path='");
		content.append(path);
		content.append("'>");
		source = source.replaceAll("&", "&amp;").replaceAll(">", "&gt;")
				.replaceAll("<", "&lt;");
		content.append(source);
		content.append("</script>\n");
	}

}