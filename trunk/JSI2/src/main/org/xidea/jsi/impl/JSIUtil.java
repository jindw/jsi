package org.xidea.jsi.impl;

import org.xidea.jsi.JSIExportorFactory;

public abstract class JSIUtil {

	public static final String PRELOAD_CONTENT_POSTFIX = "\n}";
	public static final String PRELOAD_CONTENT_PREFIX = "function(){eval(this.varText);";
	public static final String PRELOAD_FILE_POSTFIX = "__preload__.js";
	public static final String PRELOAD_POSTFIX = ")";
	public static final String PRELOAD_PREFIX = "$JSI.preload(";

	public final static String buildPreloadPerfix(String path) {
		String pkg = path.substring(0, path.lastIndexOf('/')).replace('/', '.');
		String file = path.substring(pkg.length() + 1);
		StringBuffer buf = new StringBuffer();
		buf.append(JSIUtil.PRELOAD_PREFIX);
		buf.append("'" + pkg + "',");
		buf.append("'" + file + "',");
		buf.append(JSIUtil.PRELOAD_CONTENT_PREFIX);
		return buf.toString();
	}

	public static String buildPreloadPostfix() {
		return (PRELOAD_CONTENT_POSTFIX + PRELOAD_POSTFIX);
	}

	private static JSIExportorFactory exportorFactory;
	public final static String JSI_EXPORTOR_FACTORY_CLASS = "org.jside.jsi.tools.JSAExportorFactory";

	public static JSIExportorFactory getExportorFactory() {
		if (exportorFactory == null) {
			try{
				exportorFactory = (JSIExportorFactory) Class.forName(JSI_EXPORTOR_FACTORY_CLASS).newInstance();
			}catch (Exception e) {
				exportorFactory = new DefaultJSIExportorFactory();
			}
		}
		return exportorFactory;
	}
	public static void main(String args[]) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		System.out.println(getExportorFactory().createExplorter("", 0, "", true));
		System.out.println(exportorFactory = (JSIExportorFactory) Class.forName(JSI_EXPORTOR_FACTORY_CLASS).newInstance());
	}

}
