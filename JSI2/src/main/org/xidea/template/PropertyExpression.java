package org.xidea.template;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class PropertyExpression implements Expression{

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

	private static int toInt(Object key){
		return key instanceof Number?((Number)key).intValue():Integer.valueOf(String.valueOf(key));
	}

	@SuppressWarnings("unchecked")
	public static Object getValue(Object context, Object key) {
		if (context != null) {
			try{
				if (context instanceof Object[]) {
					return ((Object[]) context)[toInt(key)];
				} else if (context instanceof List){
					return ((List<Object>) context).get(toInt(key));
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

	private final Object[] el;

	public PropertyExpression(Object[] el) {
		this.el = el;
	}


	public Object evaluate(Map<Object, Object> context) {
		int i = el.length - 1;
		Object value = getValue(context, el[i]);
		while (value != null && i-- > 0) {
			Object key = el[i];
			value = getValue(context, key);
		}
		return value;
	}

	static Map<Object, Object> map(Object source){
		final HashSet<Entry<Object, Object>> base = new HashSet<Entry<Object, Object>>();
		Map<String, PropertyDescriptor> propertyMap = getPropertyMap(source.getClass());
		for (String key : propertyMap.keySet()) {
			base.add(new PropertyEntry(key,source));
		}
		return new AbstractMap<Object, Object>(){
			@Override
			public Set<Entry<Object, Object>> entrySet() {
				return base;
			}
		};
	}
}
class PropertyEntry implements Entry<Object , Object>{
	private static Object NULL = new Object();
	private Object key;
	private Object source;
	private Object value = NULL;
	
	public PropertyEntry(String key,Object source){
		this.source = source;
		this.key = key;
	}

	public Object getKey() {
		return key;
	}

	public Object getValue() {
		if(NULL == value){
			value = PropertyExpression.getValue(source, key);
		}
		return value;
	}

	public Object setValue(Object value) {
		return this.value = value;
	}

	
}
