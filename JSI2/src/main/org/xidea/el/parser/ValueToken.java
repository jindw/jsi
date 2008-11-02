package org.xidea.el.parser;

import java.util.ArrayList;
import java.util.HashMap;

public class ValueToken implements ExpressionToken {
	private Object value;
	private int type;

	ValueToken(int type, Object value) {
		this.type = type;
		this.value = value;
	}

	public int getType() {
		return this.type;
	}

	public Object getValue() {
		switch (this.type) {
		case ExpressionToken.VALUE_NEW_LIST:
			return new ArrayList<Object>();
		case ExpressionToken.VALUE_NEW_MAP:
			return new HashMap<Object, Object>();
		default:
			return value;
		}
	}

	public String toString() {
		return String.valueOf(this.value);
	}

}
