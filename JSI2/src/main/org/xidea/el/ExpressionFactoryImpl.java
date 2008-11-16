package org.xidea.el;

public class ExpressionFactoryImpl implements ExpressionFactory {
	private static ExpressionFactoryImpl expressionFactory = new ExpressionFactoryImpl();

	public static ExpressionFactory getInstance() {
		return expressionFactory;
	}

	public Expression createEL(String el) {
		return new ExpressionImpl(el);
	}

	public String optimizeEL(String expression) {
		//check it
		expression = expression.trim();
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < expression.length(); i++) {
			char c = expression.charAt(i);
			switch (c) {
			case '\'':// 39//100111//
			case '"': // 34//100010//
				sub: for (i++; i < expression.length(); i++) {
					char c2 = expression.charAt(i);
					switch (c2) {
					case '\\':
						i++;
						break;
					case '\'':// 39//100111//
					case '"': // 34//100010//
						if (c2 == c) {
							c = 0;
							break sub;
						}
						break;
					case '\r':
						break sub;// error
					case '\n':// \\r\n
						if (expression.charAt(i - 1) != '\r') {
							break sub;// error
						}
					}
				}
				if (c == 0) {
					break;
				} else {
					throw new ExpressionSyntaxException("unclosed string at"
							+ i + ":" + expression);
				}
			case '{':// 123//1111011
			case '[':// 91 //1011011
			case '(':// 40 // 101000
				buf.append(c);
				break;
			case ')':// 41 // 101001
			case ']':// 93 //1011101
			case '}':// 125//1111101
				int offset = c - buf.charAt(buf.length() - 1);
				if (offset > 0 && offset < 3) {//[1,2]
					buf.deleteCharAt(buf.length() - 1);
				} else {
					throw new ExpressionSyntaxException("expression error at"
							+ i + ":" + expression);
				}
			}
		}
		return expression;
	}
}
