package org.xidea.jsel;

import org.xidea.template.Expression;

public class JSExpressionFactory  implements org.xidea.template.ExpressionFactory{

	public Expression createExpression(String el) {
		return new ExpressionImpl(el);
	}
}
