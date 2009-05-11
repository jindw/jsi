package org.xidea.jsi.impl;

import java.io.IOException;





public class RhinoSupport {
	public static String loadText(String path) throws IOException{
		return ClasspathRoot.loadText(path,ClasspathRoot.class.getClassLoader(),"utf-8");
	}
	public static Object buildEvaler(Object thiz){
		if(thiz.getClass().getName().startsWith("sun.")){
			return Java6Impl.buildEvaler(thiz);
		}else{
			return RhinoImpl.buildEvaler(thiz);
		}
	}
}
class Java6Impl{

	//function(code){return evaler(this,code);}
	public static Object buildEvaler(Object thiz){
		sun.org.mozilla.javascript.internal.Scriptable sthiz = (sun.org.mozilla.javascript.internal.Scriptable) thiz;
		String path = String.valueOf(sthiz.get("scriptBase", sthiz));
		path += sthiz.get("name", sthiz);
		sun.org.mozilla.javascript.internal.Context cx = sun.org.mozilla.javascript.internal.Context.getCurrentContext();
		return (sun.org.mozilla.javascript.internal.Callable) cx.evaluateString(
				sun.org.mozilla.javascript.internal.ScriptRuntime.getTopCallScope(cx), "(function(x){return eval(x)})", path, 1, null);
	}
}
class RhinoImpl{
	//function(code){return evaler(this,code);}
	public static Object buildEvaler(Object thiz){
		org.mozilla.javascript.Scriptable sthiz = (org.mozilla.javascript.Scriptable) thiz;
		String path = String.valueOf(sthiz.get("scriptBase", sthiz));
		path += sthiz.get("name", sthiz);
		org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.getCurrentContext();
		return cx.evaluateString(
				org.mozilla.javascript.ScriptRuntime.getTopCallScope(cx), "(function(x){return eval(x)})", path, 1, null);
	}
}