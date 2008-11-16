package org.xidea.el.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xidea.el.ExpressionSyntaxException;

public class ExpressionTokenizer extends ExpressionTokenizerBase {

	private ExpressionToken[] reversedArray;

	public ExpressionTokenizer(String value) {
		super(value);
		parse();
		this.expression = right(this.tokens.iterator());
	}

	private boolean rightEnd(OperatorToken item, OperatorToken privious) {
		int type = item.getType();
		int priviousType = privious.getType();
		return PRIORITY_MAP.get(type) <= PRIORITY_MAP.get(priviousType);
	}

	// 将中序表达式转换为右序表达式
	private List<ExpressionToken> right(Iterator<ExpressionToken> tokens) {
		LinkedList<List<ExpressionToken>> rightStack = new LinkedList<List<ExpressionToken>>();
		rightStack.addFirst(new ArrayList<ExpressionToken>()); // 存储右序表达式

		LinkedList<OperatorToken> buffer = new LinkedList<OperatorToken>();

		while (tokens.hasNext()) {
			final ExpressionToken item = tokens.next();
			if (item instanceof OperatorToken) {
				OperatorToken op = (OperatorToken) item;
				if (buffer.isEmpty()) {
					buffer.addFirst(op);
				} else if (item.getType() == ExpressionToken.BRACKET_BEGIN) {// ("(")
					buffer.addFirst(op);
				} else if (item.getType() == ExpressionToken.BRACKET_END) {// .equals(")"))
					while (true) {
						ExpressionToken operator = buffer.removeFirst();
						if (operator.getType() == ExpressionToken.BRACKET_BEGIN) {
							break;
						}
						addOperator(rightStack, operator);
					}
				} else {
					while (!buffer.isEmpty() && rightEnd(op, buffer.getFirst())) {
						ExpressionToken operator = buffer.removeFirst();
						// if (operator.getType() !=
						// ExpressionToken.BRACKET_BEGIN){
						addOperator(rightStack, operator);
					}
					buffer.addFirst(op);
				}
			} else {// lazy begin value exp
				addToken(rightStack, item);
			}
		}
		while (!buffer.isEmpty()) {
			ExpressionToken operator = buffer.removeFirst();
			addOperator(rightStack, operator);
		}
		return rightStack.getFirst();
	}

	private void addOperator(LinkedList<List<ExpressionToken>> rightStack,
			ExpressionToken operator) {
		switch (operator.getType()) {
		case ExpressionToken.OP_OR:
		case ExpressionToken.OP_AND:
		case ExpressionToken.OP_QUESTION:
		case ExpressionToken.OP_QUESTION_SELECT:
			List<ExpressionToken> children = rightStack.removeFirst();
			List<ExpressionToken> list = rightStack.getFirst();
			LazyToken token = (LazyToken) list.get(list.size() - 1);
			token.setChildren(toArray(children));
		}
		addToken(rightStack, operator);
	}

	public void addToken(LinkedList<List<ExpressionToken>> rightStack,
			ExpressionToken token) {
		List<ExpressionToken> list = rightStack.getFirst();
		if (token instanceof LazyToken) {
			rightStack.addFirst(new ArrayList<ExpressionToken>());
		}
		list.add(token);
	}

	private ExpressionToken[] toArray(List<ExpressionToken> list) {
		ExpressionToken[] expression = new ExpressionToken[list.size()];
		int i = expression.length - 1;
		for (ExpressionToken expressionToken : list) {
			expression[i--] = expressionToken;
		}
		return expression;
	}

	public ExpressionToken[] toArray() {
		if (this.reversedArray == null) {
			this.reversedArray = toArray(expression);
		}
		return this.reversedArray;
	}
}

class ExpressionTokenizerBase {
	private static final int STATUS_BEGIN = -100;
	private static final int STATUS_EXPRESSION = -101;
	private static final int STATUS_OPERATOR = -102;
	
