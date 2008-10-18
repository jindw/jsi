package org.xidea.template;

import java.util.Map;

public interface Expression {
	public Object evaluate(Map<Object, Object> context);
}