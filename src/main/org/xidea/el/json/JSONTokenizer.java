package org.xidea.el.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xidea.el.ExpressionSyntaxException;

public class JSONTokenizer {

	protected String value;
	protected int start;
	protected final int end;

	public JSONTokenizer(String value) {
		this.value = value.trim();
		this.end = this.value.length();
	}

	public Object parse() {
		skipComment();
		char c = value.charAt(start);
		if (c == '"') {
			return findString();
		} else if (c == '-' || c >= '0' && c <= '9') {
			return findNumber();
		} else if (c == '[') {
			return findList();
		} else if (c == '{') {
			return findMap();
		} else {
			String key = findId();
			if ("true".equals(key)) {
				return Boolean.TRUE;
			} else if ("false".equals(key)) {
				return Boolean.FALSE;
			} else if ("null".equals(key)) {
				return null;
			} else {
				throw new ExpressionSyntaxException("语法错误:" + value + "@"
						+ start);
			}
		}
	}

	private Map<String, Object> findMap() {
		start++;
		skipComment();
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
		while (true) {
			// result.add(parse());
			String key = (String) parse();
			skipComment();
			char c = value.charAt(start++);
			if (c != ':') {
				throw new ExpressionSyntaxException("错误对象语法:" + value + "@"
						+ start);
			}
			Object valueObject = parse();
			skipComment();
			c = value.charAt(start++);
			if (c == '}') {
				result.put(key, valueObject);
				return result;
			} else if (c != ',') {
				throw new ExpressionSyntaxException("错误对象语法:" + value + "@"
						+ start);
			} else {
				result.put(key, valueObject);

			}
		}
	}

	private List<Object> findList() {
		ArrayList<Object> result = new ArrayList<Object>();
		// start--;
		start++;
		skipComment();
		if (value.charAt(start) == ']') {
			start++;
			return result;
		} else {
			result.add(parse());
		}
		while (true) {
			skipComment();
			char c = value.charAt(start++);
			if (c == ']') {
				return result;
			} else if (c == ',') {
				skipComment();
				result.add(parse());
			} else {
				throw new ExpressionSyntaxException("错误数组语法:" + value + "@"
						+ start);
			}
		}
	}

	protected Number findNumber() {
		int i = start;// skip -;
		char c = value.charAt(i++);
		while (i < end) {
			c = value.charAt(i);
			if (c == '.' || c >= '0' && c <= '9') {
			} else if (c == 'E' || c == 'e') {// E+-
				i++;
			} else {
				break;
			}
			i++;
		}
		if (i < end) {
			if (value.charAt(i) == 'x') {
				boolean isNegavite = value.charAt(Math.max(i - 2, 0)) == '-';
				start = ++i;
				while (i < end) {
					c = value.charAt(i);
					if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
							&& c <= 'f')) {
						break;
					}
					i++;
				}
				String text = value.substring(start, start = i);
				return parseNumber(isNegavite ? '-' + text : text, 16);
			}
		}
		return parseNumber(value.substring(start, start = i));
	}

	protected Number parseNumber(String text) {
		if (text.indexOf("x") > 0) {
			text = text.substring(2);
			return parseNumber(text, 16);
		} else if (text.indexOf('.') >= 0 || text.indexOf('e') > 0
				|| text.indexOf('E') > 0) {
			return Double.parseDouble(text);
		} else if (text.charAt(0) == '0' && text.length() > 1) {
			return parseNumber(text.substring(1), 8);
		} else {
			return parseNumber(text, 10);
		}
	}

	protected Number parseNumber(String text, int radix) {
		try {
			return Integer.parseInt(text, radix);
		} catch (Exception e) {
			return Long.parseLong(text, radix);
		}
	}

	protected String findId() {
		int p = start;
		if (Character.isJavaIdentifierPart(value.charAt(p++))) {
			while (p < end) {
				if (!Character.isJavaIdentifierPart(value.charAt(p))) {
					break;
				}
				p++;
			}
			return (value.substring(start, start = p));
		}
		throw new ExpressionSyntaxException("无效id");

	}

	/**
	 * {@link Decompiler#printSourceString
	 */
	protected String findString() {
		char quoteChar = value.charAt(start++);
		StringBuilder buf = new StringBuilder();
		while (start < end) {
			char c = value.charAt(start++);
			switch (c) {
			case '\\':
				char c2 = value.charAt(start++);
				switch (c2) {
				case 'b':
					buf.append('\b');
					break;
				case 'f':
					buf.append('\f');
					break;
				case 'n':
					buf.append('\n');
					break;
				case 'r':
					buf.append('\r');
					break;
				case 't':
					buf.append('\t');
					break;
				case 'v':
					buf.append(0xb);
					break; // Java lacks \v.
				case ' ':
					buf.append(' ');
					break;
				case '\\':
					buf.append('\\');
					break;
				case '\'':
					buf.append('\'');
					break;
				case '\"':
					buf.append('"');
					break;
				case 'u':
					buf.append((char) Integer.parseInt(value.substring(
							start + 1, start + 5), 16));
					start += 4;
					break;
				case 'x':
					buf.append((char) Integer.parseInt(value.substring(
							start + 1, start + 3), 16));
					start += 2;
					break;
				}
				break;
			case '"':
			case '\'':
				if (c == quoteChar) {
					return (buf.toString());
				}
			default:
				buf.append(c);

			}
		}
		return null;
	}

	protected void skipComment() {
		while (true) {
			while (start < end) {
				if (!Character.isWhitespace(value.charAt(start))) {
					break;
				}
				start++;
			}
			if (start < end && value.charAt(start) == '/') {
				start++;
				char next = value.charAt(start++);
				if (next == '/') {
					int end1 = this.value.indexOf('\n', start);
					int end2 = this.value.indexOf('\r', start);
					int cend = Math.min(end1, end2);
					if (cend < 0) {
						cend = Math.max(end1, end2);
					}
					if (cend > 0) {
						start = cend;
					} else {
						start = this.end;
					}
				} else if (next == '*') {
					int cend = this.value.indexOf("*/", start);
					if (cend > 0) {
						start = cend + 2;
					} else {
						throw new ExpressionSyntaxException("未結束注釋:" + value
								+ "@" + start);
					}
				}
			} else {
				break;
			}
		}
	}

	protected boolean skipSpace(int nextChar) {
		while (start < end) {
			if (!Character.isWhitespace(value.charAt(start))) {
				break;
			}
			start++;
		}
		if (nextChar > 0 && start < end) {
			int next = value.charAt(start);
			if (nextChar == next) {
				return true;
			}
		}
		return false;
	}
}
