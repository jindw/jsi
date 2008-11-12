package org.xidea.el;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 改造自stringtree，将类改成线程安全的方式。
 * 并提供简单的静态编码方法{@see JSONEncoder#encode(Object)}
 * @author stringtree.org
 * @author jindw
 */
public class JSONEncoder {
	private static JSONEncoder encoder = new JSONEncoder(true);
	boolean emitClassName = true;

	public JSONEncoder(boolean emitClassName) {
		this.emitClassName = emitClassName;
	}

	public JSONEncoder() {
	}

	public static String encode(Object value) {
		StringWriter buf = new StringWriter();
		try {
			encoder.encode(value, buf);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return buf.toString();
	}

	public void encode(Object value, Writer out) throws IOException {
		List<Object> result = print(value, null, out);
		if (result != null) {
			result.clear();
		}
	}

	private List<Object> print(Object object, List<Object> cached, Writer out)
			throws IOException {
		if (object == null) {
			out.write("null");
		} else if (object instanceof Class) {
			out.write(String.valueOf(object));
		} else if (object instanceof Boolean) {
			out.write(String.valueOf(object));
		} else if (object instanceof Number) {
			out.write(String.valueOf(object));
		} else if (object instanceof String) {
			print((String) object, out);
		} else if (object instanceof Character) {
			print(String.valueOf(object), out);
		} else {
			if (cached == null) {
				cached = new ArrayList<Object>();
			} else if (cyclic(cached, object)) {
				print("null", out);
				return cached;
			}
			cached.add(object);
			if (object instanceof Map) {
				print((Map<?, ?>) object, cached, out);
			} else if (object instanceof Object[]) {
				print((Object[]) object, cached, out);
			} else if (object instanceof Iterator) {
				print((Iterator<?>) object, cached, out);
			} else if (object instanceof Collection) {
				print(((Collection<?>) object).iterator(), cached, out);
			} else {
				printObject(object, cached, out);
			}
		}
		return cached;
	}

	private void print(String obj, Writer out) throws IOException {
		out.write('"');
		CharacterIterator it = new StringCharacterIterator(obj.toString());
		for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
			if (c == '"')
				out.write("\\\"");
			else if (c == '\\')
				out.write("\\\\");
			else if (c == '/')
				out.write("\\/");
			else if (c == '\b')
				out.write("\\b");
			else if (c == '\f')
				out.write("\\f");
			else if (c == '\n')
				out.write("\\n");
			else if (c == '\r')
				out.write("\\r");
			else if (c == '\t')
				out.write("\\t");
			else if (Character.isISOControl(c)) {
				out.write("\\u");
				out.write(Integer.toHexString(0x10000+c),1,5);
			} else {
				out.write(c);
			}
		}
		out.write('"');
	}

	private boolean cyclic(List<?> cached, Object object) {
		Iterator<?> it = cached.iterator();
		while (it.hasNext()) {
			Object existed = it.next();
			if (object == existed) {
				return true;
			}
		}
		return false;
	}

	private void printObject(Object object, List<Object> cached, Writer out)
			throws IOException {
		out.write("{");
		BeanInfo info;
		boolean addedSomething = false;
		try {
			info = Introspector.getBeanInfo(object.getClass());
			PropertyDescriptor[] props = info.getPropertyDescriptors();
			for (int i = 0; i < props.length; ++i) {
				PropertyDescriptor prop = props[i];
				String name = prop.getName();
				Method accessor = prop.getReadMethod();
				if ((emitClassName == true || !"class".equals(name))
						&& accessor != null) {
					if (!accessor.isAccessible()) {
						accessor.setAccessible(true);
					}
					Object value = accessor.invoke(object, (Object[]) null);
					if (addedSomething) {
						out.write(',');
					}
					print(name, out);
					out.write(':');
					print(value, cached, out);
					addedSomething = true;
				}
			}
			Field[] ff = object.getClass().getFields();
			for (int i = 0; i < ff.length; ++i) {
				Field field = ff[i];
				if (addedSomething)
					out.write(',');
				print(field.getName(), out);
				out.write(':');
				print(field.get(object), cached, out);
				addedSomething = true;
			}
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
		} catch (InvocationTargetException ite) {
			ite.getCause().printStackTrace();
			ite.printStackTrace();
		} catch (IntrospectionException ie) {
			ie.printStackTrace();
		}
		out.write("}");
	}

	private void print(Map<?, ?> map, List<Object> cached, Writer out)
			throws IOException {
		out.write("{");
		Iterator<?> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) it.next();
			print(String.valueOf(e.getKey()), out);
			out.write(":");
			print(e.getValue(), cached, out);
			if (it.hasNext())
				out.write(',');
		}
		out.write("}");
	}

	private void print(Iterator<?> it, List<Object> cached, Writer out)
			throws IOException {
		out.write("[");
		while (it.hasNext()) {
			print(it.next(), cached, out);
			if (it.hasNext())
				out.write(",");
		}
		out.write("]");
	}

	private void print(Object[] object, List<Object> cached, Writer out)
			throws IOException {
		out.write("[");
		for (int i = 0; i < object.length; ++i) {
			if (i > 0) {
				out.write(',');
			}
			print(object[i], cached, out);
		}
		out.write("]");
	}

}
