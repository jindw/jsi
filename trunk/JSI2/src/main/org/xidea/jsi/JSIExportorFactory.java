package org.xidea.jsi;


public interface JSIExportorFactory {

	public abstract JSIExportor createJSIDocExplorter();

	public abstract JSIExportor createXMLExplorter();

	public abstract JSIExportor createSimpleExplorter();

	public abstract JSIExportor createExplorter(String internalPrefix,String lineSeparator, boolean confuseUnimported);

}