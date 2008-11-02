package org.xidea.el.parser;

public class VarToken implements ExpressionToken{
	private String value;
	VarToken(String value){
		this.value = value;
	}
	public int getType() {
		return VALUE_CONSTANTS;
	}
	public String getValue(){
		return value;
	}
	public String toString(){
		return this.value;
	}
}