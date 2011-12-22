package org.xidea.jsi.impl.v2;

import java.util.List;
import java.util.Map;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.NativeJavaObject;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;

@SuppressWarnings("unchecked")
class Java6MapList extends NativeJavaObject {
	private static final long serialVersionUID = 1L;
	boolean isList = false;

	public Java6MapList(Scriptable scope, Object javaObject,
			Class<?> staticType) {
		super(scope, javaObject, staticType);
		isList = javaObject instanceof List<?>;
	}

	@SuppressWarnings("rawtypes")
	private List list() {
		return (List) javaObject;
	}
	@SuppressWarnings("rawtypes")
	private Map map() {
		return (Map)javaObject;
	}
	private int getLength() {
		if (isList) {
			return list().size();
		} else {
			return map().size();
		}
	}
	public String getClassName() {
		if (isList) {
			return "JavaList";
		} else {
			return "JavaMap";
		}
	}
	public Object unwrap() {
		return javaObject;
	}

	public boolean has(String id, Scriptable start) {
		if(isList){
			return id.equals("length") || super.has(id, start);
		}else{
			return map().containsKey(id) || super.has(id, start);
		}
	}

	public boolean has(int index, Scriptable start) {
		if(isList){
			return 0 <= index && index < getLength();
		}else{
			return super.has(index, start);
		}
	}

	public Object get(String id, Scriptable start) {
		if (isList) {
			if (id.equals("length")) {
				return new Integer(getLength());
			}
		}else{
			Context cx = Context.getCurrentContext();
			Object obj = map().get(id);
			if(obj != null){
				return cx.getWrapFactory().wrap(cx, this, obj, null);
			}
		}
		return super.get(id, start);
	}

	public Object get(int index, Scriptable start) {
		if (isList) {
			Context cx = Context.getCurrentContext();
			List list = list();
			if(index < list.size()){
				Object obj = list.get(index);
				if(obj!=null){
					return cx.getWrapFactory().wrap(cx, this, obj, null);
				}
			}
		}
		return null;
	}


	public void put(String id, Scriptable start, Object value) {
		// Ignore assignments to "length"--it's readonly.
		if(! isList){
			map().put(id, Context
					.jsToJava(value, Object.class));
		}else if (!id.equals("length")){
			super.put(id, start, value);
		}
	}

	public void put(int index, Scriptable start, Object value) {
		if (isList) {
			list().set(index, Context
					.jsToJava(value, Object.class));
		} else {
			super.put(index, start, value);
		}
	}
	public Object[] getIds() {
		if (isList) {

			int length = getLength();
			Object[] result = new Object[length];
			int i = length;
			while (--i >= 0)
				result[i] = new Integer(i);
			return result;
		} else {
			return map().keySet().toArray();
		}
	}

	public Scriptable getPrototype() {
		if (prototype == null) {
			prototype = ScriptableObject.getClassPrototype(this
					.getParentScope(), isList ? "Array" : "Object");
		}
		return prototype;
	}

}
