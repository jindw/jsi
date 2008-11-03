package org.xidea.template;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.xidea.el.Expression;

public class TextParser implements Parser {
	private static final Pattern FN_PATTERN = Pattern.compile("^[\\w]+\\s*$");

	private static ExpressionFactory defaultExpressionFactory = new ExpressionFactoryImpl();

	private ExpressionFactory expressionFactory = defaultExpressionFactory;

	public void setExpressionFactory(ExpressionFactory expressionFactory) {
		this.expressionFactory = expressionFactory;
	}

	public List<Object> parse(Object text) {
		return parseText((String) text, false, false, (char) 0);
	}

	public String encodeText(String text, int quteChar) {
		StringWriter out = new StringWriter();
		for (int i = 0; i < text.length(); i++) {
			int c = text.charAt(i);
			switch (c) {
			case '<':
				out.write("&lt;");
				break;
			case '>':
				out.write("&gt;");
				break;
			case '&':
				out.write("&amp;");
				break;
			case '\'':
			case '"':
				if (quteChar == c) {
					out.write("&#39;");
					break;
				} else if (quteChar == c) {
					out.write("&#34;");
					break;
				}
			default:
				out.write(c);
			}
		}
		return out.toString();
	}

	/**
	 * 解析指定文本
	 * 
	 * @public
	 * @abstract
	 * @return <Array> result
	 */
	public List<Object> parseText(String text, boolean encodeXML,
			boolean encodeAttr, int quteChar) {
		int i = 0;
		int start = 0;
		int length = text.length();
		ArrayList<Object> result = new ArrayList<Object>();
		do {
			final int p$ = text.indexOf('$', start);
			if (isEscaped$(text, p$)) {
				continue;
			} else {
				final int p1 = text.indexOf('{', p$);
				if (p1 > p$) {
					String fn;
					if (p$ + 1 < p1) {
						fn = text.substring(p$ + 1, p1);
						if (!FN_PATTERN.matcher(fn).find()) {
							continue;
						}
					} else {
						fn = "";
					}
					int p2 = findELEnd(text, p1, length);
					if (p2 > 0) {
						if (p1 > start) {
							Object el = parseEL(text.substring(p1 + 1, p2));
							try {
								if (start < p$) {
									result.add(text.substring(start, p$));
								}
								if (encodeAttr) {
									result.add(new Object[] {
											Template.ATTRIBUTE_TYPE, el });
								} else {
									result
											.add(new Object[] {
													encodeXML ? Template.EL_TYPE_XML_TEXT
															: Template.EL_TYPE,
													el });

								}
								start = p2 + 1;
							} catch (Exception e) {
							}
						}
					}
				}
			}

		} while (++i < length);
		if (start < length) {
			result.add(text.substring(start));
		}
		if (encodeXML || encodeAttr) {
			i = result.size();
			while (i-- > 0) {
				Object item = result.get(i);
				if (item instanceof String) {
					if(((String)item).length()==0){
						result.remove(i);
					}else{
						result.set(i, encodeText((String) item, quteChar));
					}
				}
			}
		}
		return result;
	}

	protected Expression parseEL(String expression) {
		return expressionFactory.createExpression(expression.trim());
	}

	private boolean isEscaped$(String text, int p) {
		int pre = p;
		while (pre-- > 0 && text.charAt(p) == '\\')
			;
		return (p - pre & 1) == 0;
	}

	private int findELEnd(String text, int p, int length) {
		if (p >= length) {
			return -1;
		}
		int next = p;
		char stringChar = 0;
		int depth = 0;
		do {
			char c = text.charAt(next);
			switch (c) {
			case '\\':
				next++;
				break;
			case '\'':
			case '"':
				if (stringChar == c) {
					stringChar = 0;
				} else if (stringChar == 0) {
					stringChar = c;
				}
				break;
			case '{':
				if (stringChar == 0) {
					depth++;
				}
				break;
			case '}':
				if (stringChar == 0) {
					depth--;
					if (depth == 0) {
						return next;
					}
				}
			}
		} while (++next < length);
		return -1;
	}

}
