package org.xidea.el.parser;

import java.util.Arrays;

public class LazyToken implements ExpressionToken {

	private ExpressionToken[] children;

	public LazyToken() {
	}

	public int getType() {
		return VALUE_LAZY;
	}

	public ExpressionToken[] getChildren() {
		return this.children;
	}
	void setChildren(ExpressionToken[] children) {
		this.children = children;
	}
	public String toString() {
		return "#" + Arrays.asList(this.children);
	}

}
