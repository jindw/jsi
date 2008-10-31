package org.xidea.template;

import java.util.Collection;
import java.util.Map;

public class ContainsStringExpression implements Expression {
	private Expression valueEL;
	private Expression collectionEL;

	public ContainsStringExpression( Expression collectionEL,Expression valueEL) {
		this.valueEL = valueEL;
		this.collectionEL = collectionEL;
	}

	@SuppressWarnings("unchecked")
	public Object evaluate(Map context) {
		Object collection = collectionEL.evaluate(context);
		Object value = valueEL.evaluate(context);
		if (value != null) {
			value = String.valueOf(value);
			if (collection instanceof Object[]) {
				for (Object item : (Object[]) collection) {
					if (item != null && value.equals(String.valueOf(item))) {
						return true;
					}
				}
			}
			if (collection instanceof Collection) {
				for (Object item : (Collection) collection) {
					if (item != null && value.equals(String.valueOf(item))) {
						return true;
					}
				}
			}
		}
		return false;

	}

}
