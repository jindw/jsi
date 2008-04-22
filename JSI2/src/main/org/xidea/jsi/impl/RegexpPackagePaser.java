package org.xidea.jsi.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.UnsupportedSyntaxException;


public class RegexpPackagePaser implements PackageParser {

	private static final String COMMENT = "/\\*[\\s\\S]*?\\*/|//.*";
	private static final String THIS_INVOKE = "this\\s*\\.\\s*(\\w+)\\(([^\\(\\)]*)\\)";
	private static final String OTHER = "(\\w+)?";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.PackageParser#parse(java.lang.String,
	 *      org.xidea.jsi.JSIPackage)
	 */
	public void parse(String source, JSIPackage pkg) {
		Pattern regexp = Pattern.compile(COMMENT + '|' + THIS_INVOKE + '|'
				+ OTHER);
		Matcher matcher = regexp.matcher(source);
		List<List<Object>> addScriptCall = new ArrayList<List<Object>>();
		List<List<Object>> addDependenceCall = new ArrayList<List<Object>>();
		String implementation = null;
		Map<String, List<List<Object>>> callMap = new HashMap<String, List<List<Object>>>();
		while (matcher.find()) {
			String method = matcher.group(1);
			String content = matcher.group(2);
			String other = matcher.group(3);
			if (method == null) {
				if (other != null && other.trim().length() > 0) {
					throw new UnsupportedSyntaxException("正则解析器不支持复杂定义"
							+ matcher.group(0) + "#" + other);
				}
			} else {
				List<Object> arguments = this.parseArguments(content);
				if (ADD_SCRIPT.equals(method)) {
					switch (arguments.size()) {
					case 1:
						arguments.add(null);
					case 2:
						arguments.add(null);
					case 3:
						arguments.add(null);
					case 4:
						break;
					default:
						throw new RuntimeException("无效参数");
					}
					addScriptCall.add(arguments);
				} else if (ADD_DEPENDENCE.equals(method)) {
					switch (arguments.size()) {
					case 2:
						arguments.add(Boolean.FALSE);
					case 3:
						break;
					default:
						throw new RuntimeException("无效参数");
					}
					addDependenceCall.add(arguments);
				} else if (SET_IMPLEMENTATION.equals(method)) {
					if (arguments.size() != 1) {
						throw new RuntimeException("无效参数");
					}
					if (implementation == null) {
						implementation = (String) arguments.get(0);
					} else {
						throw new RuntimeException("不能多次设置实现包");
					}
				} else {
					throw new RuntimeException("正则解析器不支持复杂定义;method = "
							+ method);
				}
			}
		}
		for (Iterator<List<Object>> it = addScriptCall.iterator(); it.hasNext();) {
			List<Object> item = it.next();
			((DefaultJSIPackage)pkg).addScript((String) item.get(0), item.get(1), item.get(2), item
					.get(3));
		}
		for (Iterator<List<Object>> it = addDependenceCall.iterator(); it
				.hasNext();) {
			List<Object> item = it.next();
			((DefaultJSIPackage)pkg).addDependence((String) item.get(0), item.get(1), (Boolean) item
					.get(2));
		}
	}

	/**
	 * @param content
	 */
	private List<Object> parseArguments(String content) {
		int index = 0;
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(null);
		for (int i = 0, end; i < content.length(); i++) {
			char c = content.charAt(i);
			switch (c) {
			case '\'':
			case '"':
				if (list.get(index) != null) {
					throw new IllegalStateException();
				}
				end = content.indexOf(c, i + 1);
				list.set(index, content.substring(i + 1, end));
				i = end;
				break;
			case '[':
				if (list.get(index) != null) {
					throw new IllegalStateException();
				}
				end = content.indexOf(']', i + 1);
				list.set(index, parseArguments(content.substring(i + 1, end)));
				i = end;
				break;
			case 'n':
				if (list.get(index) != null) {
					throw new IllegalStateException();
				}
				if (content.indexOf("null", i) == i) {
					list.set(index, null);
					i += 3;
				} else {
					throw new IllegalStateException();
				}
				break;
			case 't':
				if (list.get(index) != null) {
					throw new IllegalStateException();
				}
				if (content.indexOf("true", i) == i) {
					list.set(index, Boolean.TRUE);
					i += 3;
				} else {
					throw new IllegalStateException();
				}
				break;
			case '1':// shorter for true
				if (list.get(index) != null) {
					throw new IllegalStateException();
				}
				list.set(index, Boolean.TRUE);
				break;
			case 'f':
				if (list.get(index) != null) {
					throw new IllegalStateException();
				}
				if (content.indexOf("false", i) == i) {
					list.set(index, Boolean.FALSE);
					i += 4;
				} else {
					throw new IllegalStateException();
				}
				break;
			case '0': // shorter for false
				if (list.get(index) != null) {
					throw new IllegalStateException();
				}
				list.set(index, Boolean.TRUE);
				break;
			case ',':
				index++;
				list.add(null);
				break;
			default:
				if (" \r\n".indexOf(c) < 0) {
					throw new IllegalStateException();
				}
				break;
			}
		}
		return list;

	}
}
