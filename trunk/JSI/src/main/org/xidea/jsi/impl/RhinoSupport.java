package org.xidea.jsi.impl;

import java.io.IOException;





public class RhinoSupport {
	static final String EVAL = "(function(code){return Packages."+RhinoSupport.class.getName()+".buildInnerEvaler(this,String(this.scriptBase)+this.name).call(this,code);})";
	static final String EVALER = "(function(x){return eval(x)})";
	public static String loadText(String path) throws IOException{
		return ClasspathRoot.loadText(path,ClasspathRoot.class.getClassLoader(),"utf-8");
	}

	public static Object buildEvaler(Object thiz){
		RhinoSupport s = get(thiz);
		return s.eval(EVAL,"<new_freeEval>");
	}
	/**
	 * @internal
	 * @param thiz
	 * @param path
	 * @return
	 */
	public static Object buildInnerEvaler(Object thiz,String path){
		RhinoSupport s = get(thiz);
		return s.eval(EVALER,path);
	}
	private static RhinoSupport get(Object thiz){
		if(thiz.getClass().getName().startsWith("sun.")){
			return new Java6Impl();
		}else{
			return new RhinoImpl();
		}
	}
	public Object eval(String code,String path){
		return null;
	}
}
class Java6Impl extends RhinoSupport{
	//function(code){return evaler(this,code);}
	public Object eval(String code,String path){
		sun.org.mozilla.javascript.internal.Context cx = sun.org.mozilla.javascript.internal.Context.getCurrentContext();
		return cx.evaluateString(
				sun.org.mozilla.javascript.internal.ScriptRuntime.getTopCallScope(cx), code, path, 1, null);

	}
}
class RhinoImpl extends RhinoSupport{
	//function(code){return evaler(this,code);}
	public Object eval(String code,String path){
		org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.getCurrentContext();
		return cx.evaluateString(
				org.mozilla.javascript.ScriptRuntime.getTopCallScope(cx), code, path, 1, null);
	}
}