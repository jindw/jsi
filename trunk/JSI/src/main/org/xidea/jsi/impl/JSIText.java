package org.xidea.jsi.impl;


import org.xidea.jsi.JSIPackage;

public abstract class JSIText {
	public static final String PRELOAD_FILE_POSTFIX = "__preload__.js";
	
	public static final String PRELOAD_PREFIX = "$JSI.preload(";
	public static final String PRELOAD_CONTENT_PREFIX = "eval(this.varText);";


	public final static String buildPreloadPerfix(String path) {
		String packageName = path.substring(0, path.lastIndexOf('/')).replace('/', '.');
		String fileName = path.substring(packageName.length() + 1);
		return buildPreloadPerfix(packageName, fileName);
	}
	public final static String buildPreloadPerfix(String packageName,String fileName) {
		if(JSIPackage.PACKAGE_FILE_NAME.equals(fileName)){
			fileName= "";
		}
		StringBuffer buf = new StringBuffer();
		buf.append(JSIText.PRELOAD_PREFIX);
		buf.append("'" + packageName + "',");
		buf.append("'" + fileName + "',function(){");
		if(fileName.length()>0){
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
