package org.jside.jsi.tools;


public interface JavaScriptCompressor {

	public JavaScriptAnalysisResult analyse(String source);
	public String format(String script);
	public String compress(String source,JavaScriptCompressionAdvisor compressionAdvisor);
	public float getLatestRate();
	public void setCompressorConfig(JavaScriptCompressorConfig config);
}