package org.xidea.el;

import java.util.Map;

interface Expression {
	@SuppressWarnings("unchecked")
	public Object evaluate(Map context);
}