	protected static Map<Integer, Integer> PRIORITY_MAP = new HashMap<Integer, Integer>();
	private static Map<String, Integer> OP_TYPE_MAP = new HashMap<String, Integer>();
	private static Map<Integer, String> TYPE_OP_MAP = new HashMap<Integer, String>();
	private static final String[] OPS;// = ">= <= == != && || + - * / % ? : .

	private static void addOperator(int type, int priopity, String key,
			List<String> ops) {
		PRIORITY_MAP.put(type, priopity);
		if (key != null) {
			TYPE_OP_MAP.put(type, key);
			if (OP_TYPE_MAP.containsKey(key)) {
				OP_TYPE_MAP.put(key, -1);
			} else {
				OP_TYPE_MAP.put(key, type);
			}
			if (key.length() == 1) {
				if (",:[{}]".indexOf(key.charAt(0)) == -1) {
					ops.add(key);
				}
			} else if (key.length() == 2) {
				// 当前只有二个字符的操作符，以后需要扩展
				ops.add(0, key);
			}
		}
	}

	static String getOperator(int type) {
		return TYPE_OP_MAP.get(type);
	}

	static {
		ArrayList<String> ops = new ArrayList<String>();
		ops.add("[");// !!
		ops.add("{");
		ops.add("]");
		ops.add("}");

		addOperator(ExpressionToken.BRACKET_BEGIN, Integer.MIN_VALUE, "(", ops);
		addOperator(ExpressionToken.BRACKET_END, Integer.MIN_VALUE, ")", ops);

		addOperator(ExpressionToken.OP_GET_PROP, 12, ".", ops);
		addOperator(ExpressionToken.OP_GET_METHOD, 12, null, ops);
		addOperator(ExpressionToken.OP_GET_STATIC_METHOD, 12, null, ops);
		addOperator(ExpressionToken.OP_INVOKE_METHOD, 12, null, ops);
		addOperator(ExpressionToken.VALUE_NEW_LIST, 12, "[", ops);
		addOperator(ExpressionToken.VALUE_NEW_MAP, 12, "{", ops);

		addOperator(ExpressionToken.OP_NOT, 8, "!", ops);
		addOperator(ExpressionToken.OP_POS, 8, "+", ops);
		addOperator(ExpressionToken.OP_NEG, 8, "-", ops);

		addOperator(ExpressionToken.OP_MUL, 4, "*", ops);
		addOperator(ExpressionToken.OP_DIV, 4, "/", ops);
		addOperator(ExpressionToken.OP_MOD, 4, "%", ops);

		addOperator(ExpressionToken.OP_ADD, 1, "+", ops);
		addOperator(ExpressionToken.OP_SUB, 1, "-", ops);

		addOperator(ExpressionToken.OP_LT, 0, "<", ops);
		addOperator(ExpressionToken.OP_GT, 0, ">", ops);
		addOperator(ExpressionToken.OP_LTEQ, 0, "<=", ops);
		addOperator(ExpressionToken.OP_GTEQ, 0, ">=", ops);
		addOperator(ExpressionToken.OP_EQ, 0, "==", ops);
		addOperator(ExpressionToken.OP_NOTEQ, 0, "!=", ops);

		addOperator(ExpressionToken.OP_AND, -1, "&&", ops);
		addOperator(ExpressionToken.OP_OR, -2, "||", ops);

		addOperator(ExpressionToken.OP_QUESTION, -4, "?", ops);
		addOperator(ExpressionToken.OP_QUESTION_SELECT, -4, ":", ops);// !!

		addOperator(ExpressionToken.OP_MAP_PUSH, -7, ":", ops);// !!
		addOperator(ExpressionToken.OP_PARAM_JOIN, -8, ",", ops);

		OPS = ops.toArray(new String[ops.size()]);
	}
	private static Map<Integer, OperatorToken> operatorMap = new HashMap<Integer, OperatorToken>();
	private static final Map<String, ExpressionToken> CONSTAINS_MAP = new HashMap<String, ExpressionToken>();
	static {
		CONSTAINS_MAP.put("true", createToken(ExpressionToken.VALUE_CONSTANTS,
				Boolean.TRUE));
		CONSTAINS_MAP.put("false", createToken(ExpressionToken.VALUE_CONSTANTS,
				Boolean.FALSE));
		CONSTAINS_MAP.put("null", createToken(ExpressionToken.VALUE_CONSTANTS,
				null));
	}

