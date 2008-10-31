package org.xidea.template;

import java.util.Map;


public class ConstantExpression implements Expression {
	private Object value;
	public ConstantExpression(Object value){
		this.value = value;
	}
	@SuppressWarnings("unchecked")
	public Object evaluate(Map context) {
		return value;
	}

}
