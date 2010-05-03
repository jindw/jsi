package org.xidea.jsi.impl;

import java.util.Map;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;

class Java6Impl extends Java6InternalImpl {
	// function(code){return evaler(this,code);}
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

class Java6InternalImpl extends RhinoSupport {
	// function(code){return evaler(this,code);}
	public Object invoke(Object thisObj, Object function, Object... args) {
		Context cx = Context.getCurrentContext();
		cx.getWrapFactory().setJavaPrimitiveWrap(false);

		Scriptable thiz = Context.toObject(thisObj, (Scriptable) topScope);
		return ((Function) function).call(cx, (Scriptable) topScope, thiz,
				args);
	}

	public Object eval(String code, String path, Map<String, Object> varMap) {
		Context cx = Context.getCurrentContext();
		Scriptable localScope = (Scriptable) topScope;
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