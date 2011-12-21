package org.xidea.jsi.util;

public class JavaScriptError {

	private String lineSource;
	private String message;
	private int line;
	private int lineOffset;

	public JavaScriptError(String lineSource, String message, int line,
			int lineOffset) {
		this.lineSource = lineSource;
		this.message = message;
		this.line = line;
		this.lineOffset = lineOffset;
	}

	public JavaScriptError(String message) {
		this.message = message;
	}

	public JavaScriptError(Exception message) {
		this.message = message.getMessage();
	}

	public String getLineSource() {
		return lineSource;
	}

	public String getMessage() {
		return message;
	}

	public int getLine() {
		return line;
	}

	public int getLineOffset() {
		return lineOffset;
	}

	public String toString() {
		return "error:"+this.message + "\nin:" + this.lineSource + "\n@[" + this.line + ","
				+ this.lineOffset + "]\n\n";
	}

}
