package org.xidea.jsel;

import java.util.HashMap;
import java.util.Map;

public class OperatorToken implements ExpressionToken {
	private int type;
	private int length;
	private Object param;
	private static Map<Integer, OperatorToken> operatorMap = new HashMap<Integer, OperatorToken>();
	private static Map<Integer, Integer> lengthMap = new HashMap<Integer, Integer>();

	static {
		lengthMap.put(TYPE_NOT, 1);
		lengthMap.put(TYPE_POS, 1);
		lengthMap.put(TYPE_GET_STATIC_METHOD, 1);
		lengthMap.put(TYPE_NEW_LIST, 0);
		lengthMap.put(TYPE_NEW_MAP, 0);
	}

	public static ExpressionToken createToken(int type,
			Object param) {
		return new OperatorToken(type,param);
	}
	public static OperatorToken getToken(final int type) {
		OperatorToken token = operatorMap.get(type);
		if (token == null) {
			synchronized (operatorMap) {
				token = operatorMap.get(type);
				if (token == null) {
					token = new OperatorToken(type,null);
					operatorMap.put(type, token);
				}
			}
		}
		return token;
	}

	private OperatorToken(int type,Object param) {
		this.type = type;
		this.param = param;
		Integer length = lengthMap.get(type);
		this.length = length == null ? 2 : length;
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

	public void setLength(int length) {
		this.length = length;
	}
	public String toString(){
		String op = ExpressionTokenizer.getOperator(type);
		if(op == null){
			switch(type){
			case ExpressionToken.TYPE_GET_METHOD:
				return "#getMethod";
			case ExpressionToken.TYPE_GET_STATIC_METHOD:
				return "#getStaticMethod";
			case ExpressionToken.TYPE_INVOKE_METHOD:
				return "#invoke";
			case ExpressionToken.TYPE_INVOKE_STATIC_METHOD:
				return "#staticInvoke";
			}
		}
		return op;
	}

}
