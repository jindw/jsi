package org.xidea.el;

import java.lang.reflect.Method;

public class Invoker {
	private Object object;
	private String name;
	
	public Invoker(Object object, String name) {
		this.object = object;
		this.name = name;
	}
	
	public Object invoke(Object... args) {
		try {
			Class<?> clazz =null;
			String name = this.name;
			if (object == null) {
				int index = name.lastIndexOf(".");
				clazz = Class.forName(name.substring(0,index));
				name = name.substring(index+1);
			}else{
				clazz = object.getClass();
			}
			Method method = clazz.getMethod(name,
					getClassList(args));
			return method.invoke(object, args);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	private static Class<?>[] EMPTY_TYPES = new Class<?>[0];

	private Class<?>[] getClassList(Object[] args) {
		if(args.length == 0){
			return EMPTY_TYPES;
		}
		Class<?>[] clazz = new Class<?>[args.length];
		for (int i = 0; i < clazz.length; i++) {
			Object object = args[i];
			if(object == null){
				clazz[i] = Object.class;
			}else{
				clazz[i] = object.getClass();
			}
		}
		return clazz;
	}
}
