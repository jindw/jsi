package org.xidea.jsi.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;





public abstract class RhinoSupport {

	static final String EVAL = loadText("org/xidea/jsi/impl/initialize.js");

	/**
	 * 1.初始化 freeEval，加上调试姓习
	 * 2.$JSI.scriptBase 设置为 classpath:///
	 * 3.返回 loadTextByURL
	 * @param arguments
	 * @return
	 */
	public static Object initialize(Object arguments){
		RhinoSupport s = get(arguments);
		Object initializer = s.eval(EVAL,"<setup>");
		return s.call(initializer, null, arguments);
	}
	public static String loadText(String path) {
		try {
			return ClasspathRoot.loadText(path,ClasspathRoot.class.getClassLoader(),"utf-8");
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @internal
	 * @param thiz
	 * @param path
	 * @return
	 */
	public static Object createEvaler(Object thiz,String path){
		RhinoSupport s = get(thiz);
		Object result =  s.eval("(function(){eval(arguments[0])})",path);
		return result;
	}
	private static RhinoSupport get(Object thiz){
		String cn = thiz.getClass().getName();
		if(cn.startsWith("com.sun.") || cn.startsWith("sun.")){
			return new Java6Impl();
		}else{
			return new RhinoImpl();
		}
	}
	public abstract Object eval(String code,String path);
	protected abstract Object call(Object function,Object thisObj,Object... args);
}
class Java6Impl extends RhinoSupport{
	//function(code){return evaler(this,code);}
	public Object call(Object function,Object thisObj,Object... args){
		sun.org.mozilla.javascript.internal.Context cx = sun.org.mozilla.javascript.internal.Context.getCurrentContext();
		return ((sun.org.mozilla.javascript.internal.Function)function).call(cx, 
				sun.org.mozilla.javascript.internal.ScriptRuntime.getTopCallScope(cx), 
				(sun.org.mozilla.javascript.internal.Scriptable)thisObj, args);
	}
	public Object eval(String code,String path){
		sun.org.mozilla.javascript.internal.Context cx = sun.org.mozilla.javascript.internal.Context.getCurrentContext();
		return cx.evaluateString(
				sun.org.mozilla.javascript.internal.ScriptRuntime.getTopCallScope(cx), code, path, 1, null);

	}
}
class RhinoImpl extends RhinoSupport{
	//function(code){return evaler(this,code);}
	public Object call(Object function,Object thisObj,Object... args){
		org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.getCurrentContext();
		return ((org.mozilla.javascript.Function)function).call(cx, 
				org.mozilla.javascript.ScriptRuntime.getTopCallScope(cx), 
				(org.mozilla.javascript.Scriptable)thisObj, args);
	}
	public Object eval(String code,String path){
		org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.getCurrentContext();
		return cx.evaluateString(
				org.mozilla.javascript.ScriptRuntime.getTopCallScope(cx), code, path, 1, null);
	}
}