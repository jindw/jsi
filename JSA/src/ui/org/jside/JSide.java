package org.jside;

import java.io.File;
import java.util.List;

import org.jside.ui.DockUI;
import org.jside.ui.TrayUI;

public class JSide {
	private static JSideImpl impl;

	protected static JSideImpl getImpl() {
		return impl;
	}

	public static void addListener(JSideListener<? extends Object> listener) {
		getImpl().addListener(listener);
	}

	public static boolean fireEvent(Class<? extends JSideListener<? extends Object>> type, Object source) {
		return getImpl().fireEvent(type, source);
	}
	
	public static List<JSideListener<? extends Object>> removeAllListener(Class<? extends JSideListener<? extends Object>> type) {
		return getImpl().removeAllListener(type);
	}

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[] {// "org.jside.webserver.proxy.ProxyHandler",
					"org.jside.webserver.handler.JSFilterHandler",
					"org.jside.webserver.handler.PHPHandler",
					"org.jside.webserver.handler.SJSHandler",
					"org.jside.webserver.handler.JSIHandler",
					"org.jside.webserver.handler.LiteHandler",
					"org.jside.webserver.handler.VelocityHandler",
					"org.jside.jsi.tools.JSA",
					"org.jside.xtools.encoding.EncodingTransformer",
					//"org.jside.xtools.xml.XTransformer"
					};
		}
		DockUI.showDock();
		impl = new JSideImpl();
		impl.initialize(args);
		if (TrayUI.isSupported()) {
			TrayUI.getInstance();
		}
	}
	public static String getAttribute(String key) {
		return getImpl().getAttribute(key);
	}

	public static void setAttribute(String key,String value) {
		getImpl().setAttribute(key,value);
	}
	
	public static <T> T loadConfig(Class<T> cls,
			boolean create) {
		return loadConfig(new File(getHome(), cls.getName()),
					create ? cls : null);
	}

	public static <T> T loadConfig(File file, Class<T> cls) {
		return getImpl().loadConfig(file,cls);
	}
	
	public static <T> void saveConfig(Class<T> cls, T config) {
		String name = cls.getName();
		File file = new File(getHome(), name);
		saveConfig(file, config);
	}

	public static <T> void saveConfig(File file, T config) {
		getImpl().saveConfig(file, config);
	}

	public static String getHome() {
		return getImpl().getHome();
	}


}
