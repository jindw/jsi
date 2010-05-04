package org.xidea.jsi.impl;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class RhinoSupport {
	private static final Log log = LogFactory.getLog(RhinoSupport.class);
	private ClassLoader loader = RhinoSupport.class.getClassLoader();
	private URL boot = loader.getResource("boot.js");

	protected Object topScope;

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

	public Object eval(String code) {
		return this.eval(code, "<code>", null);
	}

	public abstract Object eval(String code, String path,
			Map<String, Object> vars);

	protected abstract Object invoke(Object thisObj, Object function,
			Object... args);

	/**
	 * 按类路径装载文本（utf-8） 如：org/xidea/jsi/impl/initialize.js
	 * 
	 * @param path
	 *            类路径
	 * @return
	 */
	public String loadText(String path) {
		try {
			return ClasspathRoot.loadText(path, loader, "utf-8");
		} catch (IOException e) {
			return null;
		}
	}
	public String list(String path) {
		List<String> result = new ResourceRoot().getPackageFileList(path);
		StringBuilder buf = new StringBuilder("[");
		for (String name:result) {
			if(buf.length()>1){
				buf.append(",");
			}
			buf.append("\"");
			buf.append(name);
			buf.append("\"");
		}
		buf.append("]");
		return buf.toString();
	}
	

	public static RhinoSupport create(Object topScope) {
		String cn = topScope.getClass().getName();
		RhinoSupport sp;
		if (cn.startsWith("com.sun.") || cn.startsWith("sun.")) {
			sp = new Java6InternalImpl();
		} else {
			sp = new RhinoInternalImpl();
		}
		sp.topScope = topScope;
		return sp;
	}

	public static RhinoSupport create() {
		RhinoSupport sp;
		try {
			sp = new RhinoImpl();
		} catch (Exception e) {
			sp = new Java6Impl();
		}
		sp.topScope = sp.eval("this");
		sp.eval("window = this;");
		try {
			sp.eval(sp.boot);
		} catch (Exception e) {
			log.debug("尝试JSI启动编译脚本失败", e);
		}
		return sp;
	}

	/**
	 * 1.初始化 freeEval，加上调试姓习 2.$JSI.scriptBase 设置为 classpath:/// 3.返回
	 * loadTextByURL
	 * 
	 * @param arguments
	 * @return
	 */
	public static Object initialize(Object arguments, Object topScope) {
		RhinoSupport s = create(topScope);
		Object initializer = s.eval(RhinoSupport.class
				.getResource("initialize.js"));
		return s.invoke(s, initializer, arguments);
	}

}
