package org.xidea.el;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xidea.el.parser.ExpressionToken;
import org.xidea.el.parser.OperatorToken;
import org.xidea.template.ReflectUtil;

public class CalculaterImpl extends NumberArithmetic implements Calculater {
	private static final Object SKIP_QUESTION = new Object();

	/**
	 * 
	 * @param <T>
	 * @param value
	 * @param expectedType
	 * @see <a href="http://www.ecma-international.org/publications/standards/Ecma-262.htm">Ecma262</a>
	 * @return  <null|Number|Boolean|String>
	 */
	@SuppressWarnings("unchecked")
	public Object ToPrimitive(Object value, Class<?> expectedType) {
		boolean toString;
		if (expectedType == Number.class){
			toString = false;
		}else if (expectedType == String.class){
			toString = true;
		}else if(expectedType == null){
			toString =!(value instanceof Date);
		}else{
			throw new IllegalArgumentException("expectedType 只能是 Number或者String");
		}
		if(value == null){
			return null;
		}else if (value instanceof Boolean) {
			return value;
		} else if (value instanceof Number) {
			return value;
		} else if (value instanceof String) {
			return value;
		}
		
		if (toString) {
			return String.valueOf(value);
		} else{
			if (value instanceof Date) {
				return new Long(((Date) value).getTime());
			} else {
				return String.valueOf(value);
			}
			
		}
	}
	/**
	 * @param value
	 * @see <a href="http://www.ecma-international.org/publications/standards/Ecma-262.htm">Ecma262</a>
	 * @return
	 */
	public boolean ToBoolean(Object value) {
		if( value == null){
			return false;
		}else if (value instanceof Number) {
			if(value instanceof Float || value instanceof Double){
				return ((Number) value).floatValue() != 0;
			}else if(value instanceof Long){
				return ((Number) value).longValue() != 0;
			}else {
				return ((Number) value).intValue() != 0;
			}
		}else if(value instanceof String){
			return ((String)value).length() > 0;
		}else if(value instanceof Boolean){
			return (Boolean)value;
		}else{
			return true;
		}
	}
	/**
	 * @param arg1
	 * @param force
	 * @see <a href="http://www.ecma-international.org/publications/standards/Ecma-262.htm">Ecma262</a>
	 * @return
	 */
	private Number ToNumber(Object value) {
		value = ToPrimitive(value,String.class);
		if(value == null){
			return 0;
		}else if (value instanceof Boolean) {
			return ((Boolean)value)?1:0;
		} else if (value instanceof Number) {
			return (Number)value;
		}else{
			String text = (String)value;
			try {
				if (text.indexOf('.') >= 0) {
					return Float.parseFloat(text);
				}
				if (text.startsWith("0x")) {
					return Long.parseLong(text.substring(2), 16);
				} else if (text.startsWith("0")) {
					return Integer.parseInt(text.substring(1), 8);
				} else {
					return Integer.parseInt(text.substring(1), 10);
				}
			} catch (NumberFormatException ex) {
				return Double.NaN;
			}
		}
	}

	protected boolean compare(int type, Object arg1, Object arg2) {
		switch (type) {
		case ExpressionToken.TYPE_EQ:
			return compare(arg1, arg2,-1) == 0;
		case ExpressionToken.TYPE_NOTEQ:
			return compare(arg1, arg2,0) != 0;
		case ExpressionToken.TYPE_GT:
			return compare(arg1, arg2,-1) > 0;
		case ExpressionToken.TYPE_GTEQ:
			return compare(arg1, arg2,-1) >= 0;
		case ExpressionToken.TYPE_LT:
			return compare(arg1, arg2,1) < 0;
		case ExpressionToken.TYPE_LTEQ:
			return compare(arg1, arg2,1) <= 0;
		}
		throw new RuntimeException("怎么可能？？？");
	}

