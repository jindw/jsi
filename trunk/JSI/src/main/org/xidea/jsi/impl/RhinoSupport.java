package org.xidea.jsi.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.net.www.URLConnection;

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
		try {
			if(path.startsWith("/")){
				path = path.substring(1);
			}
			Enumeration<URL> res = loader.getResources(path);
			ArrayList<String> result = new ArrayList<String>();
			while(res.hasMoreElements()){
				URL item = res.nextElement();
				try {
					this.append(item,result);
				} catch (Exception e) {
					log.debug(e);
				}
			}
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
		} catch (IOException e) {
			return "]";
		}
	}
	
	private void append(URL item, final List<String> result) throws URISyntaxException,IOException {
		if(item.getProtocol().equals("file")){
			new File(item.toURI()).listFiles(new FileFilter() {
				public boolean accept(File file) {
					String name = file.getName();
					if(file.isFile() && name.endsWith(".js") && !result.contains(name)){
						result.add(name);
					}
					return false;
				}
			});
		}else if(item.getProtocol().equals("jar")){
			JarURLConnection jarCon = (JarURLConnection) item.openConnection();
			JarFile jarFile = jarCon.getJarFile();
			Enumeration<JarEntry> en = jarFile.entries();
			String name = jarCon.getJarEntry().getName();
			while (en.hasMoreElements()) {
				JarEntry jarEntry = (JarEntry) en.nextElement();
				String name2 = jarEntry.getName();
				if(name2.startsWith(name)){
					name2 = name2.substring(name.length());
					if(name2.indexOf('/')<0 && name2.endsWith(".js") && !result.contains(name2)){
						result.add(name2);
					}
				}
			}
			jarFile.close();
		}
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
