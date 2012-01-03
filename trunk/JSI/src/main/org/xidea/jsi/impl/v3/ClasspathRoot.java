package org.xidea.jsi.impl.v3;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIRoot;

public class ClasspathRoot implements JSIRoot {
	private static final Log log = LogFactory.getLog(ClasspathRoot.class);
	protected String encoding = "utf-8";
	protected ClassLoader loader;

	public ClasspathRoot() {
		this(null);
	}

	public ClasspathRoot(String encoding) {
		this(null, encoding);
	}

	public ClasspathRoot(ClassLoader loader, String encoding) {
		this.loader = loader == null ? this.getClass().getClassLoader()
				: loader;
		if (encoding != null) {
			this.encoding = encoding;
		}
	}

	public static String loadText(String path, ClassLoader loader,
			String encoding) {
		InputStream in = loader.getResourceAsStream(path);
		try {
			return loadTextAndClose(in, encoding);
		} catch (IOException e) {
			return null;
		}
	}

	public static String loadTextAndClose(InputStream in, String encoding)
			throws IOException {
		if (in == null) {
			return null;
		}
		try {
			Reader reader = new InputStreamReader(in, encoding);
			StringBuilder buf = new StringBuilder();
			char[] cbuf = new char[1024];
			for (int len = reader.read(cbuf); len > 0; len = reader.read(cbuf)) {
				buf.append(cbuf, 0, len);
			}
			return buf.toString();
		} finally {
			in.close();
		}
	}

	public String loadText(String absPath) {
		return loadText(absPath, loader, encoding);
	}

}
