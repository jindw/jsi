package org.xidea.jsi;


public interface JSIExportorFactory {
	
	public abstract JSIExportor createSimpleExplorter();

	public abstract JSIExportor createConfuseExplorter(String internalPrefix,String lineSeparator, boolean confuseUnimported);

	public abstract JSIExportor createJSIDocExplorter();

	public abstract JSIExportor createXMLExplorter();
	
	public abstract JSIExportor createReportExplorter();


}