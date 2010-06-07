package org.xidea.jsi.impl;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.mozilla.javascript.WrapFactory;

/**
 * @see Java6Impl
 * @author test
 * 
 */


class RhinoImpl extends RhinoSupport {
	
	public static RhinoSupport create(boolean newEngine) {
		return newEngine?new NewRhinoImpl():new RhinoImpl();
	}

	static Context getContext() {
		Context context = Context.getCurrentContext();
		WrapFactory wrap = new WrapFactory() {
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
		context.setWrapFactory(wrap);
		wrap.setJavaPrimitiveWrap(false);
		return context;
	}
	@Override
	protected Object invokeJavaMethod(Object thiz, String name,
			Class<? extends Object> type, Object[] args) {
		Object result = this.invoke(thiz, name, args);
		if (type == Void.TYPE) {
			return null;
		} else {
			return Context.jsToJava(result, type);
		}
	}
	public Object invoke(Object thisObj, Object function, Object... args) {
		Context cx = getContext();
		Scriptable thiz = Context.toObject(thisObj, (Scriptable) globals);
		if(!(function instanceof Function)){
			function = (Function) ScriptableObject.getProperty(thiz,
					function.toString());
		}
		return ((Function) function)
				.call(cx, (Scriptable) globals, thiz, args);
	}

	public Object eval(String code, String path, Map<String, Object> varMap) {
		Context cx = getContext();
		Scriptable localScope = (Scriptable) globals;
		if (varMap != null) {
			Scriptable globals = localScope;
			localScope = cx.newObject(globals);
			for (Object key : globals.getIds()) {
				if (key instanceof String) {
					String index = (String) key;
					Object value = globals.get(index, globals);
					localScope.put(index, localScope, value);
				}
			}
			for (String key : varMap.keySet()) {
				localScope.put(key, localScope, varMap.get(key));
			}
		}
		return cx.evaluateString(localScope, code, path, 1, null);

	}

}
class NewRhinoImpl extends RhinoImpl {
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
			Context cx = Context.enter();
			cx.getWrapFactory().setJavaPrimitiveWrap(false);
			return super.invokeJavaMethod(thiz, name, type, args);
		} finally {
			Context.exit();
		}
	}
	public Object invoke(Object thisObj, Object function, Object... args) {

		try {
			Context cx = Context.enter();
			cx.getWrapFactory().setJavaPrimitiveWrap(false);
			return super.invoke(thisObj, function, args);
		} finally {
			Context.exit();
		}
	}

	public Object eval(String code, String path, Map<String, Object> varMap) {
		try {
			Context cx = Context.enter();
			cx.getWrapFactory().setJavaPrimitiveWrap(false);
			return super.eval(code, path, varMap);
		} finally {
			Context.exit();
		}
	}
}