package org.xidea.jsi.impl;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class RhinoSupport {
	private static final Log log = LogFactory.getLog(RhinoSupport.class);
	protected Object globals;
	protected ResourceRoot root = new ResourceRoot();
	
	public void setRoot(ResourceRoot root) {
		this.root = root;
	}


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
	private static final Pattern PARAM = Pattern.compile("[?&]([^=&#]+)=([^&#]+)");

	/**
	 * 按类路径装载文本（utf-8） 如：org/xidea/jsi/impl/initialize.js
	 * @param path 类路径
	 * @return
	 */
	public String loadText(String path) {
		Matcher m = PARAM.matcher(path);
		String service = null;
		while(m.find()){
			String key = m.group(1);
			if("path".equals(key)){
				path = m.group(2);
			}else if("service".equals(key)){
				service = m.group(2);
			}
		}
		if("list".equals(service)){
			return this.list(path);
		}else{
			return root.getResourceAsString(path);
		}
	}
	private String list(String path) {
		List<String> result = root.getPackageFileList(path);
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
	

	public static RhinoSupport create() {
		RhinoSupport sp;
		try {
			sp = RhinoImpl.create(true);
		} catch (Exception e) {
			sp = Java6Impl.create(true);
		}
		try {
			sp.eval(sp.loadText("boot.js"));
		} catch (Exception e) {
			log.error("尝试JSI启动编译脚本失败", e);
			throw new RuntimeException(e);
		}
		return sp;
	}

	public static RhinoSupport create(Object topScope) {
		String cn = topScope.getClass().getName();
		RhinoSupport sp;
		if (cn.startsWith("com.sun.") || cn.startsWith("sun.")) {
			sp = Java6Impl.create(false);
		} else {
			sp = RhinoImpl.create(false);
		}
		sp.globals = topScope;
		return sp;
	}

	/**
	 * 1.初始化 freeEval，加上调试信息
	 * 2.$JSI.scriptBase 设置为 classpath:/// 
	 * 3.返回 loadTextByURL
	 * @param arguments
	 * @return
	 */
	public Object setup(Object arguments) {
		Object initializer = this.eval(RhinoSupport.class
				.getResource("setup.js"));
		return this.invoke(this, initializer, arguments);
	}

}
