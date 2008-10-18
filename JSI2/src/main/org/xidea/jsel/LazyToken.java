package org.xidea.jsel;

public class LazyToken implements ExpressionToken {
	private int step = -1;
	public static final ExpressionToken LAZY_TOKEN_END = new ExpressionToken() {
		public int getType() {
			return SKIP_END;
		}
	};

	public LazyToken() {
	}

	public int getType() {
		return SKIP_BEGIN;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof LazyToken) {
			return step == ((LazyToken) object).getStep();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return step;
	}
	public String toString(){
		return "#"+this.step;
	}
}
