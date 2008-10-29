package org.xidea.el;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xidea.el.parser.ExpressionToken;
import org.xidea.el.parser.OperatorToken;
import org.xidea.template.ReflectUtil;

public class CalculaterImpl extends NumberArithmetic implements Calculater {
	private static final Object SKIP_QUESTION = new Object();

	protected boolean toBoolean(Object value) {
		if (value instanceof Number) {
			if (((Number) value).floatValue() == 0) {
				return false;
			}
		}
		return value != null && !Boolean.FALSE.equals(value)
				&& !"".equals(value);
	}

	protected boolean compare(int type, Object arg1, Object arg2) {
		switch (type) {
		case ExpressionToken.TYPE_GT:
			return compare(arg1, arg2) > 0;
		case ExpressionToken.TYPE_GTEQ:
			return compare(arg1, arg2) >= 0;
		case ExpressionToken.TYPE_EQ:
			return compare(arg1, arg2) == 0;
		case ExpressionToken.TYPE_LT:
			return compare(arg1, arg2) < 0;
		case ExpressionToken.TYPE_LTEQ:
			return compare(arg1, arg2) <= 0;
		}
		throw new RuntimeException("怎么可能？？？");
	}

	protected int compare(Object arg1, Object arg2) {
		if (arg1 instanceof Comparable<?> && arg2 instanceof Comparable<?>) {
			@SuppressWarnings("unchecked")
			Comparable<Object> comparable = ((Comparable<Object>) arg1);
			return comparable.compareTo((Comparable<?>) arg2);
		}
		double offset = toFloat(arg1) - toFloat(arg2);
		return offset > 0 ? 1 : offset == 0 ? 0 : -1;
	}

	protected Number toNumber(Object arg1, boolean force) {
		if (arg1 == null) {
			return 0;
		} else if (arg1 instanceof Boolean) {
			return (Boolean) arg1 ? 1 : 0;
		} else if (arg1 instanceof Number) {
			return (Number) arg1;
		} else if (force) {
			String n = String.valueOf(arg1);
			try {
				if (n.indexOf('.') >= 0) {
					return Double.parseDouble(n);
				}
				if (n.startsWith("0x")) {
					return Long.parseLong(n.substring(2),16);
				} else if (n.startsWith("0")) {
					return Long.parseLong(n.substring(1),8);
				} else {
					return Long.parseLong(n.substring(1),10);
				}
			} catch (NumberFormatException ex) {
				return Double.NaN;
			}
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public Object compute(OperatorToken op, final Object arg1, final Object arg2) {
		final int type = op.getType();
		switch (type) {
		case ExpressionToken.TYPE_NOT:
			return !toBoolean(arg1);
		case ExpressionToken.TYPE_POS:
			return toNumber(arg1,true);
		case ExpressionToken.TYPE_NEG:
			return this.subtract(0,toNumber(arg1,true));
			/* +-*%/ */
		case ExpressionToken.TYPE_ADD:
			Number n1 = toNumber(arg1,false);
			Number n2 = toNumber(arg2,false);
			if(n1 != null && n2 != null){
				return this.add(n1,n2);
			}else{
				return String.valueOf(arg1)+String.valueOf(arg2);
			}
		case ExpressionToken.TYPE_SUB:
			return this.subtract(toNumber(arg1,true),toNumber(arg2,true));
		case ExpressionToken.TYPE_MUL:
			return this.multiply(toNumber(arg1,true),toNumber(arg2,true));
		case ExpressionToken.TYPE_DIV:
			return this.divide(toNumber(arg1,true),toNumber(arg2,true));
		case ExpressionToken.TYPE_MOD:
			return this.modulus(toNumber(arg1,true),toNumber(arg2,true));

			/* boolean */
		case ExpressionToken.TYPE_GT:
		case ExpressionToken.TYPE_GTEQ:
		case ExpressionToken.TYPE_EQ:
		case ExpressionToken.TYPE_LT:
		case ExpressionToken.TYPE_LTEQ:
			return compare(type, arg1, arg2);

			/* and or */
		case ExpressionToken.TYPE_AND:
			if (toBoolean(arg1)) {
				return arg2;// 进一步判断
			} else {// false
				return arg1;// //skip
			}

		case ExpressionToken.TYPE_OR:
			if (toBoolean(arg1)) {
				return arg1;
			} else {
				return arg2;
			}
		case ExpressionToken.TYPE_QUESTION:// a?b:c -> a?:bc -- >a?b:c
			if (toBoolean(arg1)) {// 取值1
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
			return new Invoker(null, String.valueOf(arg1));
		case ExpressionToken.TYPE_GET_METHOD:
			return new Invoker(arg1, String.valueOf(arg2));
		case ExpressionToken.TYPE_INVOKE_METHOD:
			try {
				return ((Invoker) arg1).invoke(((List) arg2).toArray());
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
}
