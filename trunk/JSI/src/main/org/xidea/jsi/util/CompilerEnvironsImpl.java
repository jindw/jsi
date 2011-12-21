package org.xidea.jsi.util;

import java.util.ArrayList;
import java.util.Collection;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Parser;

public class CompilerEnvironsImpl extends CompilerEnvirons implements
		ErrorReporter {
	{
		setLanguageVersion(Context.VERSION_1_8);
		setReservedKeywordAsIdentifier(true);
		setGeneratingSource(true);
		setGenerateDebugInfo(true);
		setIdeMode(true);
		setAllowSharpComments(true);
		setRecordingLocalJsDocComments(true);
		setRecordingComments(true);
	}
	private ArrayList<JavaScriptError> errorList = new ArrayList<JavaScriptError>();

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

	public Parser createParser() {
		return new Parser(this, this);
	}
}