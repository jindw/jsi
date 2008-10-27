package org.xidea.jsel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LazyToken implements ExpressionToken {

	private ExpressionToken[] children;

	public LazyToken() {
	}

	public int getType() {
		return SKIP_BEGIN;
	}

	public ExpressionToken[] getChildren() {
		return this.children;
	}
	public void setChildren(ExpressionToken[] children) {
		this.children = children;
	}
	public String toString() {
		return "#" + Arrays.asList(this.children);
	}

}
