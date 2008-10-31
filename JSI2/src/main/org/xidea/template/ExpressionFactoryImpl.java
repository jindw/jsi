package org.xidea.template;

import org.xidea.el.ExpressionImpl;

public class ExpressionFactoryImpl  implements ExpressionFactory{

	public Expression createExpression(String el) {
		return new ExpressionAdapter(el);
	}
	static class ExpressionAdapter extends ExpressionImpl implements Expression{

		public ExpressionAdapter(String el) {
			super(el);
		}
		
	}
}
