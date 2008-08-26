package org.xidea.jsi.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.xidea.jsi.JSIPackage;
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

	public static List<String> findPackageList(File root) {
		ArrayList<String> result = new ArrayList<String>();
		walkPackageTree(root, null, result);
		return result;

	}

	private static void walkPackageTree(final File dir, String prefix,
			final List<String> result) {
		final String subPrefix;
		if (prefix == null) {
			subPrefix = "";
		} else if (prefix.length() == 0) {
			subPrefix = dir.getName();
		} else {
			subPrefix = prefix + '.' + dir.getName();
		}
		File packageFile = new File(dir, JSIPackage.PACKAGE_FILE_NAME);
		if (packageFile.exists()) {
			result.add(subPrefix);
		}
		dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (file.isDirectory()) {
					String name = file.getName();
					if (!name.startsWith(".")) {
						walkPackageTree(file, subPrefix, result);
					}
				}
				return false;
			}
		});

	}
}
