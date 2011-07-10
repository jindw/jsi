package cn.jside.jsi.tools.rhino;

import java.util.ArrayList;
import java.util.Collection;

import org.jside.jsi.tools.JavaScriptError;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

public class AnalyserErrorReporter implements ErrorReporter {
	private ArrayList<JavaScriptError> errorList = null;

	public void error(String message, String sourceName, int line,
			String lineSource, int lineOffset) {
		errorList.add(0, new JavaScriptError(lineSource, message, line,
				lineOffset));
	}

	public void warning(String message, String sourceName, int line,
			String lineSource, int lineOffset) {
		errorList.add(0, new JavaScriptError(lineSource, message, line,
				lineOffset));
	}

	public EvaluatorException runtimeError(String message, String sourceName,
			int line, String lineSource, int lineOffset) {
		error(message, sourceName, line, lineSource, lineOffset);
		return new EvaluatorException(message, sourceName, line, lineSource,
				lineOffset);
	}

	public Collection<JavaScriptError> getErrors() {
		return errorList;
	}

	public void clearErrors() {
		errorList = new ArrayList<JavaScriptError>();
	}

}
