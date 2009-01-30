package org.xidea.jsi.impl;


import org.xidea.jsi.JSIPackage;

public abstract class JSIText {
	public static final String PRELOAD_FILE_POSTFIX = "__preload__.js";
	
	public static final String PRELOAD_PREFIX = "$JSI.preload(";
	public static final String PRELOAD_CONTENT_PREFIX = "eval(this.varText);";


	public final static String buildPreloadPerfix(String path) {
		String pkg = path.substring(0, path.lastIndexOf('/')).replace('/', '.');
		String file = path.substring(pkg.length() + 1);
		if(JSIPackage.PACKAGE_FILE_NAME.equals(file)){
			file= "";
		}
		StringBuffer buf = new StringBuffer();
		buf.append(JSIText.PRELOAD_PREFIX);
		buf.append("'" + pkg + "',");
		buf.append("'" + file + "',function(){");
		if(file.length()>0){
			buf.append(JSIText.PRELOAD_CONTENT_PREFIX);
		}
		return buf.toString();
	}
	public static String buildPreloadPostfix(String content) {
		int pos1 = content.lastIndexOf("//");
		if(content.indexOf('\n',pos1)>0 || content.indexOf('\r',pos1)>0){
			return "\n})";
		}else{
			return "})";
		}
	}
}
