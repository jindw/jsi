package org.xidea.template;

import java.util.Map;

public interface Expression {
	@SuppressWarnings("unchecked")
	public Object evaluate(Map context);
}