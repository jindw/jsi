package org.xidea.el;


public interface ExpressionFactory {
	public Expression createEL(String el);
	public String optimizeEL(String expression);
}