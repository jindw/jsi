package org.xidea.template;

import java.util.ArrayList;

public class PropertyExpressionFactory implements ExpressionFactory {
	public Expression createExpression(String value) {
		try {
			value = value.trim();
			int end = value.length();
			int start = skipSpace(value, 0, end);
			char c = value.charAt(start);
			if (c >= '0' && c <= '9') {
				Number numnber;
				if(value.indexOf('.')>=0){
					numnber = Double.parseDouble(value);
				}else{
					if(value.startsWith("0x")){
						numnber = Long.parseLong(value.substring(2),16); 
					}else if(value.startsWith("0")){//
						numnber = Long.parseLong(value.substring(1),8); 
					}else{
						numnber = Long.parseLong(value); 
					}
				}
				return new ConstantExpression(numnber);
				
			} else {
				ArrayList<Object> keys = new ArrayList<Object>();
				parse(value, 0, keys);
				Object[] arrays = keys.toArray();
				String first = (String) arrays[0];
				if ("true".equals(first)) {
					return new ConstantExpression(Boolean.TRUE);
				} else if ("false".equals(first)) {
					return new ConstantExpression(Boolean.FALSE);
				} else if ("null".equals(first)) {
					return new ConstantExpression(null);
				}
				return new PropertyExpression(arrays);
			}
		} catch (Exception e) {
			return null;
		}
	}

	private void parse(String value, int start, ArrayList<Object> keys) {
		int end = value.length();
		boolean findID = true;
		while (true) {
			start = skipSpace(value, start, end);
			if (findID) {
				start = findId(value, start, end, keys);
			} else {
				char c = value.charAt(start);
				if (c == '"' || c == '\'') {
					start = findString(value, start, end, keys);
				} else if (c >= '0' && c <= '9') {
					start = findInteger(value, start, end, keys);
				} else {
					throw new RuntimeException();
				}
				start = skipSpace(value, start, end);
				c = value.charAt(start++);
				if (c != ']') {
					throw new RuntimeException();
				}
			}
			start = skipSpace(value, start, end);
			if (start < end) {
				char c = value.charAt(start);
				if (c == '[') {
					findID = false;
				} else if (c == '.') {
					findID = true;
				} else {
					throw new RuntimeException();
				}
			} else {
				return;
			}
			start++;
		}
	}

	private int findInteger(String value, int start, int end,
			ArrayList<Object> keys) {
		int i = start;
		char c = value.charAt(i++);
		if (c == '0') {
			if (i < end) {
				c = value.charAt(i++);
				if (c == 'x') {
					start = i;
					while (i < end) {
						c = value.charAt(i++);
						if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A'
								&& c <= 'F')) {
							break;
						}
					}
					keys.add(Integer.parseInt(value.substring(start, i), 16));
				} else if (c >= '0' && c <= '9') {
					start = i - 1;
					while (i < end) {
						c = value.charAt(i++);
						if (c < '0' || c > '7') {
							break;
						}
					}
					keys.add(Integer.parseInt(value.substring(start, i), 8));
				} else {
					keys.add(Integer.valueOf(0));
				}
			} else {
				keys.add(Integer.valueOf(0));
			}
			return i;
		} else {
			while (i < end) {
				c = value.charAt(i++);
				if (c < '0' || c > '9' || c == 'x') {
					break;
				}
			}
			keys.add(Integer.valueOf(value.substring(start, i)));
			return i;
		}
	}

	private int findId(String value, int start, int end, ArrayList<Object> keys) {
		int p = start;
		if (Character.isJavaIdentifierPart(value.charAt(p++))) {
			while (p < end) {
				if (!Character.isJavaIdentifierPart(value.charAt(p))) {
					break;
				}
				p++;
			}
			keys.add(value.substring(start, p));
			return end;
		}
		throw new RuntimeException();

	}

	/**
	 * {@link Decompiler#printSourceString
	 */

	private int findString(String value, int start, int end,
			ArrayList<Object> keys) {
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
					keys.add(buf.toString());
					return start;
				}
			default:
				buf.append(c);

			}
		}
		return end;
	}

	private int skipSpace(String value, int start, int end) {
		while (start < end) {
			if (!Character.isWhitespace(value.charAt(start))) {
				return start;
			}
			start++;
		}
		return start;
	}

}