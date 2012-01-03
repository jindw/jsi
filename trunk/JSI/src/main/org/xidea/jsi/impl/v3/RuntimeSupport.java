package org.xidea.jsi.impl.v3;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xidea.el.json.JSONDecoder;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.JSIRuntime;
import org.xidea.jsi.impl.v2.JSIText;

/**
 * @author test
 * 
 */
public abstract class RuntimeSupport implements JSIRuntime {
	private static final String ININT_SCRIPT = "this.window =this;this.print = this.print || function(msg){java.lang.System.out.print(String(msg))}";
	static final Object[] EMPTY_ARG = new Object[0];
	private static final Log log = LogFactory.getLog(RuntimeSupport.class);
	private static boolean testRhino = true;
	protected Object globals;
	protected JSIRoot root = new ClasspathRoot();
	protected int optimizationLevel = 0;

	public void setOptimizationLevel(int optimizationLevel) {
		this.optimizationLevel = optimizationLevel;
	}

	public void setRoot(JSIRoot root) {
		this.root = root;
	}

	public JSIRoot getRoot() {
		return root;
	}

	public Object getGlobals() {
		return this.globals;
	}

	protected String getFileInfo() {
		return "unknow";
	}


	protected static NodeList wrapNodeList(final NodeList javaObject) {
		return new NodeList() {
			public Node item(int index) {
				return javaObject.item(index);
			}

			public int getLength() {
				return javaObject.getLength();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIRuntime#eval(java.net.URL)
	 */
	public Object eval(URL resource) {
		if (resource == null) {
			return null;
		}
		try {
			String code = loadText(resource, "UTF-8");
			return this.eval(code, resource.toString());
		} catch (IOException e) {
			throw new RuntimeException("script load error:" + resource, e);
		}
	}

	public static String loadText(InputStream in, String encoding)
			throws IOException {
		if (in == null) {
			return null;
		}
		Reader reader = new InputStreamReader(in, encoding);
		StringBuilder buf = new StringBuilder();
		char[] cbuf = new char[1024];
		for (int len = reader.read(cbuf); len > 0; len = reader.read(cbuf)) {
			buf.append(cbuf, 0, len);
		}
		return buf.toString();
	}

	public static String loadText(URL resource, String encoding)
			throws IOException {
		if (resource == null) {
			return null;
		}
		InputStream in = resource.openStream();
		try {
			return JSIText.loadText(in,encoding);
		} finally {
			in.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIRuntime#eval(java.lang.String)
	 */
	public Object eval(String source) {
		String path = source;
		if (path.length() > 256) {
			path = path.replaceFirst("^/\\*[\\s\\S]*?\\*/", "");
			if (path.length() > 256) {
				path = path.substring(0, 256) + "...";
			}
		}
		return this.eval(null, source, "source:" + path, (Object) null);
	}

	public Object eval(String source, String path) {
		return this.eval(null, source, path, (Object) null);
	}

	public Object eval(String source, String path, Object scope) {
		return this.eval(null, source, path, (Object) scope);
	}

	public abstract Object eval(Object thisObj, String source, String path,
			Object scope);

	@SuppressWarnings("unchecked")
	public <T> T wrapToJava(final Object thiz, Class<T> clasz) {

		return (T) Proxy.newProxyInstance(
				RuntimeSupport.class.getClassLoader(), new Class[] { clasz },
				new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						Class<?> returnType = method.getReturnType();
						Object rtv = invokeJavaMethod(thiz, method.getName(),
								returnType, args);
						return rtv;
					}

				});
	}

	protected Object invokeJavaMethod(Object thiz, String name,
			Class<? extends Object> type, Object[] args)
			throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Object result = invoke(thiz, name, args);
		if (type == Void.TYPE) {
			return null;
		} else if (result == null && type.isPrimitive()) {
			if (Boolean.TYPE == type) {
				return false;
			} else if (Character.TYPE == type) {
				return '\0';
			}
			result = 0;
		}
		try {
			return jsToJava(type, result);
		} catch (Exception e) {
			if (result != null) {
				try {
					Constructor<? extends Object> cons = type
							.getConstructor(String.class);
					String code = (String) this.eval(result,
							"return String(this)", "<inline>", null);
					return cons.newInstance(code);
				} catch (SecurityException e1) {
				} catch (NoSuchMethodException e1) {
				}
				String code = (String) this
						.eval(
								result,
								"return JSON.stringify(this)",
								"<inline>", null);
				result = JSONDecoder.decode(code);
				if (result instanceof List<?>
						&& Set.class.isAssignableFrom(type)) {

					result = new HashSet<Object>((List<?>) result);
				}
			}
			return result;
		}
	}

	protected abstract Object jsToJava(Class<? extends Object> type,
			Object result);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIRuntime#invoke(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	public abstract Object invoke(Object thisObj, Object function,
			Object... args);

	/**
	 * 按类路径装载文本（utf-8） 如：org/xidea/jsi/impl/initialize.js
	 * 
	 * @param path
	 *            类路径
	 * @return
	 */
	public String loadText(String path) {
		return root.loadText(path);
	}

	public static JSIRuntime create() {
		RuntimeSupport sp = null;
		try {
			if (testRhino) {
				sp = RhinoImpl.create(true);
			}
		} catch (NoClassDefFoundError e) {
			testRhino = false;
		}
		if (sp == null) {
			// sp = Java6Impl.create(true);
		}
		sp.initialize();
		return sp;
	}

	void initialize() {
		try {
			try {
				this.eval(ININT_SCRIPT);
				ScriptDocument.bind(this);
				Console.bind(this);
				this.eval(this.loadText("require.js"),"require.js");
				
			} finally {
			}
			if ((Boolean) this.eval("!($JSI && $export)")) {
				log.error("JSI 加载失败");
			}
		} catch (Exception e) {
			log.error("尝试JSI启动编译脚本失败", e);
			throw new RuntimeException(e);
		}
	}
}
