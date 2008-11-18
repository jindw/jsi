package org.xidea.el.operation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class InvocableFactory implements Invocable {
	public static Invocable createProxy(final Method method) {
		if ((method.getModifiers() & Modifier.STATIC) > 0) {
			return new Invocable() {
				public Object invoke(Object... args) throws Exception {
					return method.invoke(null, args);
				}
			};
		}else{
			return null;
		}
	}
	public static Invocable createProxy(final Object object,
			final Invocable baseInvocable) {
		return new Invocable() {
			public Object invoke(Object... args) throws Exception {
				Object[] args2 = new Object[args.length + 1];
				System.arraycopy(args, 0, args2, 1, args.length);
				args2[0] = object;
				return baseInvocable.invoke(args2);
			}
		};
	}
	public static Invocable createProxy(final Object object, final String name) {
		return new Invocable() {
			public Object invoke(Object... args) throws Exception {
				Method method = object.getClass().getMethod(name,
						getClassList(args));
				return method.invoke(object, args);
			}
		};

	}
	private static Class<?>[] EMPTY_TYPES = new Class<?>[0];

	private static Class<?>[] getClassList(Object[] args) {
		if (args.length == 0) {
			return EMPTY_TYPES;
		}
		Class<?>[] clazz = new Class<?>[args.length];
		for (int i = 0; i < clazz.length; i++) {
			Object object = args[i];
			if (object == null) {
				clazz[i] = Object.class;
			} else {
				clazz[i] = object.getClass();
			}
		}
		return clazz;
	}
}
