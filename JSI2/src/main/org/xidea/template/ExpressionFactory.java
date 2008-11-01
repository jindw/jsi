package org.xidea.template;

import org.xidea.el.Expression;

public interface ExpressionFactory {
	public Expression createExpression(String el);
}