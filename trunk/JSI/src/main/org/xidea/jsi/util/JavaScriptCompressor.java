package org.xidea.jsi.util;


public interface JavaScriptCompressor {

	public String format(String script);
	public String compress(String source,ReplaceAdvisor replaceAdvisor);
	public float getLatestRate();
	public void setConfig(JavaScriptConfig config);
}