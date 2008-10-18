package org.xidea.jsel;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CalculaterImpl implements Calculater {

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

	public static Object getValue(Object context, Object key) {
		if (context != null) {
			try{
				if (context instanceof Object[]) {
					return ((Object[]) context)[toIndex(key)];
				} else if (context instanceof List){
					return ((List<Object>) context).get(toIndex(key));
				}
			}catch(Exception ex){
				
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
		double offset = toDouble(arg1) - toDouble(arg2);
		return offset > 0 ? 1 : offset == 0 ? 0 : -1;
	}

	public boolean compute(OperatorToken op, ValueStack stack,Iterator<ExpressionToken> it) {
		Object arg1 = null;
		Object arg2 = null;
		Object arg3 = null;
		switch (op.getType()) {
		case ExpressionToken.TYPE_NOT:
			stack.push(!toBoolean(stack.pop()));
			return false;
		case ExpressionToken.TYPE_POS:
			stack.push(toDouble(stack.pop()));return false;
		case ExpressionToken.TYPE_NEG:
			stack.push(-toDouble(stack.pop()));return false;
		/* +-*%/*/
		case ExpressionToken.TYPE_ADD:
			stack.push(toDouble(stack.pop()) + toDouble(stack.pop()));return false;
		case ExpressionToken.TYPE_SUB:
			stack.push(toDouble(stack.pop()) - toDouble(stack.pop()));return false;
		case ExpressionToken.TYPE_MUL:
			stack.push(toDouble(stack.pop()) * toDouble(stack.pop()));return false;
		case ExpressionToken.TYPE_DIV:
			stack.push(toDouble(stack.pop()) / toDouble(stack.pop()));return false;
		case ExpressionToken.TYPE_MOD:
			stack.push(toDouble(stack.pop()) % toDouble(stack.pop()));return false;
			
		/* boolean*/
		case ExpressionToken.TYPE_GT:
			stack.push(compare(stack.pop(),stack.pop()) >0);return false;
		case ExpressionToken.TYPE_GTEQ:
			stack.push(compare(stack.pop(),stack.pop()) >=0);return false;
		case ExpressionToken.TYPE_EQ:
			stack.push(compare(stack.pop(),stack.pop()) ==0);return false;
		case ExpressionToken.TYPE_LT:
			stack.push(compare(stack.pop(),stack.pop()) <0);return false;
		case ExpressionToken.TYPE_LTEQ:
			stack.push(compare(stack.pop(),stack.pop()) <=0);return false;
			
		/*and or*/
		case ExpressionToken.TYPE_AND:
			arg1 = stack.pop();
			arg2 = stack.pop();
			if(arg2 == VarToken.LazyToken){
				if(toBoolean(arg1)){
				    stack.push(true);return false;
				}else{//false
					stack.push(arg1);return true;////skip
				}
			}else{
				if(toBoolean(arg1)){//1 true
					stack.push(arg2);return false;
				}else{//1 false
					stack.push(arg1);return false;
				}
			}
		case ExpressionToken.TYPE_OR:
			arg1 = stack.pop();
			arg2 = stack.pop();
			if(arg2 == VarToken.LazyToken){
				if(toBoolean(arg1)){
					stack.push(arg1);return true;////skip
				}else{
				    stack.push(false);return false;
				}
			}else{
				if(toBoolean(arg1)){
					stack.push(arg1);return false;
				}else{
					stack.push(arg2);return false;
				}
			}
		case ExpressionToken.TYPE_QUESTION://a?b:c  -> a?:bc -- >a?b:c
			if(toBoolean(stack.pop())){//取值1
				stack.push(true);
				return false;
			}else{//跳过 取值2
				stack.push(false);
				return true;
			}
		case ExpressionToken.TYPE_QUESTION_SELECT:
			arg1 = stack.pop();
			arg2 = stack.pop();
			if(toBoolean(arg1)){//取值1 跳过
				stack.push(arg2);
				return true;
			}else{//取值2 前面已经跳过一次了
				stack.push(arg2);
				return false;
			}
		case ExpressionToken.TYPE_GET_PROP:
			arg1 = stack.pop();
			arg2 = stack.pop();
			stack.push(getValue(arg1, arg2));
			return false;
		case ExpressionToken.TYPE_GET_GLOBAL_METHOD:

		}
		throw new RuntimeException("不支持的操作符"+op.getType());

	}
}