	protected static ExpressionToken createToken(int type, Object value) {
		switch (type) {
		case ExpressionToken.VALUE_VAR:
			return new VarToken((String) value);
		case ExpressionToken.VALUE_CONSTANTS:
		case ExpressionToken.VALUE_NEW_MAP:
		case ExpressionToken.VALUE_NEW_LIST:
			return new ValueToken(type, value);
		case ExpressionToken.VALUE_LAZY:
			return new LazyToken();
		case ExpressionToken.OP_MAP_PUSH:
			return new OperatorToken(type, value);
		default:
			// public static OperatorToken getToken(final int type) {
			OperatorToken token = operatorMap.get(type);
			if (token == null) {
				synchronized (operatorMap) {
					token = operatorMap.get(type);
					if (token == null) {
						token = new OperatorToken(type, null);
						operatorMap.put(type, token);
					}
				}
			}
			return token;
		}
	}

	private String value;
	private int start;
	private final int end;
	private int status = STATUS_BEGIN;
	private int previousType = STATUS_BEGIN;

	protected ArrayList<ExpressionToken> tokens = new ArrayList<ExpressionToken>();
	protected List<ExpressionToken> expression;


	protected ExpressionTokenizerBase(String value) {
		this.value = value.trim();
		this.end = this.value.length();
	}

	protected void parse() {
		skipSpace();
		while (start < end) {
			char c = value.charAt(start);
			if (c == '"' || c == '\'') {
				String text = findString();
				addKeyOrObject(text, false);
			} else if (c >= '0' && c <= '9') {
				Number number = findNumber();
				addKeyOrObject(number, false);
			} else if (Character.isJavaIdentifierStart(c)) {
				String id = findId();
				ExpressionToken constains = CONSTAINS_MAP.get(id);
				if (constains == null) {
					skipSpace();
					if (previousType == ExpressionToken.OP_GET_PROP) {
						addToken(createToken(ExpressionToken.VALUE_CONSTANTS,
								id));
					} else {
						addKeyOrObject(id, true);
					}
				} else {
					addToken(constains);
				}
			} else {
				String op = findOperator();
				// if (value.startsWith(op, start))
				parseOperator(op);
				if (op == null) {
					throw new ExpressionSyntaxException("语法错误:" + value + "@"
							+ start);
				}
			}
			skipSpace();
		}
	}


	/**
	 * 碰見:和,的時候，就需要檢查是否事map的間隔符號了
	 * 
	 * @return
	 */
	private boolean isMapMethod() {
		int i = tokens.size() - 1;
		int depth = 0;
		for (; i >= 0; i--) {
			ExpressionToken token = tokens.get(i);
			int type = token.getType();
			if (depth == 0) {
				if (type == ExpressionToken.OP_MAP_PUSH || type == ExpressionToken.VALUE_NEW_MAP) {// ( <#newMap>
															// <#push>
					return true;
				} else if (type == ExpressionToken.OP_PARAM_JOIN) {// (
																	// <#newList>
																	// <#param_join>
					return false;
				}
			}
			if (type == ExpressionToken.BRACKET_BEGIN) {
				depth--;
			} else if (type == ExpressionToken.BRACKET_END) {
				depth++;
			}
		}
		return false;
	}

