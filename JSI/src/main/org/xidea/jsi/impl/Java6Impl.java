package org.xidea.jsi.impl;

import java.util.Map;

import sun.org.mozilla.javascript.internal.WrapFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sun.org.mozilla.javascript.internal.ScriptableObject;

import sun.org.mozilla.javascript.internal.ScriptRuntime;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;

class Java6Impl extends RuntimeSupport {

	private static final WrapFactory wrap = new WrapFactory() {
		@SuppressWarnings("unchecked")
		@Override
		public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
				final Object javaObject, Class staticType) {
			if (NodeList.class == staticType) {
				return super.wrapAsJavaObject(cx, scope, new NodeList() {
					public Node item(int index) {
						return ((NodeList) javaObject).item(index);
					}

					public int getLength() {
						return ((NodeList) javaObject).getLength();
					}
				}, staticType);
			}
			return super
					.wrapAsJavaObject(cx, scope, javaObject, staticType);
		}
	};
	public static RuntimeSupport create(boolean newEngine) {
		return newEngine ? new NewJava6Impl() : new Java6Impl();
	}

	static Context getContext() {
		Context context = Context.getCurrentContext();
		
		context.setWrapFactory(wrap);
		wrap.setJavaPrimitiveWrap(false);
		return context;
	}

	@Override
	protected Object invokeJavaMethod(Object thiz, String name,
			Class<? extends Object> type, Object[] args) {
		Object result = invoke(thiz, name, args);
		if (type == Void.TYPE) {
			return null;
		} else {
			return Context.jsToJava(result, type);
		}
	}

	@Override
	public Object invoke(Object thisObj, Object function, Object... args) {
		Context cx = getContext();
		Scriptable thiz = Context.toObject(thisObj, (Scriptable) globals);
		if (!(function instanceof Function)) {
			function = (Function) ScriptableObject.getProperty(thiz, function
					.toString());
		}
		return ((Function) function).call(cx, (Scriptable) globals, thiz, args);
	}

	@Override
	public Object eval(String code, String path, Map<String, Object> vars) {
		Context cx = getContext();
		Scriptable localScope = (Scriptable) globals;
		if (vars != null) {
			Scriptable globals = localScope;
			localScope = cx.newObject(globals);
//			for (Object key : globals.getIds()) {
//				if (key instanceof String) {
//					String index = (String) key;
//					Object value = globals.get(index, globals);
//					localScope.put(index, localScope, value);
//				}
//			}
			for (String key : vars.keySet()) {
				localScope.put(key, localScope, vars.get(key));
			}
		}
		return cx.evaluateString(localScope, code, path, 1, null);

	}

}

class NewJava6Impl extends Java6Impl {
	{
		try {
			Context context = Context.enter();
			globals = ScriptRuntime.getGlobal(context);
		} finally {
			Context.exit();
		}
	}

	@Override
	protected Object invokeJavaMethod(Object thiz, String name,
			Class<? extends Object> type, Object[] args) {
		try {
			Context.enter();
			return super.invokeJavaMethod(thiz, name, type, args);
		} finally {
			Context.exit();
		}
	}

	@Override
	public Object invoke(Object thisObj, Object function, Object... args) {
		try {
			 Context.enter();
			return super.invoke(thisObj, function, args);
		} finally {
			Context.exit();
		}
	}

	@Override
	public Object eval(String code, String path, Map<String, Object> varMap) {
		try {
			Context.enter();
			return super.eval(code, path, varMap);
		} finally {
			Context.exit();
		}
	}
}
