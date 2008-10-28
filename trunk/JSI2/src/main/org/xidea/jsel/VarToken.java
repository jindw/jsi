package org.xidea.jsel;

public class VarToken implements ExpressionToken{
	private String value;
	public VarToken(String value){
		this.value = value;
	}
	public int getType() {
		return TYPE_CONSTANTS;
	}
	public String getValue(){
		return value;
	}
	@Override
	public boolean equals(Object object) {
		if(object instanceof VarToken){
			Object target = ((VarToken)object).getValue();
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
		return this.value;
	}
}