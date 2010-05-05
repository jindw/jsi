package org.xidea.jsi.impl;

import java.util.Map;

import sun.org.mozilla.javascript.internal.ScriptRuntime;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;

class Java6Impl extends RhinoSupport {

	public static RhinoSupport create(boolean newEngine) {
		return newEngine?new NewJava6Impl():new Java6Impl();
	}
	
	@Override
	public Object invoke(Object thisObj, Object function, Object... args) {
		Context cx = Context.getCurrentContext();
		cx.getWrapFactory().setJavaPrimitiveWrap(false);

		Scriptable thiz = Context.toObject(thisObj, (Scriptable) globals);
		return ((Function) function)
				.call(cx, (Scriptable) globals, thiz, args);
	}

	@Override
	public Object eval(String code, String path, Map<String, Object> vars) {
		Context cx = Context.getCurrentContext();
		Scriptable localScope = (Scriptable) globals;
		if (vars != null) {
			Scriptable globals = localScope;
			localScope = cx.newObject(globals);
			for (Object key : globals.getIds()) {
				if (key instanceof String) {
					String index = (String) key;
					Object value = globals.get(index, globals);
					localScope.put(index, localScope, value);
				}
			}
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
	public Object invoke(Object thisObj, Object function, Object... args) {
		try {
			Context cx = Context.enter();
			cx.getWrapFactory().setJavaPrimitiveWrap(false);
			return super.invoke(thisObj, function, args);
		} finally {
			Context.exit();
		}
	}

	@Override
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