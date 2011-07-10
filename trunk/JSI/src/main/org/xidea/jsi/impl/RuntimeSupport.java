package org.xidea.jsi.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
//import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xidea.el.json.JSONDecoder;
import org.xidea.jsi.JSIRuntime;

/**
 * @see org.xidea.jsi.impl.Java6Impl
 * @author test
 * 
 */
public abstract class RuntimeSupport implements JSIRuntime {
	private static final String ININT_SCRIPT = "this.window =this;this.print = this.print || function(msg){java.lang.System.out.print(String(msg))}";
	static final Object[] EMPTY_ARG = new Object[0];
	private static final Log log = LogFactory.getLog(RuntimeSupport.class);
	protected Object globals;
	protected ResourceRoot root = new ResourceRoot();
	protected int optimizationLevel = 0;

	public void setOptimizationLevel(int optimizationLevel){
		this.optimizationLevel  = optimizationLevel;
	}
	public void setRoot(ResourceRoot root) {
		this.root = root;
	}

	public ResourceRoot getRoot() {
		return root;
	}

	protected String getFileInfo() {
		return "unknow";
	}

	private static final Pattern LOG_FILE = Pattern
			.compile("^classpath\\:[/]+org/xidea/jsi/log.js$");

	// "trace,debug,info,warn,error,fatal"
	public boolean log(String title,int level, String msg) {
		List<String> jsName = getFileStack();
		msg += "[scriptName]:" + jsName;
		Log log = LogFactory.getLog(title);
		switch (level) {
		case 0:
			log.trace(msg);
			break;
		case 1:
			log.debug(msg);
			break;
		case 2:
			log.info(msg);
			break;
		case 3:
			log.warn(msg);
			break;
		case 4:
			log.error(msg);
			break;
		case 5:
			log.fatal(msg);
			break;
		default:
			log.info(msg);
		}
		return true;
	}
	private List<String> getFileStack() {
		String firstFile = null;
		ArrayList<String> jsName = new ArrayList<String>();
		for (StackTraceElement s : new Exception().getStackTrace()) {
			String fileName = s.getFileName();
			if (fileName != null && !fileName.endsWith(".java")) {
				if (firstFile == null) {
					firstFile = fileName;
				}
				if (!LOG_FILE.matcher(firstFile).find()
						|| !firstFile.equals(fileName)) {
					int pos = jsName.size() - 1;
					int line = s.getLineNumber();
					if(pos >=0 ){
						String file = jsName.get(pos);
						if(file.startsWith(fileName)){
							if(file.equals(fileName+"@-1")){
								jsName.remove(pos);
							}else{
								if(line == -1){
									continue;
								}
							}
						}
					}
					if(log.isDebugEnabled() || jsName.size()<3){
						jsName.add(fileName + '@' + line);
					}
				}
			}
		}
		return jsName;
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
			String code = JSIText.loadText(resource, "UTF-8");
			return this.eval(code, resource.toString());
		} catch (IOException e) {
			throw new RuntimeException("script load error:"+resource,e);
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
		return this.eval(null, source, "source:" + path, (Object)null);
	}

	public Object eval(String source, String path) {
		return this.eval(null, source, path, (Object)null);
	}

	public Object eval(String source, String path,Object scope) {
		return this.eval(null, source, path, (Object)scope);
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
			Class<? extends Object> type, Object[] args) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
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
					Constructor<? extends Object> cons = type.getConstructor(String.class);
					String code = (String) this.eval(result,
							"return String(this)","<inline>", null);
					return cons.newInstance(code);
				} catch (SecurityException e1) {
				} catch (NoSuchMethodException e1) {
				}
				String code = (String) this
						.eval(
								result,
								"return $import('org.xidea.jsidoc.util:JSON').stringify(this)",
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

	private static boolean testRhino = true;

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
			sp = Java6Impl.create(true);
		}
		sp.initialize();
		return sp;
	}

	private static ThreadLocal<RuntimeSupport> IMPL = new ThreadLocal<RuntimeSupport>();

	void initialize() {
		try {
			try {
				IMPL.set(this);
				this.eval(ININT_SCRIPT);
				this.eval(this.root.getResource("boot.js"));
			} finally {
				IMPL.remove();
			}
			if ((Boolean) this.eval("!($JSI && $import)")) {
				log.error("JSI 加载失败");
			}
		} catch (Exception e) {
			log.error("尝试JSI启动编译脚本失败", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 在当前执行上下文中创建执行环境 JSI 内部预留方法。
	 * 
	 * @param topScope
	 * @private
	 * @return
	 */
	public static JSIRuntime create(Object topScope) {
		RuntimeSupport sp = IMPL.get();
		if (sp != null) {
			sp.eval(ININT_SCRIPT);
			return sp;
		}
		String cn = topScope.getClass().getName();
		if (cn.startsWith("com.sun.") || cn.startsWith("sun.")) {
			sp = Java6Impl.create(false);
		} else {
			sp = RhinoImpl.create(false);
		}
		sp.globals = topScope;
		sp.eval(ININT_SCRIPT);
		return sp;
	}
}
