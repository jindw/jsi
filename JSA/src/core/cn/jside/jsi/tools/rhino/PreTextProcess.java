package cn.jside.jsi.tools.rhino;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jinjinyun
 */
public class PreTextProcess {
	private String __tempReplacer = null;

	private int conditionalCompilationCount;

	public int getConditionalCompilationCount() {
		return conditionalCompilationCount;
	}

	/**
	 * 编码注释,将注释编码为JS不安全函数调用
	 */
	public String encodeComment(String value, boolean encodeAll) {
		
		// /\\*(?:[^\\*]|\\*[^/])*\\*/
		//
		Matcher ms = Pattern.compile("/\\*(?:[\\s\\S])*?\\*/" + "|" + // muti-comment
				"//[^\\r\\n]*"
				+"|" + // single comment
				//TODO:这里导致了溢出
				"/(?:\\\\.|[^/\\n\\r])+/|" + // regexp
				"\"(?:\\\\.|\\\\\\r\\n|[^\"\\n\\r])*\"|" + // string
				"'(?:\\\\.|\\\\\\r\\n|[^'\\n\\r])*'"// string
		, Pattern.MULTILINE).matcher(value);
		Matcher objectLit = Pattern.compile(
				"(?:" + "\\s*" + "/\\*(?:[\\s\\S])*?\\*/|" + // muti-comment
						"//[^\\r\\n]*$" + // single comment
						")*" + "\\s*(?:" + "[\\w_$][\\w_$\\d]*\\s*:|"// id
						+ "\"(?:\\\\.|\\\\\\r\\n|[^\"\\n\\r])*\"\\s*:|" + // string
						"'(?:\\\\.|\\\\\\r\\n|[^'\\n\\r])*'\\s*:" + // string
						")").matcher(value);
		String tempReplacer = "__tempReplacer__";
		while (value.indexOf(tempReplacer) > 0) {
			tempReplacer += "_";
		}
		__tempReplacer = tempReplacer;
		int pos = 0;
		StringBuilder buf = new StringBuilder();
		int ccCount = 0;
		while (ms.find()) {
			String text = ms.group();

			if (text.startsWith("/*") || text.startsWith("//")) {
				buf.append(value.substring(pos, ms.start()));
				if (encodeAll) {
				} else if (text.startsWith("/*@") && text.endsWith("@*/")) {
					//DebugTool.println(text);
					ccCount++;
				} else if (text.startsWith("//@")) {
					ccCount++;
				} else {
					pos = ms.end();
					continue;
				}
				buf.append(tempReplacer);
				int end = ms.end();
				if (objectLit.find(end) && objectLit.start() == end) {
					// DebugTool.println(text);
					// DebugTool.println(对象属性.group());
					buf.append(":"+__tempReplacer+".");
					buf.append(encode(text));
					buf.append(",");
				} else {
					buf.append("."+__tempReplacer+".");
					buf.append(encode(text));
					buf.append(";");
				}

				pos = ms.end();
				continue;
			} else {
				buf.append(value.substring(pos, ms.end()));
				pos = ms.end();
			}
		}
		buf.append(value.substring(pos));
		this.conditionalCompilationCount = ccCount;
		return buf.toString();
	}
	private String encode(String value){
		StringBuilder buf = new StringBuilder();
		for(char c : value.toCharArray()){
			buf.append('_');
			buf.append(Integer.toString(c,32));
		}
		return buf.toString();
	}

	private String decode(String value){
		StringBuilder buf = new StringBuilder();
		for(String c : value.substring(1).split("[_]")){
			buf.append((char)Integer.parseInt(c,32));
		}
		return buf.toString();
	}

	public String decodeComment(String value, boolean includeNCC) {
		if (__tempReplacer != null && value.indexOf(__tempReplacer) >= 0) {
			Matcher ms = Pattern.compile(
					__tempReplacer + "[\\.:]" + __tempReplacer+".(_[\\w_]+)(?:;|,)").matcher(value);
			int pos = 0;
			StringBuilder buf = new StringBuilder();
			while (ms.find()) {
				buf.append(value.substring(pos, ms.start()));
				String text = ms.group(1);
				text = decode(text);
				StringBuffer perfix = null;
				if (includeNCC) {
					perfix = new StringBuffer();
					int lstart = value.lastIndexOf('\n', ms.start()) + 1;
					int lend = lstart;
					While: while (true) {
						switch (value.charAt(lend)) {
						case '\t':
						case ' ':
							perfix.append(' ');
							lend++;
							break;
						default:
							break While;
						}
					}
					if (lend == ms.start()) {
						buf.setLength(buf.length() - perfix.length());
					}
					if (buf.length() > 0) {
						switch (buf.charAt(buf.length() - 1)) {
						case ',':
						case '{':
							buf.append("\r\n");
						}
					}
				}
				if (text.charAt(1) == '/') {
					if (perfix != null) {
						buf.append(perfix);
					}
					buf.append(text);
				} else {
					String[] lines = text.split("\r\n|\n\r|\n|\r");
					for (int i = 0;;) {
						String v = lines[i].trim();
						if (perfix != null) {
							buf.append(perfix);
							if (i == 0) {
								perfix.append(' ');
							}
						}
						buf.append(v);
						i++;
						if (i < lines.length) {
							buf.append('\n');
						} else {
							break;
						}
					}
				}
				if (includeNCC || text.charAt(1) == '/') {
					if (ms.end() < value.length()) {
						// DebugTool.print("$" + value.charAt(ms.end()));
						if ("\r\n".indexOf(value.charAt(ms.end())) < 0) {
							buf.append("\n");
						}
					}
				}
				pos = ms.end();
			}
			buf.append(value.substring(pos));
			//DebugTool.println("over");
			return buf.toString();
		}
		return value;
	}
}
