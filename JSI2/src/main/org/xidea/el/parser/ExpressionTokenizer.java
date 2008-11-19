package org.xidea.el.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xidea.el.ExpressionSyntaxException;
import org.xidea.el.json.JSONTokenizer;

public class ExpressionTokenizer extends JSONTokenizer {
	private static final int STATUS_BEGIN = -100;
	private static final int STATUS_EXPRESSION = -101;
	private static final int STATUS_OPERATOR = -102;

	private final static ExpressionToken TOKEN_TRUE = createToken(
			ExpressionToken.VALUE_CONSTANTS, Boolean.TRUE);
	private final static ExpressionToken TOKEN_FALSE = createToken(
			ExpressionToken.VALUE_CONSTANTS, Boolean.FALSE);
	private final static ExpressionToken TOKEN_NULL = createToken(
			ExpressionToken.VALUE_CONSTANTS, null);

	protected static ExpressionToken getConstainsToken(String key) {
		if ("true".equals(key)) {
			return TOKEN_TRUE;
		} else if ("false".equals(key)) {
			return TOKEN_FALSE;
		} else if ("null".equals(key)) {
			return TOKEN_NULL;
		}
		return null;
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
			return new OperatorToken(type, null);
		}
	}

	private int status = STATUS_BEGIN;
	private int previousType = STATUS_BEGIN;

	protected ArrayList<ExpressionToken> tokens = new ArrayList<ExpressionToken>();
	protected List<ExpressionToken> expression;
	public ExpressionTokenizer(String value) {
		super(value);
		parseEL();
		this.expression = right(this.tokens.iterator());
	}

	private boolean rightEnd(OperatorToken item, OperatorToken privious) {
		return item.getPriority() <= privious.getPriority();
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
						addRightOperator(rightStack, operator);
					}
				} else {
					while (!buffer.isEmpty() && rightEnd(op, buffer.getFirst())) {
						ExpressionToken operator = buffer.removeFirst();
						// if (operator.getType() !=
						// ExpressionToken.BRACKET_BEGIN){
						addRightOperator(rightStack, operator);
					}
					buffer.addFirst(op);
				}
			} else {// lazy begin value exp
				addRightToken(rightStack, item);
			}
		}
		while (!buffer.isEmpty()) {
			ExpressionToken operator = buffer.removeFirst();
			addRightOperator(rightStack, operator);
		}
		return rightStack.getFirst();
	}

	private void addRightOperator(LinkedList<List<ExpressionToken>> rightStack,
			ExpressionToken operator) {
		switch (operator.getType()) {
		case ExpressionToken.OP_OR:
		case ExpressionToken.OP_AND:
		case ExpressionToken.OP_QUESTION:
		case ExpressionToken.OP_QUESTION_SELECT:
			List<ExpressionToken> children = rightStack.removeFirst();
			List<ExpressionToken> list = rightStack.getFirst();
			LazyToken token = (LazyToken) list.get(list.size() - 1);
			token.setChildren(reverseArray(children));
		}
		addRightToken(rightStack, operator);
	}

	private void addRightToken(LinkedList<List<ExpressionToken>> rightStack,
			ExpressionToken token) {
		List<ExpressionToken> list = rightStack.getFirst();
		if (token instanceof LazyToken) {
			rightStack.addFirst(new ArrayList<ExpressionToken>());
		}
		list.add(token);
	}

	private ExpressionToken[] reverseArray(List<ExpressionToken> list) {
		ExpressionToken[] expression = new ExpressionToken[list.size()];
		int i = expression.length - 1;
		for (ExpressionToken expressionToken : list) {
			expression[i--] = expressionToken;
		}
		return expression;
	}

	public ExpressionToken[] toReversedArray() {
		return reverseArray(expression);//reversed
	}

	protected void parseEL() {
		skipSpace(0);
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
				ExpressionToken constains = getConstainsToken(id);
				if (constains == null) {
					skipSpace(0);
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
			skipSpace(0);
		}
	}
	private String findOperator() {// optimize json ,:[{}]
		switch (value.charAt(start)) {
		case '!'://!,!=
		case '>'://>,>=
		case '<'://<,<=
			if(value.charAt(start+1)=='='){
				return value.substring(start,start+=2);
			}
		case ','://optimize for json
		case ':'://3op,map key
		case '['://list
		case ']':
		case '{'://map
		case '}':
		case '('://quote
		case ')':
		case '.'://prop
		case '?'://3op
		case '+'://5op
		case '-':
		case '*':
		case '/':
		case '%':
			return value.substring(start,start+=1);

		case '='://==
		case '&'://&&
		case '|'://||
			assert (value.charAt(start) == value.charAt(start+1));
			return value.substring(start,start+=2);
		}
		return null;
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
				if (type == ExpressionToken.OP_MAP_PUSH
						|| type == ExpressionToken.VALUE_NEW_MAP) {// (
																	// <#newMap>
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
				addToken(createToken(OperatorToken.findType(op), null));
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
			addToken(createToken(OperatorToken.findType(op), null));
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
			int depth = 0;
			index--;
			while (index >= 0) {
				int type = token.getType();
				if (type == ExpressionToken.BRACKET_BEGIN) {
					depth--;
					if (depth == 0) {
						if (index-1 > 0
								&& tokens.get(index-1).getType() == ExpressionToken.OP_GET_PROP) {
							//TODO:....
							tokens.set(index-1, createToken(ExpressionToken.OP_GET_METHOD, null));
							return false;
						} else {
//							tokens.add(index, createToken(ExpressionToken.OP_GET_GLOBAL_METHOD,
//									null));
							return true;
						}
					}
				} else if (type == ExpressionToken.BRACKET_END) {
					depth++;
				}
				index--;
				token = tokens.get(index);
			}
		} else if (token instanceof VarToken) {//gloabl call
//			tokens.set(index,createToken(ExpressionToken.VALUE_CONSTANTS, ((VarToken)token).getValue()));
//			tokens.add(index, createToken(ExpressionToken.OP_GET_GLOBAL_METHOD, null));
			return true;
		} else if (token instanceof ValueToken) {//member call
			tokens.set(index-1, createToken(ExpressionToken.OP_GET_METHOD, null));
			return false;
		}
		throw new ExpressionSyntaxException("无效方法调用语法");
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
}
