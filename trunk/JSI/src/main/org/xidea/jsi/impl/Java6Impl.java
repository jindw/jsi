package org.xidea.jsi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sun.org.mozilla.javascript.internal.WrapFactory;

import org.w3c.dom.NodeList;

import sun.org.mozilla.javascript.internal.ScriptableObject;

import sun.org.mozilla.javascript.internal.ScriptRuntime;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;

class Java6Impl extends RuntimeSupport {

	private static final WrapFactory wrap = new WrapFactory() {
		@Override
		public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
				final Object javaObject, @SuppressWarnings("rawtypes") Class staticType) {
			if (NodeList.class == staticType) {
				return super.wrapAsJavaObject(cx, scope, wrapNodeList((NodeList)javaObject), staticType);
			}

			if (javaObject instanceof List || javaObject instanceof Map) {
				return new Java6MapList(scope, javaObject, staticType);
			}
			return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
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

	protected Object jsToJava(Class<? extends Object> type, Object result) {
		return Context.jsToJava(result, type);
	}

	@Override
	public Object invoke(Object thisObj, Object function, Object... args) {
		Context cx = getContext();
		Scriptable scope = (Scriptable) globals;
		if(thisObj == null){
			thisObj = scope;
		}
		Scriptable thiz = Context.toObject(thisObj, scope);
		if (!(function instanceof Function)) {
			function = ScriptableObject.getProperty(thiz, function.toString());
		}
		if (args != null) {
			int i = args.length;
			while (i-- > 0) {
				args[i] = wrap.wrap(cx, scope, args[i], Object.class);
			}
		}
		Object result = ((Function) function).call(cx, scope, thiz, args);
		return jsToJava(Object.class, result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object eval(Object thiz, String code, String path,
			Object scope) {
		Context cx = getContext();
		Scriptable localScope;
		Map<String, Object> varMap = null;
		if (scope instanceof Map) {
			varMap = (Map<String, Object>)scope;
			localScope = cx.newObject((Scriptable)this.globals);
			for (String key : varMap.keySet()) {
				localScope.put(key, localScope, varMap.get(key));
			}
		}else{
			localScope = (Scriptable) (scope==null?globals:scope);
		}

		if (thiz instanceof Scriptable) {
			Object[] args = EMPTY_ARG;
			StringBuilder buf = new StringBuilder("function(");
			if (varMap != null && !varMap.isEmpty()) {
				ArrayList<Object> list = new ArrayList<Object>();
				for (Map.Entry<String, Object> e : varMap.entrySet()) {
					if (!list.isEmpty()) {
						buf.append(",");
					}
					buf.append(e.getKey());
					list.add(e.getValue());
				}
				args = list.toArray();
			}
			buf.append("){");
			buf.append(code);
			buf.append("\n}");
			Function fn = cx.compileFunction(localScope, buf.toString(), path,
					1, null);
			return fn.call(cx, localScope, (Scriptable) thiz, args);
		} else {
			return cx.evaluateString(localScope, code, path, 1, null);
		}

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
			Context.enter();
			return super.invoke(thisObj, function, args);
		} finally {
			Context.exit();
		}
	}

	@Override
	public Object eval(Object thiz, String code, String path,
			Object varMap) {
		try {
			Context.enter();
			return super.eval(thiz, code, path, varMap);
		} finally {
			Context.exit();
		}
	}
}

