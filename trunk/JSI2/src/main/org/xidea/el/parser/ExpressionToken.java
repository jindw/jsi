package org.xidea.el.parser;

/**
 * @author jindw
 */
public abstract interface ExpressionToken {

	public static final int VALUE_CONSTANTS = 0;//'c';
	public static final int VALUE_VAR = 1;//'n';
	public static final int VALUE_NEW_MAP = 2;//{;
	public static final int VALUE_NEW_LIST = 3;//[;
	public static final int VALUE_LAZY = 4;
	
	public static final int BRACKET_BEGIN = 5;//'('[{;
	public static final int BRACKET_END = 6;//')']};


	
	//与正负符号共享字面值
	public static final int OP_ADD = 10;//'+';
	public static final int OP_SUB = 11;//'-';
	
	public static final int OP_MUL = 12;//'*';
	public static final int OP_DIV = 13;//'/';
	public static final int OP_MOD = 14;//'%';
	public static final int OP_QUESTION = 15;//'?';
	public static final int OP_QUESTION_SELECT = 16;//':';
	public static final int OP_GET_PROP = 17;//'.';
	
	public static final int OP_LT = 18;//'<';
	public static final int OP_GT = 19;//'>';
	public static final int OP_LTEQ = 20;//('<' << 8) + '=';// '<=';
	public static final int OP_GTEQ = 21;//('>' << 8) + '=';// '>=';
	public static final int OP_EQ = 22;//('=' << 8) + '=';// '==';
	public static final int OP_NOTEQ = 23;//('!' << 8) + '=';// '!=';
	public static final int OP_AND = 24;//('&' << 8) + '&';
	public static final int OP_OR = 25;//('|' << 8) + '|';// '||';
	
	

	public static final int OP_NOT = 26;//'!';
	public static final int OP_POS = 27;//('+'<<8) + 'p';//負數
	public static final int OP_NEG = 28;//('-'<<8) + 'n';//負數

	public static final int OP_GET_METHOD = 29;//('.'<<16) + ('('<<8) + ')';
	public static final int OP_GET_GLOBAL_METHOD = 30;//('('<<8) + ')';
	public static final int OP_INVOKE_METHOD = 31;


	//与Map Join 共享字面量（map join 会忽略）
	public static final int OP_PARAM_JOIN = 32;//,
	//与三元运算符共享字面值
	public static final int OP_MAP_PUSH = 33;//:,
	
	
	public abstract int getType();

	public abstract String toString();
	


}
