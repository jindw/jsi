package org.xidea.el.operation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReflectUtil {
	private static final Log log = LogFactory.getLog(ReflectUtil.class);
	private static final String LENGTH = "length";
	private static final Map<Class<?>, Map<String, PropertyDescriptor>> classPropertyMap = new HashMap<Class<?>, Map<String, PropertyDescriptor>>();

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

	private static int toIndex(Object key) {
		return key instanceof Number ? ((Number) key).intValue() : Integer
				.parseInt(String.valueOf(key));
	}

	public static Object getValue(Object context, Object key) {
		if (context != null) {
			try {
				if (context instanceof Object[]) {
					if (LENGTH.equals(key)) {
						return ((Object[]) context).length;
					} else {
						return ((Object[]) context)[toIndex(key)];
					}
				} else if (context instanceof Collection<?>) {
					if (LENGTH.equals(key)) {
						return ((List<?>) context).size();
					} else if (context instanceof Collection<?>) {
						return ((List<?>) context).get(toIndex(key));
					}
				}
				if (context instanceof Map) {
					return ((Map<?, ?>) context).get(key);
				}
				Map<String, PropertyDescriptor> pm = getPropertyMap(context
						.getClass());
				PropertyDescriptor pd = pm.get(key);
				if (pd != null) {
					Method method = pd.getReadMethod();
					if (method != null) {
						return method.invoke(context);
					}
				}
			} catch (Exception e) {
				if(log.isDebugEnabled()){
					log.debug(e);
				}
			}
		}
		return null;
	}
}
