package org.xidea.jsi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIRoot;

public class ClasspathJSIRoot extends AbstractJSIRoot implements JSIRoot {
	private static final Log log = LogFactory.getLog(ClasspathJSIRoot.class);
	private String encoding = "utf-8";
	private ClassLoader loader;

	public ClasspathJSIRoot() {
		this(null);
	}
	public ClasspathJSIRoot(String encoding) {
		this(null,encoding);
	}
	public ClasspathJSIRoot(ClassLoader loader,String encoding) {
		this.loader = loader == null?this.getClass().getClassLoader():loader;
		if(encoding!=null){
			this.encoding = encoding;
		}
	}

	public String loadText(String pkgName, String scriptName) {
		try {
			String path ;
			if(pkgName!=null&&pkgName.length()>0){
				path = pkgName.replace('.', '/')+'/'+scriptName;
			}else{
				path = scriptName;
			}
			return loadText(path);
		} catch (IOException e) {
			log.warn(e);;
			return null;
		}
	}
	public String loadText(String path) throws UnsupportedEncodingException,
			IOException {
		InputStream in = loader.getResourceAsStream(path); 
		if(in == null){
			return null;
		}
		Reader reader = new InputStreamReader(in,this.encoding);
		StringBuilder buf = new StringBuilder();
		char[] cbuf = new char[1024];
		for (int len = reader.read(cbuf); len > 0; len = reader.read(cbuf)) {
			buf.append(cbuf, 0, len);
		}
		return buf.toString();
	}

}
