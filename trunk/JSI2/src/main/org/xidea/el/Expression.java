package org.xidea.el;

import java.util.Map;

public interface Expression {
	@SuppressWarnings("unchecked")
	public Object evaluate(Map context);
}