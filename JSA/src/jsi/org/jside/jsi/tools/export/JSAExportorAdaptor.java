package org.jside.jsi.tools.export;

import java.util.List;
import java.util.Map;

import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.ScriptLoader;

public class JSAExportorAdaptor implements JSIExportor {
	private String lineSeparator = "\r\n";
	private String internalPrefix;
	private boolean preserve;
	private JavaScriptCompressorConfig config;
	
	public void setInternalPrefix(String internalPrefix) {
		this.internalPrefix = internalPrefix;
	}
	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}
	public void setPreserve(boolean preserve) {
		this.preserve = preserve;
	}
	public void setConfig(JavaScriptCompressorConfig config) {
		this.config = config;
	}
	public JavaScriptCompressorConfig getConfig() {
		return this.config;
	}
	
	public JSAExportorAdaptor(){
		config = new JavaScriptCompressorConfig();
	}
	public JSAExportorAdaptor(JavaScriptCompressorConfig config,String internalPrefix, boolean preserveImportable) {
		super();
		this.config = config;
		if(internalPrefix != null){
			this.internalPrefix = internalPrefix;
		}
		this.preserve = preserveImportable;
	}

	public String export(JSILoadContext context) {
		return join(export(context.getScriptList(),context.getExportMap()),	lineSeparator);
	}
	public Map<String, String> export(List<ScriptLoader> list,Map<String, String> exportMap){
		JSAAbstractExporter exporterInstance = createExporterInstance(config, internalPrefix,preserve);
		return  exporterInstance.export(list,exportMap);
	}
	private static JSAAbstractExporter createExporterInstance(JavaScriptCompressorConfig compressorConfig,String internalPrefix, boolean preserve) {
		if (preserve) {
			return new JSAPreserveExporter(compressorConfig,
					internalPrefix, 0);
		} else {
			return new JSAConfuseExporter(compressorConfig,
					internalPrefix, 0);
		}

	}
	static String join(Map<String, String> resultMap,String lineSeparator) {
		lineSeparator = lineSeparator.replaceAll("\\\\r","\r").replaceAll("\\\\n","\n");
		StringBuilder buf = new StringBuilder();
		for (final String path : resultMap.keySet()) {
			String compressed = resultMap.get(path);
			if (buf.length() > 0) {
				if (compressed.startsWith("/*")) {
					compressed = compressed.replaceFirst(
							"^\\s*/\\*[\\s\\S]*?\\*/\\s*", "");
				}
				if (compressed.startsWith("(")) {// 有待优化
					// char last = buf.charAt(buf.length()-1);
					// if(";+-".indexOf(last)<0){
					buf.append(";");
					// }

				}
				buf.append(lineSeparator);
			}
			buf.append(compressed);
		}
		return buf.toString();
	}
}
