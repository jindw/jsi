package org.xidea.jsi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xidea.jsi.JSIPackage;

public class RegexpPackagePaser extends PackageParser {

	private static final String COMMENT = "/\\*[\\s\\S]*?\\*/|//.*";
	private static final String THIS_INVOKE = "this\\s*\\.\\s*(\\w+)\\(([^\\(\\)]*)\\)";
	private static final String OTHER = "(\\w+)?";
	public RegexpPackagePaser(JSIPackage packageObject){
		parse(packageObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.PackageParser#parse(java.lang.String,
	 *      org.xidea.jsi.JSIPackage)
	 */
	private void parse(JSIPackage packageObject) {
		String source = packageObject.loadText(JSIPackage.PACKAGE_FILE_NAME);
		Pattern regexp = Pattern.compile(COMMENT + '|' + THIS_INVOKE + '|'
				+ OTHER);
		Matcher matcher = regexp.matcher(source);
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
					if("*".equals(arguments.get(1))){
						throw new UnsupportedSyntaxException("正则解析器不支持复杂定义"
								+ matcher.group(0) + "#" + other);
					}
					addScript((String)arguments.get(0),arguments.get(1),arguments.get(2),arguments.get(3));
				} else if (ADD_DEPENDENCE.equals(method)) {
					switch (arguments.size()) {
					case 2:
						arguments.add(Boolean.FALSE);
					case 3:
						break;
					default:
						throw new RuntimeException("无效参数");
					}
					addDependence((String)arguments.get(0),arguments.get(1),(Boolean)arguments.get(2));
				} else if (SET_IMPLEMENTATION.equals(method)) {
					if (arguments.size() != 1) {
						throw new RuntimeException("无效参数");
					}
					this.setImplementation((String)arguments.get(0));
				} else {
					throw new RuntimeException("正则解析器不支持复杂定义;method = "
							+ method);
				}
			}
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
				if (!Character.isWhitespace(c)) {
					throw new IllegalStateException();
				}
				break;
			}
		}
		return list;

	}
}
