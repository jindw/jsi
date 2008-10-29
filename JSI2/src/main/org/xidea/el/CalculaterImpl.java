package org.xidea.el;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xidea.el.parser.ExpressionToken;
import org.xidea.el.parser.OperatorToken;
import org.xidea.template.ReflectUtil;

public class CalculaterImpl implements Calculater {
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

	protected double toDouble(Object value) {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else {
			return Double.parseDouble(String.valueOf(value));
		}
	}

	protected int compare(Object arg1, Object arg2) {
		if (arg1 instanceof Comparable<?> && arg2 instanceof Comparable<?>) {
			@SuppressWarnings("unchecked")
			Comparable<Object> comparable = ((Comparable<Object>) arg1);
			return comparable.compareTo((Comparable<?>) arg2);
		}
		double offset = toDouble(arg1) - toDouble(arg2);
		return offset > 0 ? 1 : offset == 0 ? 0 : -1;
	}

	@SuppressWarnings("unchecked")
	public Object compute(OperatorToken op, final Object arg1, final Object arg2) {
		switch (op.getType()) {
		case ExpressionToken.TYPE_NOT:
			return !toBoolean(arg1);
		case ExpressionToken.TYPE_POS:
			return toDouble(arg1);
		case ExpressionToken.TYPE_NEG:
			return -toDouble(arg1);
			/* +-*%/ */
		case ExpressionToken.TYPE_ADD:
			return toDouble(arg1) + toDouble(arg2);
		case ExpressionToken.TYPE_SUB:
			return toDouble(arg1) - toDouble(arg2);
		case ExpressionToken.TYPE_MUL:
			return toDouble(arg1) * toDouble(arg2);
		case ExpressionToken.TYPE_DIV:
			return toDouble(arg1) / toDouble(arg2);
		case ExpressionToken.TYPE_MOD:
			return toDouble(arg1) % toDouble(arg2);

			/* boolean */
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
			return new Invoker(null,String.valueOf(arg1));
		case ExpressionToken.TYPE_GET_METHOD:
			return new Invoker(arg1, String.valueOf(arg2));
		case ExpressionToken.TYPE_INVOKE_METHOD:
			try {
				return ((Invoker)arg1).invoke(((List) arg2).toArray());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		case ExpressionToken.TYPE_NEW_LIST:
			return new ArrayList<Object>();
		case ExpressionToken.TYPE_NEW_MAP:
			return new HashMap<Object,Object>();
		case ExpressionToken.TYPE_PARAM_JOIN:
			((List) arg1).add(arg2);
			return arg1;
		case ExpressionToken.TYPE_MAP_PUSH:
			((Map) arg1).put(op.getParam(),arg2);
			return arg1;
		}
		throw new RuntimeException("不支持的操作符" + op.getType());

	}
}
