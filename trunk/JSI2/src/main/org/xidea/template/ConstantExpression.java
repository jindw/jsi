package org.xidea.template;

import java.util.Map;

public class ConstantExpression implements Expression {
	private Object value;
	public ConstantExpression(Object value){
		this.value = value;
	}
	public Object evaluate(Map<Object, Object> context) {
		return value;
	}

}
