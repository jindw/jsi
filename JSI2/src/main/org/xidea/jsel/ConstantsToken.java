package org.xidea.jsel;

public class ConstantsToken implements ExpressionToken {
	private Object value;

	public ConstantsToken(Object value) {
		this.value = value;
	}

	public int getType() {
		return TYPE_CONSTANTS;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public boolean equals(Object object) {
		if(object instanceof ConstantsToken){
			Object target = ((ConstantsToken)object).getValue();
			if(this.value !=null ){
				return this.value.equals(target);
			}else{
				return target == null;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return value == null?0:value.hashCode();
	}
	public String toString(){
		return String.valueOf(this.value);
	}
	
}
