package org.jside.webserver.sjs;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;
import org.mozilla.javascript.Wrapper;

public class AdapterFactory {

	public Object toJava(Object o) {
		if (o == null || o == Undefined.instance || o == UniqueTag.NOT_FOUND || o == UniqueTag.NULL_VALUE) {
			return null;
		} else if (o instanceof Double) {
			return o.equals(0.0d)?0:o;
		} else if (o instanceof Wrapper) {
			return ((Wrapper) o).unwrap();
		} else if (o instanceof ScriptableObject) {
			ScriptableObject so = (ScriptableObject) o;
			String type = so.getClassName();
			if (type.equals("Boolean")) {
				return ScriptRuntime.toBoolean(so);
			} else if (type.equals("Number") || type.equals("String")
					|| type.equals("Date")) {
				return Context.jsToJava(o, Object.class);
			} else if (o instanceof NativeArray) {
				return new ScriptArrayList((NativeArray) o);
			} else if (o instanceof NativeObject) {
				return new ScriptObjectMap((NativeObject) o);
			} else {
				System.err
						.println("未知js对象" + so.getClass() + so.getClassName());
			}
		} else {
			return o;
		}
		return null;
	}

	class ScriptArrayList extends AbstractList<Object> {
		final NativeArray nao;

		public ScriptArrayList(NativeArray nativeArray) {
			nao = nativeArray;
		}

		@Override
		public Object get(int index) {
			return toJava(nao.get(index, nao));
		}

		@Override
		public int size() {
			return (int) nao.getLength();
		}

	}

	class ScriptObjectMap extends AbstractMap<String, Object> {
		private NativeObject no;

		ScriptObjectMap(NativeObject no) {
			this.no = no;
		}

		@Override
		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			for (String key : keySet()) {
				map.put(key, get(key));
			}
			return map.entrySet();
		}

		@Override
		public Set<String> keySet() {
			Context cx = Context.getCurrentContext();
			Object enums = ScriptRuntime.enumInit(no, cx, false);
			LinkedHashSet<String> keys = new LinkedHashSet<String>();
			while (ScriptRuntime.enumNext(enums)) {
				keys.add(ScriptRuntime
						.toString(ScriptRuntime.enumId(enums, cx)));
			}
			return keys;
		}

		@Override
		public boolean containsKey(Object key) {
			return no.has(String.valueOf(key), no);
		}

		@Override
		public Object get(Object key) {
			return toJava(no.get(String.valueOf(key), no));
		}

	}
}
