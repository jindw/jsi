package org.xidea.jsi.impl;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * @see Java6Impl
 * @author test
 * 
 */


class RhinoImpl extends RhinoSupport {
	
	public static RhinoSupport create(boolean newEngine) {
		return newEngine?new NewRhinoImpl():new RhinoImpl();
	}
	
	public Object invoke(Object thisObj, Object function, Object... args) {
		Context cx = Context.getCurrentContext();
		Scriptable thiz = Context.toObject(thisObj, (Scriptable) topScope);
		return ((Function) function)
				.call(cx, (Scriptable) topScope, thiz, args);
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
class NewRhinoImpl extends RhinoImpl {
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