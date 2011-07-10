package org.jside.jsi.tools.util;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.Parser;

public class JSAClassLoader extends ClassLoader {
	static String classPrefix = "sun.org.mozilla.javascript.internal.";
	//static String className = "org.jside.jsi.tools.AnalysisError";

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		if (classPrefix.equals(name) && name.endsWith("2")) {
			String path = '/' + (classPrefix+name.substring(classPrefix.length())).replace('.', '/') + ".class";

			try {
				InputStream in = JSAClassLoader.class.getResourceAsStream(path);
//				FileInputStream in = new FileInputStream(
//						"C:/Users/ut/workspace/JSA-CORE/classes" + path);// JSAClassLoader.class.getResourceAsStream(path);
				byte[] buf = new byte[1024*1024];
				int count = in.read(buf);
				//while ((count += in.read(buf)) >= 0)
					;
				Class<?> c = this.defineClass(name, buf, 0, count);
				if (resolve) {
					resolveClass(c);
				}
				return c;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return super.loadClass(name, resolve);
	}

	public static void main(String[] args) throws Exception {
		JSAClassLoader loader = new JSAClassLoader();
		Class<?> clazz = loader.loadClass(classPrefix + "TokenStream");
		System.out.println(clazz);
		clazz = loader.loadClass(classPrefix + "Parser2");
		Parser parser = (Parser) clazz.newInstance();
		System.out.println(parser);
		System.out.println(clazz);
	}

}