	/**
	 * @param arg1
	 * @param arg2
	 * @see <a href="http://www.ecma-international.org/publications/standards/Ecma-262.htm">Ecma262</a>
	 * @return
	 */
	protected int compare(Object arg1, Object arg2,int validReturn) {
		if(arg1 == null){
			if(arg2 == null){
				return 0;
			}
		}else if(arg1.equals(arg2)){
			return 0;
		}
		arg1 = ToPrimitive(arg1, Number.class);
		arg2 = ToPrimitive(arg2, Number.class);
		if(arg1 instanceof String && arg2 instanceof String){
			return ((String)arg1).compareTo((String)arg2);
		}
		Number n1 = ToNumber(arg1);
		Number n2 = ToNumber(arg2);
		return this.compare(n1,n2,validReturn);
	}


	@SuppressWarnings("unchecked")
	public Object compute(OperatorToken op, final Object arg1, final Object arg2) {
		final int type = op.getType();
		switch (type) {
		case ExpressionToken.TYPE_NOT:
			return !ToBoolean(arg1);
		case ExpressionToken.TYPE_POS:
			return ToNumber(arg1);
		case ExpressionToken.TYPE_NEG:
			return this.subtract(0, ToNumber(arg1));
			/* +-*%/ */
		case ExpressionToken.TYPE_ADD:
			Object p1 = ToPrimitive(arg1, String.class);
			Object p2 = ToPrimitive(arg2, String.class);
			if (p1 instanceof String && p2 instanceof String) {
				return String.valueOf(p1) + p2;
			} else {
				return this.add(ToNumber(p1), ToNumber(p2));
			}
		case ExpressionToken.TYPE_SUB:
			return this.subtract(ToNumber(arg1), ToNumber(arg2));
		case ExpressionToken.TYPE_MUL:
			return this.multiply(ToNumber(arg1), ToNumber(arg2));
		case ExpressionToken.TYPE_DIV:
			return this.divide(ToNumber(arg1), ToNumber(arg2));
		case ExpressionToken.TYPE_MOD:
			return this.modulus(ToNumber(arg1), ToNumber(arg2));

			/* boolean */
		case ExpressionToken.TYPE_GT:
		case ExpressionToken.TYPE_GTEQ:
		case ExpressionToken.TYPE_NOTEQ:
		case ExpressionToken.TYPE_EQ:
		case ExpressionToken.TYPE_LT:
		case ExpressionToken.TYPE_LTEQ:
			return compare(type, arg1, arg2);

			/* and or */
		case ExpressionToken.TYPE_AND:
			if (ToBoolean(arg1)) {
				return arg2;// 进一步判断
			} else {// false
				return arg1;// //skip
			}

		case ExpressionToken.TYPE_OR:
			if (ToBoolean(arg1)) {
				return arg1;
			} else {
				return arg2;
			}
		case ExpressionToken.TYPE_QUESTION:// a?b:c -> a?:bc -- >a?b:c
			if (ToBoolean(arg1)) {// 取值1
				return arg2;
			} else {// 跳过 取值2
				return SKIP_QUESTION;
			}
		case ExpressionToken.TYPE_QUESTION_SELECT:
			if (arg1 == SKIP_QUESTION) {
				return arg2;
			} else {
				return arg1;
			}

		case ExpressionToken.TYPE_GET_PROP:
			return ReflectUtil.getValue(arg1, arg2);
		case ExpressionToken.TYPE_GET_STATIC_METHOD:
			return createInvocable(null, String.valueOf(arg1));
		case ExpressionToken.TYPE_GET_METHOD:
			return createInvocable(arg1, String.valueOf(arg2));
		case ExpressionToken.TYPE_INVOKE_METHOD:
			try {
				return ((Invocable) arg1).invoke(((List) arg2).toArray());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		case ExpressionToken.TYPE_NEW_LIST:
			return new ArrayList<Object>();
		case ExpressionToken.TYPE_NEW_MAP:
			return new HashMap<Object, Object>();
		case ExpressionToken.TYPE_PARAM_JOIN:
			((List) arg1).add(arg2);
			return arg1;
		case ExpressionToken.TYPE_MAP_PUSH:
			((Map) arg1).put(op.getParam(), arg2);
			return arg1;
		}
		throw new RuntimeException("不支持的操作符" + op.getType());

	}

	private Invocable createInvocable(final Object thisObject, final String name) {
		return new InvokerImp(thisObject, name);
	}
}