	private String findOperator() {// optimize json ,:[{}]
		switch (value.charAt(start)) {
		case ',':
			start++;
			return ",";
		case ':':
			start++;
			return ":";
		case '[':
			start++;
			return "[";
		case '{':
			start++;
			return "{";
		case ']':
			start++;
			return "]";
		case '}':
			start++;
			return "}";
		}
		for (String op : OPS) {
			if (value.startsWith(op, start)) {
				start += op.length();
				return op;
			}
		}
		return null;
	}
	private void parseOperator(String op) {
		if (op.length() == 1) {
			switch (op.charAt(0)) {
			case '(':
				if (status == STATUS_EXPRESSION) {
					insertAndReturnIsStatic();
					addToken(createToken(ExpressionToken.OP_INVOKE_METHOD, null));
					if (skipSpace(')')) {
						addToken(createToken(ExpressionToken.VALUE_CONSTANTS,
								Collections.EMPTY_LIST));
					} else {
						addList();
					}

				} else {
					addToken(createToken(ExpressionToken.BRACKET_BEGIN, null));
				}
				break;
			case '[':
				if (status == STATUS_BEGIN || status == STATUS_OPERATOR) {// list
					addList();

				} else if (status == STATUS_EXPRESSION) {// getProperty
					addToken(createToken(ExpressionToken.OP_GET_PROP, null));
					addToken(createToken(ExpressionToken.BRACKET_BEGIN, null));
				} else {
					throw new ExpressionSyntaxException("语法错误:" + value + "@"
							+ start);
				}
				break;
			case '{':
				addMap();
				break;
			case '}':
			case ']':
			case ')':
				addToken(createToken(ExpressionToken.BRACKET_END, null));
				break;
			case '+'://
				addToken(createToken(
						status == STATUS_OPERATOR ? ExpressionToken.OP_POS
								: ExpressionToken.OP_ADD, null));
				// addToken(OperatorToken.getToken(ExpressionToken.SKIP_AND));
				break;
			case '-':
				addToken(createToken(
						status == STATUS_OPERATOR ? ExpressionToken.OP_NEG
								: ExpressionToken.OP_SUB, null));
				// addToken(OperatorToken.getToken(ExpressionToken.SKIP_AND));
				break;
			case '?':// ?:
				addToken(createToken(ExpressionToken.OP_QUESTION, null));
				// addToken(OperatorToken.getToken(ExpressionToken.SKIP_QUESTION));
				addToken(new LazyToken());
				break;
			case ':':// :(object_setter is skiped)
				addToken(createToken(ExpressionToken.OP_QUESTION_SELECT, null));
				addToken(new LazyToken());
				break;
			case ',':// :(object_setter is skiped,',' should
				// be skip)
				if (!isMapMethod()) {
					addToken(createToken(ExpressionToken.OP_PARAM_JOIN, null));

				}
				break;
			case '/':
				char next = value.charAt(start);
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
					break;
				} else if (next == '*') {
					int cend = this.value.indexOf("*/", start);
					if (cend > 0) {
						start = cend + 2;
					} else {
						throw new ExpressionSyntaxException("未結束注釋:" + value
								+ "@" + start);
					}
					break;
				}
			default:
				addToken(createToken(OP_TYPE_MAP.get(op), null));
			}
		} else if (op.equals("||")) { // ||
			addToken(createToken(ExpressionToken.OP_OR, null));
			addToken(new LazyToken());
			// addToken(LazyToken.LAZY_TOKEN_END);
		} else if (op.equals("&&")) {// &&
			addToken(createToken(ExpressionToken.OP_AND, null));
			addToken(new LazyToken());
			// addToken(OperatorToken.getToken(ExpressionToken.SKIP_AND));
		} else {
			addToken(createToken(OP_TYPE_MAP.get(op), null));
		}

	}

	private void addToken(ExpressionToken token) {
		switch (token.getType()) {
		case ExpressionToken.BRACKET_BEGIN:
			status = STATUS_BEGIN;
			break;
		case ExpressionToken.VALUE_CONSTANTS:
		case ExpressionToken.VALUE_VAR:
		case ExpressionToken.BRACKET_END:
			status = STATUS_EXPRESSION;
			break;
		default:
			status = STATUS_OPERATOR;
			break;
		}
		// previousType2 = previousType;
		previousType = token.getType();
		tokens.add(token);
	}
	private void addKeyOrObject(Object object, boolean isVar) {
		if (skipSpace(':') && isMapMethod()) {// object key
			addToken(createToken(ExpressionToken.OP_MAP_PUSH, object));
			this.start++;// skip :
		} else if (isVar) {
			addToken(createToken(ExpressionToken.VALUE_VAR, object));
		} else {
			addToken(createToken(ExpressionToken.VALUE_CONSTANTS, object));
		}
	}

	private boolean insertAndReturnIsStatic() {
		int index = tokens.size() - 1;
		ExpressionToken token = tokens.get(index);
		if (token.getType() == ExpressionToken.BRACKET_END) {
			int depth = 1;
			index--;
			while (index > 0) {
				int type = token.getType();
				if (type == ExpressionToken.BRACKET_BEGIN) {
					depth--;
					if (depth == 0) {
						return insertAndReturnIsStatic(index - 1);
					}
				} else if (type == ExpressionToken.BRACKET_END) {
					depth++;
				}
				index--;
			}
		} else if (token instanceof VarToken) {
			return insertAndReturnIsStatic(index - 1);
		} else if (token instanceof ValueToken) {
			return insertAndReturnIsStatic(index - 1);
		}
		throw new ExpressionSyntaxException("无效方法调用语法");
	}

	private boolean insertAndReturnIsStatic(int index) {
		if (index > 0
				&& tokens.get(index).getType() == ExpressionToken.OP_GET_PROP) {
			tokens.set(index, createToken(ExpressionToken.OP_GET_METHOD, null));
			return false;
		} else {
			tokens.add(index, createToken(ExpressionToken.OP_GET_STATIC_METHOD,
					null));
			return true;
		}
	}

	private void addList() {
		addToken(createToken(ExpressionToken.BRACKET_BEGIN, null));
		addToken(createToken(ExpressionToken.VALUE_NEW_LIST, null));
		if (!skipSpace(']')) {
			addToken(createToken(ExpressionToken.OP_PARAM_JOIN, null));
		}
	}

	private void addMap() {
		addToken(createToken(ExpressionToken.BRACKET_BEGIN, null));
		addToken(createToken(ExpressionToken.VALUE_NEW_MAP, null));
	}

	private Number findNumber() {
		int i = start;
		char c = value.charAt(i++);
		if (c == '0' && i < end) {
			c = value.charAt(i++);
			if (c == 'x') {
				while (i < end) {
					c = value.charAt(i++);
					if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A'
							&& c <= 'F')) {
						i--;
						break;
					}
				}
				return parseNumber(value.substring(start, start = i));
			} else if (c >= '0' && c <= '9') {
				while (i < end) {
					c = value.charAt(i++);
					if (c < '0' || c > '7') {
						i--;
						break;
					}
				}
				return parseNumber(value.substring(start, start = i));

			} else {
				i--;// next process
			}
		}
		while (i < end) {
			c = value.charAt(i++);
			if (c < '0' || c > '9') {
				i--;
				break;
			}
		}
		if (c == '.') {
			i++;
			while (i < end) {
				c = value.charAt(i++);
				if (c < '0' || c > '9') {
					i--;
					break;
				}
			}
		}
		return parseNumber(value.substring(start, start = i));
	}

	private Number parseNumber(String text) {
		if (text.startsWith("0x")) {
			return Integer.parseInt(text.substring(2), 16);
		} else if (text.indexOf('.') >= 0) {
			return Double.parseDouble(text);
		} else if (text.charAt(0) == '0' && text.length() > 1) {
			return Integer.parseInt(text.substring(1), 8);
		} else {
			return Integer.parseInt(text);
		}
	}

	private String findId() {
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
		throw new ExpressionSyntaxException();

	}

	/**
	 * {@link Decompiler#printSourceString
	 */

	private String findString() {
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

	private boolean skipSpace(int... nextChars) {
		while (start < end) {
			if (!Character.isWhitespace(value.charAt(start))) {
				break;
			}
			start++;
		}
		if (nextChars.length > 0 && start < end) {
			int next = value.charAt(start);
			for (int i : nextChars) {
				if (i == next) {
					return true;
				}
			}
		}
		return false;
	}
}
