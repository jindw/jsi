package cn.jside.jsi.tools.rhino;

import org.jside.jsi.tools.JavaScriptCompressionAdvisor;
import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.mozilla.javascript.Parser;

/**
 * @author jinjinyun bug for var A; function (b){ A = b; }
 * @see DeleteCompressor
 * @see ReplaceCompressor
 * @see check(result)
 */
public class SyntaxCompressor extends RhinoTool {

	private ErrorHolder errorHolder = new ErrorHolder(true);
	private Parser parser = RhinoTool.createParser(new CompilerEnvironsImpl(), errorHolder);
	DeleteCompressor deleteCompressor = new DeleteCompressor(parser);
	ReplaceCompressor replaceCompressor = new ReplaceCompressor(parser);
	Delete2Compressor syntaxTextCompressor = new Delete2Compressor(parser);
	private JavaScriptCompressorConfig config;

	public synchronized String compress(String source,
			JavaScriptCompressionAdvisor advisor) {
		String result = deleteCompressor.compress(source, advisor);
		result = replaceCompressor.compress(result, advisor);
		if (config.isTrimBracket()) {
			result = syntaxTextCompressor.compress(result, advisor);
		}
		return check(result);
	}

	private String check(String result) {
		try {
			parser.parse(result, "", 1);
			return result;
		} catch (Exception e) {
			throw new IllegalStateException(
					"压缩后从新解析失败，压缩工具可能有bug，请将压缩的文件提交给 xidea.org，以供测试。", e);

		}
	}

	public void setCompressorConfig(JavaScriptCompressorConfig config) {
		this.config = config;
		deleteCompressor.setCompressorConfig(config);
		replaceCompressor.setCompressorConfig(config);
		syntaxTextCompressor.setCompressorConfig(config);

	}

}
