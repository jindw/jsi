package org.xidea.el.parser;

public class OperatorToken implements ExpressionToken {
	private static Object[] OP_LIST = {
	OP_ADD, "+", OP_SUB, "-", OP_MUL, "*", OP_DIV, "/",
			OP_MOD,
			"%",// +-*/%
			OP_LT, "<", OP_GT, ">", OP_LTEQ, "<=", OP_GTEQ, ">=",
			OP_EQ,
			"==",// relative
			OP_NOTEQ, "!=", OP_NOT, "!", OP_AND, "&&",
			OP_OR,
			"||",// boolean
			OP_QUESTION, "?",
			OP_QUESTION_SELECT,
			":",// 3op
			OP_POS, "+",
			OP_NEG,
			"-",// +-
			BRACKET_BEGIN, "(",
			BRACKET_END,
			")", // group
			VALUE_NEW_LIST, "[", VALUE_NEW_MAP, "{", OP_MAP_PUSH, ":",
			OP_PARAM_JOIN,
			",",// map list,
			OP_GET_PROP,
			".",// prop
			OP_GET_METHOD, "#.", OP_INVOKE_METHOD,
			"#()" // , OP_GET_GLOBAL_METHOD, "#"//method call

	};

	public static int findType(String op) {
		for (int i = 1; i < OP_LIST.length; i += 2) {
			if (op.equals(OP_LIST[i])) {
				return ((Integer)OP_LIST[i-1]).intValue();
			}
		}
		return -1;
	}

	private int type;
	private int length = 2;
	private Object param;

	OperatorToken(int type, Object param) {
		this.type = type;
		this.param = param;
		switch (type) {
		case OP_NOT:
		case OP_POS:
		case OP_NEG:
		//case OP_GET_GLOBAL_METHOD:
			this.length = 1;
		}
	}

	public int getType() {
		return type;
	}

	public int getLength() {
		return length;
	}

	public Object getParam() {
		return param;
	}

	public int getPriority() {
		switch (type) {
		case BRACKET_BEGIN:
		case BRACKET_END:
			return Integer.MIN_VALUE;
		case OP_GET_PROP:
		case OP_GET_METHOD:
		//case OP_GET_GLOBAL_METHOD:
		case OP_INVOKE_METHOD:
		case VALUE_NEW_LIST:
		case VALUE_NEW_MAP:
			return 12;

		case OP_NOT:
		case OP_POS:
		case OP_NEG:
			return 8;

		case OP_MUL:
		case OP_DIV:
		case OP_MOD:
			return 4;

		case OP_ADD:
		case OP_SUB:
			return 1;

		case OP_LT:
		case OP_GT:
		case OP_LTEQ:
		case OP_GTEQ:
		case OP_EQ:
		case OP_NOTEQ:
			return 0;

		case OP_AND:
			return -1;
		case OP_OR:
			return -2;

		case OP_QUESTION:
		case OP_QUESTION_SELECT:
			return -4;// !!

		case OP_MAP_PUSH:
			return -7;// !!
		case OP_PARAM_JOIN:
			return -8;
		}

		throw new RuntimeException("unsupport token:" + type);
	}

	public String toString() {
		for (int i = 0; i < OP_LIST.length; i += 2) {
			if (type == ((Integer) OP_LIST[i]).intValue()) {
				String text = (String) OP_LIST[i + 1];
				if (text.charAt(0) == '#') {
					text += this.getParam();
				}
				return text;
			}
		}
		return "?" + type;
	}

}
