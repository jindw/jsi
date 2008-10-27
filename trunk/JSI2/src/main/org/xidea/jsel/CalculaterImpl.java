package org.xidea.jsel;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculaterImpl implements Calculater {
	private static final Object SKIP_QUESTION = new Object();

	private static Map<Class<?>, Map<String, PropertyDescriptor>> classPropertyMap = new HashMap<Class<?>, Map<String, PropertyDescriptor>>();

	private static Map<String, PropertyDescriptor> getPropertyMap(Class<?> clazz) {
		Map<String, PropertyDescriptor> propertyMap = classPropertyMap
				.get(clazz);
		if (propertyMap == null) {
			try {
				propertyMap = new HashMap<String, PropertyDescriptor>();
				classPropertyMap.put(clazz, propertyMap);
				PropertyDescriptor[] properties = java.beans.Introspector
						.getBeanInfo(clazz).getPropertyDescriptors();
				for (int i = 0; i < properties.length; i++) {
					PropertyDescriptor property = properties[i];
					propertyMap.put(property.getName(), property);
				}
			} catch (Exception e) {
			}
		}
		return propertyMap;
	}

	@SuppressWarnings("unchecked")
	public static Object getValue(Object context, Object key) {
		if (context != null) {
			try {
				if (context instanceof Object[]) {
					return ((Object[]) context)[toIndex(key)];
				} else if (context instanceof List) {
					return ((List<Object>) context).get(toIndex(key));
				}
			} catch (Exception ex) {

			}
			if (context instanceof Map) {
				return ((Map) context).get(key);
			}
			Map<String, PropertyDescriptor> pm = getPropertyMap(context
					.getClass());
			PropertyDescriptor pd = pm.get(key);
			if (pd != null) {
				Method method = pd.getReadMethod();
				if (method != null) {
					try {
						return method.invoke(context);
					} catch (Exception e) {
					}
				}
			}
		}
		return null;
	}

	private static int toIndex(Object key) {
		return key instanceof Number ? ((Number) key).intValue() : Integer
				.valueOf(String.valueOf(key));
	}

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

	public Object compute(OperatorToken op, Object arg1, Object arg2) {
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
			if(arg1 == SKIP_QUESTION){
				return arg2;
			}else{
				return arg1;
			}
			
		case ExpressionToken.TYPE_GET_PROP:
			return getValue(arg1, arg2);
		case ExpressionToken.TYPE_GET_GLOBAL_METHOD:

		}
		throw new RuntimeException("不支持的操作符" + op.getType());

	}
}
