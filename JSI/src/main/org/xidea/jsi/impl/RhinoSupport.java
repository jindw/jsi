package org.xidea.jsi.impl;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIRuntime;

/**
 * @see org.xidea.jsi.impl.Java6Impl
 * @author test
 * 
 */
public abstract class RhinoSupport implements JSIRuntime {
	private static final Log log = LogFactory.getLog(RhinoSupport.class);
	protected Object globals;
	protected ResourceRoot root = new ResourceRoot();

	public void setRoot(ResourceRoot root) {
		this.root = root;
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
			String code = JSIText.loadText(resource, "UTF-8");
			return this.eval(code, resource.toString(), null);
		} catch (IOException e) {
			throw new RuntimeException(e);
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
		return this.eval(source, "source:" + path, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIRuntime#eval(java.lang.String,
	 * java.lang.String, java.util.Map)
	 */
	public abstract Object eval(String code, String path,
			Map<String, Object> vars);

	@SuppressWarnings("unchecked")
	public <T> T wrapToJava(final Object thiz, Class<T> clasz) {

		return (T) Proxy.newProxyInstance(RhinoSupport.class.getClassLoader(),

		new Class[] { clasz }, new InvocationHandler() {

			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				return invokeJavaMethod(thiz, method.getName(), method
						.getReturnType(), args);
			}

		});
	}

	protected abstract Object invokeJavaMethod(Object thiz, String name,
			Class<? extends Object> type, Object[] args);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.impl.JSIRuntime#invoke(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	public abstract Object invoke(Object thisObj, Object function,
			Object... args);

	private static final Pattern PARAM = Pattern
			.compile("[?&]([^=&#]+)=([^&#]+)");

	/**
	 * 按类路径装载文本（utf-8） 如：org/xidea/jsi/impl/initialize.js
	 * 
	 * @param path
	 *            类路径
	 * @return
	 */
	public String loadText(String path) {
		Matcher m = PARAM.matcher(path);
		String service = null;
		while (m.find()) {
			String key = m.group(1);
			if ("path".equals(key)) {
				path = m.group(2);
			} else if ("service".equals(key)) {
				service = m.group(2);
			}
		}
		if ("list".equals(service)) {
			return this.list(path);
		} else {
			return root.getResourceAsString(path);
		}
	}

	private String list(String path) {
		List<String> result = root.getPackageFileList(path);
		StringBuilder buf = new StringBuilder("[");
		for (String name : result) {
			if (buf.length() > 1) {
				buf.append(",");
			}
			buf.append("\"");
			buf.append(name);
			buf.append("\"");
		}
		buf.append("]");
		return buf.toString();
	}

	private static ThreadLocal<RhinoSupport> IMPL = new ThreadLocal<RhinoSupport>();

	public static JSIRuntime create() {
		RhinoSupport sp;
		try {
			sp = RhinoImpl.create(true);
		} catch (Exception e) {
			sp = Java6Impl.create(true);
		}
		try {
			try {
				IMPL.set(sp);
				sp.eval(sp.root.getResource("boot.js"));
			} finally {
				IMPL.remove();
			}
			if ((Boolean) sp.eval("!($JSI && $import)")) {
				log.error("JSI 加载失败");
			}
		} catch (Exception e) {
			log.error("尝试JSI启动编译脚本失败", e);
			throw new RuntimeException(e);
		}
		return sp;
	}

	/**
	 * 在当前执行上下文中创建执行环境
	 * JSI 内部预留方法。
	 * @param topScope
	 * @private
	 * @return
	 */
	public static JSIRuntime create(Object topScope) {
		RhinoSupport sp = IMPL.get();
		if (sp != null) {
			return sp;
		}
		String cn = topScope.getClass().getName();
		if (cn.startsWith("com.sun.") || cn.startsWith("sun.")) {
			sp = Java6Impl.create(false);
		} else {
			sp = RhinoImpl.create(false);
		}
		sp.globals = topScope;
		return sp;
	}

	/**
	 * 1.初始化 freeEval，加上调试信息 2.$JSI.scriptBase 设置为 classpath:/// 3.返回
	 * loadTextByURL
	 * 
	 * @param arguments
	 * @return
	 */
	public Object setup(Object arguments) {
		Object initializer = this.eval(RhinoSupport.class
				.getResource("setup.js"));
		return this.invoke(this, initializer, arguments);
	}

}
