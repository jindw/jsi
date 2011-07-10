package org.jside.jsi.tools.export;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jside.jsi.tools.JSAToolkit;
import org.jside.jsi.tools.JavaScriptAnalysisResult;
import org.jside.jsi.tools.JavaScriptCompressor;
import org.jside.jsi.tools.JavaScriptError;
import org.jside.jsi.tools.util.JavaScriptConstants;
import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.ScriptLoader;

public class JSAReportExplorter implements JSIExportor {
	protected JavaScriptCompressor compressor = JSAToolkit.getInstance()
			.createJavaScriptCompressor();

	public String export(JSILoadContext context) {
		// HashMap<String, AnalysisResult> entryInfoMap = new HashMap<String,
		// AnalysisResult>();
		StringWriter result = new StringWriter();
		PrintWriter out = new PrintWriter(result);
		for (ScriptLoader scriptLoader : context.getScriptList()) {
			out.print("文件:");
			out.println(scriptLoader.getPath());
			final String source = scriptLoader.getSource();
			JavaScriptAnalysisResult info = compressor.analyse(source);
			
			List<JavaScriptError> errors = info.getErrors();
			if (errors != null && !errors.isEmpty()) {
				out.println();
				out.println("!!!!脚本错误!!!!!");
				out.println(errors);
			}
			if (info.isUnsafe()) {// 保留全部本地变量
				out.println("不安全脚本");
			}
			out.print("公开变量集：");
			out.println(new TreeSet<String>(scriptLoader.getLocalVars()));

			out.print("本地变量集：");
			out.println(new TreeSet<String>(info.getLocalVars()));

			out.print("依赖变量集：");
			out.println(new TreeMap<String, String>(scriptLoader
					.getDependenceVarMap()));

			out.print("覆盖变量集：");
			TreeSet<String> override = new TreeSet<String>(info.getLocalVars());
			HashSet<String> protecteds = new HashSet<String>(
					JavaScriptConstants.BROWSER_VARIBALES);
			protecteds.addAll(JavaScriptConstants.ECMA_VARIBALES);
			protecteds.addAll(scriptLoader.getDependenceVars());
			override.retainAll(protecteds);
			out.println(override);

			out.print("未知变量集：");
			TreeSet<String> unknow = new TreeSet<String>(info.getExternalVars());
			unknow.removeAll(scriptLoader.getDependenceVars());
			unknow.removeAll(JavaScriptConstants.ECMA_VARIBALES);
			unknow.removeAll(JavaScriptConstants.BROWSER_VARIBALES);
			out.println(unknow);

			out.println();
			out.println();
			out.println("================= 格式化后源码 =============");
			out.println(compressor.format(source));
			out.println();
		}
		out.flush();
		result.flush();
		return result.toString();
	}
}