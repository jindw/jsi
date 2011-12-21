package org.xidea.jsi.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;



public class IDGenerator {
	private int state = 10;
	private String perfix;
	private String postfix;

	public IDGenerator(String perfix,int state,String postfix) {
		if(!"".equals(perfix)){
			this.perfix = perfix;
		}
		this.state = state;
		if(!"".equals(postfix)){
			this.postfix = postfix;
		}
	}

	public IDGenerator() {
	}

	public final static String BASE;

	public final static Set<String> KEYWORD_SET;

	private final static int BASE_LENGTH;

	static {
		String[] keywords = { "abstract", "boolean", "break", "byte", "case",
				"catch", "char", "class", "const", "continue", "debugger",
				"default", "delete", "do", "double", "else", "enum", "export",
				"extends", "false", "final", "finally", "float", "for",
				"function", "goto", "if", "implements", "import", "in",
				"instanceof", "int", "interface", "long", "native", "new",
				"null", "package", "private", "protected", "prototype",
				"public", "return", "short", "static", "super", "switch",
				"synchronized", "this", "throw", "throws", "transient", "true",
				"try", "typeof", "var", "void", "volatile", "while", "with" };
		KEYWORD_SET = Collections.unmodifiableSet(new HashSet<String>(Arrays
				.asList(keywords)));
		StringBuilder b = new StringBuilder("0123456789_");

		for (char c = 'A'; c <= 'Z'; c++) {
			b.append(c);
		}
		for (char c = 'a'; c <= 'z'; c++) {
			b.append(c);
		}

		BASE = b.toString();
		BASE_LENGTH = BASE.length();
	}

	public String newId() {
		String result = null;
		{
			if (state < BASE_LENGTH) {
				if (state < 10 && perfix == null){
					state =10;
				}
				result =  BASE.substring(state++, state);
				result = perfix == null?result:perfix+result;
				result = postfix == null?result:result + postfix;
			}else{
				StringBuilder buf ;
				int b = state % BASE_LENGTH;
				if(perfix == null){
					if(b<10){
						state += 10-b;
						b = 10;
					}
					buf = new StringBuilder(2);
				}else{
					buf = new StringBuilder(perfix);
				}
				buf.append(BASE.charAt(b));
				int left = state / BASE_LENGTH-1;
				do {
					b = left % BASE_LENGTH;
					buf.append(BASE.charAt(b));
					left = left / BASE_LENGTH-1;
				} while (left >= 0);
				result = buf.toString();
				result = postfix == null?result:result + postfix;
				state++;
			}
		}
		if (KEYWORD_SET.contains(result)) {
			return newId();
		}else{
			return result;
		}
	}
}