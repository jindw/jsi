package org.xidea.jsi.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xidea.jsi.JSIRoot;

public class FileJSIRoot extends AbstractJSIRoot implements JSIRoot {
	private File scriptBase;
	private String encoding = "utf-8";

	public FileJSIRoot(String scriptBase, String encoding) {
		this.scriptBase = new File(scriptBase);
		this.encoding = encoding;
	}

	public String loadText(String pkgName, String scriptName) {

		File file = this.scriptBase;
		if (pkgName != null && pkgName.length() > 0) {
			pkgName = pkgName.replace('.', '/');
			file = new File(file, pkgName);
		}
		file = new File(file, scriptName);
		try {
			Reader in = new InputStreamReader(new FileInputStream(file),
					this.encoding);
			StringBuilder buf = new StringBuilder();
			char[] cbuf = new char[1024];
			for (int len = in.read(cbuf); len > 0; len = in.read(cbuf)) {
				buf.append(cbuf, 0, len);
			}
			return buf.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
