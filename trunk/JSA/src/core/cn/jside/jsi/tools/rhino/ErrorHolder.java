package cn.jside.jsi.tools.rhino;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

public class ErrorHolder implements ErrorReporter {
	StringBuffer buf = new StringBuffer();
	private boolean log = true;

	public ErrorHolder(boolean log) {
		this.log = log;
	}

	public String getAndClean() {
		String rtv = buf.toString();
		buf = new StringBuffer();
		return rtv;
	}

	public void error(String message, String sourceName, int line,
			String lineSource, int lineOffset) {
		if (log) {
			buf.append("ERROR:");
			buf.append("message:" + message + ";lineSource" + lineSource);
			buf.append("\n");
			buf.append("sourceName:" + sourceName + ";line:" + line
					+ ";lineOffset:" + lineOffset);
			buf.append("\n");
			buf.append("\n");
			
			//DebugTool.println(buf);
		}
	}

	public EvaluatorException runtimeError(final String message,
			final String sourceName, final int line, final String lineSource,
			final int lineOffset) {
		return new EvaluatorException(message, sourceName, line, lineSource,
				lineOffset) {
			String msg = null;

			public String toString() {
//				return "message:" + message + "\nlineSource" + lineSource
//						+ "\nsourceName:" + sourceName + ";line:" + line
//						+ ";lineOffset:" + lineOffset;
				return msg != null? msg:(msg = getAndClean());
			}
		};
	}

	public void warning(String message, String sourceName, int line,
			String lineSource, int lineOffset) {
		if (log) {
			buf.append("WARNING:");
			buf.append("message:" + message + ";lineSource" + lineSource);
			buf.append("\n");
			buf.append("sourceName:" + sourceName + ";line:" + line
					+ ";lineOffset:" + lineOffset);
			buf.append("\n");
			buf.append("\n");
		}
	}
}
