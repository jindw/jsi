package org.xidea.jsel;

public abstract interface ExpressionToken {

	public static final String INTERNAL_METHOD_MAP = "#map";
	public static final String INTERNAL_METHOD_LIST = "#list";
	
	
	public static final int TYPE_CONSTANTS = 0;//'c';
	public static final int TYPE_VAR = 1;//'n';

	public static final int BRACKET_BEGIN = 2;//'('[{;
	public static final int BRACKET_END = 3;//')']};

	public static final int TYPE_NEW_MAP = 115;//{;
	public static final int TYPE_NEW_LIST = 1135;//[;
	
	//与三元运算符共享字面值
	public static final int TYPE_PARAM_JOIN = 5;//,
	public static final int TYPE_MAP_PUSH = 6;//:,
	
	//与正负符号共享字面值
	public static final int TYPE_ADD = 9;//'+';
	public static final int TYPE_SUB = 10;//'-';
	
	public static final int TYPE_MUL = 11;//'*';
	public static final int TYPE_DIV = 12;//'/';
	public static final int TYPE_MOD = 13;//'%';
	public static final int TYPE_QUESTION = 14;//'?';
	public static final int TYPE_QUESTION_SELECT = 15;//':';
	public static final int TYPE_GET_PROP = 16;//'.';
	
	public static final int TYPE_LT = 17;//'<';
	public static final int TYPE_GT = 18;//'>';
	public static final int TYPE_LTEQ = 19;//('<' << 8) + '=';// '<=';
	public static final int TYPE_GTEQ = 20;//('>' << 8) + '=';// '>=';
	public static final int TYPE_EQ = 21;//('=' << 8) + '=';// '==';
	public static final int TYPE_NOTEQ = 22;//('!' << 8) + '=';// '!=';
	public static final int TYPE_AND = 23;//('&' << 8) + '&';
	public static final int TYPE_OR = 24;//('|' << 8) + '|';// '||';
	
	

	public static final int TYPE_NOT = 25;//'!';
	public static final int TYPE_POS = 26;//('+'<<8) + 'p';//負數
	public static final int TYPE_NEG = 27;//('-'<<8) + 'n';//負數

	public static final int TYPE_GET_METHOD = 28;//('.'<<16) + ('('<<8) + ')';
	public static final int TYPE_GET_STATIC_METHOD = 29;//('('<<8) + ')';
	public static final int TYPE_INVOKE_METHOD = 30;
	public static final int TYPE_INVOKE_STATIC_METHOD = 31;

	public static final int SKIP_BEGIN = 100;
	public static final int SKIP_END = 101;
	
	
//	public static final int SKIP_OR = -10;
//	public static final int SKIP_AND = -11;
//	public static final int SKIP_QUESTION = -12;
	
	
	


	public abstract int getType();

	public abstract String toString();
	


}
