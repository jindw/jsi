package org.xidea.el.parser;

import java.util.HashMap;
import java.util.Map;

public class OperatorToken implements ExpressionToken {
	private int type;
	private int length = 2;
	private Object param;

	OperatorToken(int type,Object param) {
		this.type = type;
		this.param = param;
		switch(type){
		case OP_NOT:
		case OP_POS:
		case OP_NEG:
		case OP_GET_STATIC_METHOD:
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
	public String toString(){
		String op = ExpressionTokenizer.getOperator(type);
		if(op == null){
			switch(type){
			case ExpressionToken.OP_GET_METHOD:
				return "#getMethod";
			case ExpressionToken.OP_GET_STATIC_METHOD:
				return "#getStaticMethod";
			case ExpressionToken.OP_INVOKE_METHOD:
				return "#invoke";
			}
		}
		return op;
	}

}
