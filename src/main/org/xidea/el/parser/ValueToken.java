package org.xidea.el.parser;

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
		return value;
	}

	public String toString() {
		return String.valueOf(this.value);
	}

}
