package org.xidea.el;

public class ExpressionFactoryImpl implements ExpressionFactory {

	public Expression createExpression(String el) {
		return new ExpressionImpl(el);
	}
}
