package cn.jside.jsi.tools.rhino;

import org.jside.jsi.tools.JavaScriptAnalysisResult;
import org.jside.jsi.tools.JavaScriptCompressionAdvisor;
import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.NodeTransformer;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Token;

import cn.jside.jsi.tools.JavaScriptCompressorAdaptor;
import cn.jside.jsi.tools.rhino.block.ScriptScopReplacer;

public class RhinoCompressor {
	
	private AnalyserErrorReporter errorReporter = new AnalyserErrorReporter();
	private Parser parser = RhinoTool.createParser(new CompilerEnvironsImpl(), errorReporter);


	private SyntaxCompressor syntaxCompressor = new SyntaxCompressor();

	private TextCompressor textCompressor = new TextCompressor();

	private JavaScriptCompressorConfig config;
	/**
	 * @param source
	 * @return
	 */
	public JavaScriptAnalysisResult analyse(String source) {
		// 包信息集合.getExposedVariables(filePath);
		ScriptOrFnNode root = null;
		errorReporter.clearErrors();
		try {
			root = parser.parse(source, "<jsfile>", 0);
			new NodeTransformer().transform(root);
			int fncount = root.getFunctionCount();
			for (int i = 0; i < fncount; i++) {
				FunctionNode fn = root.getFunctionNode(i);
				fn.getLineno();
			}
		} catch (RuntimeException ex) {
			errorReporter.error(ex.getMessage(), null, 0, "", 0);
			throw ex;
		}
		return new AnalysisResultImpl(new ScriptScopReplacer(
				root, JavaScriptCompressorAdaptor.EMPTY_ADVISOR), errorReporter.getErrors());

	}
	/**
	 * @param source
	 * @param globalReplacer
	 * @return
	 */
	public String compress(String source, JavaScriptCompressionAdvisor globalReplacer) {
		// source = source.replaceFirst("^/\\*(?:.*)?\\*/|^//(?:.*)?[\r\n]*",
		// "");
		String value = null;
		if (config.isSyntaxCompression()) {
			String source2 = null;
			PreTextProcess preTextProcess = null;
			if (source.indexOf("@cc_on") > 0) {// JScript 条件编译
				preTextProcess = new PreTextProcess();
				source2 = preTextProcess.encodeComment(source, false);
				source2 = preTextProcess.getConditionalCompilationCount() > 0 ? source2
						: null;
			}
			if (source2 != null) {
				Parser p = RhinoTool.createParser(new CompilerEnvironsImpl(),
						new ToolErrorReporter(true));
				p.parse(source2, "", 1024);
				source2 = RhinoTool.decode(p.getEncodedSource()
						.toCharArray(), null, null);
				value = preTextProcess.decodeComment(source2, false);
				// if (转换大字符) {
				// value = 转换大字符(value);
				// }
			} else {
				value = this.syntaxCompressor.compress(source, globalReplacer);

				double rate = value.length() * 1.0 / source.length();
				DebugTool.debug("压缩比率为："+rate);
			}
		} else {
			value = source;
		}
		if (this.config.isTextCompression()
				&& config.getRatioCondition() > 0
				&& value.length() > config.getSizeCondition()) {
			DebugTool.info("尝试文本压缩：");
			String value2 = textCompressor.compress(value, config
					.isCompatible());
			double rate = value2.length() * 1.0 / value.length();
			DebugTool.info("编码比率为："+rate);
			if (rate < config.getRatioCondition()) {
				value = value2;
			} else {
			}
		}
		double rate = value.length() * 1.0 / source.length();
		value = getCopyright() + value;
		DebugTool.debug("总压缩比率为："+rate);
		return value;
	}

	private String copyright = null;

	private String getCopyright() {
		if (copyright == null) {
			String LINE_SEPARATOR = System.getProperty("line.separator");
			String baseCopyright = this.config.getCopyright();
			copyright = "/*"
					+ LINE_SEPARATOR
					+ " * "
					+ baseCopyright.replaceAll("\\s*\r\n\\s*" + "|\\s*\n\r\\s*"
							+ "|\\s*\r\\s*" + "|\\s*\n\\s*", LINE_SEPARATOR
							+ " * ") + LINE_SEPARATOR + " */" + LINE_SEPARATOR;
		}
		return copyright;
	}

	public String format(String source) {
		Parser p = RhinoTool.createParser(new CompilerEnvironsImpl(), new ToolErrorReporter(
				true));
		ScriptOrFnNode root = p.parse(source, "", 1024);
		// Rhino调试工具.print(root);
		Node node = root.getFirstChild();
		if (node == null) {
			return source;
		}
		if (node.getType() == Token.EXPR_RESULT && node.getNext() == null) {
			node = node.getFirstChild();
			if (node.getType() == Token.CALL && node.getNext() == null) {
				node = node.getFirstChild();
				if (node.getType() == Token.NAME
						&& "eval".equals(node.getString())) {
					try {
						final String text = RhinoTool.decode(p
								.getEncodedSource().toCharArray(), "", "");
						// DebugTool.println(text.replaceFirst("eval", ""));
						Object result = Context.call(new ContextAction() {
							public Object run(final Context cx) {
								Scriptable scope = ScriptRuntime.getGlobal(cx);
								return cx.evaluateString(scope, text
										.replaceAll("^eval|;$", "")
										+ "+''", "", 0, null);
							}
						});// text.replaceFirst("eval", "")+
						// DebugTool.println("result:"+result);
						String value = (String) result;
						source = value;
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}

		try {
			PreTextProcess 预处理 = new PreTextProcess();
			String value = 预处理.encodeComment(source, true);
			p.parse(value, "", 1024);
			value = RhinoTool.decode(p.getEncodedSource().toCharArray(),
					"\r\n", "  ");
			return 预处理.decodeComment(value, true);
		} catch (Throwable e) {
			e.printStackTrace();
			p.parse(source, "", 1024);
			return RhinoTool.decode(p.getEncodedSource().toCharArray(),
					"\r\n", "  ");
		}
	}

	public void setCompressorConfig(JavaScriptCompressorConfig config) {
		this.config = config;
		this.syntaxCompressor.setCompressorConfig(config);
	}
}
