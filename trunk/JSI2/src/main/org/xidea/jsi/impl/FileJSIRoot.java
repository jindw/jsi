package org.xidea.jsi.impl;

import java.io.File;
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
		pkgName = pkgName.replace('.', '/');
		File file = new File(new File(this.scriptBase, pkgName), scriptName);
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

	public static List<String> findPackageList(File root){
		ArrayList<String> result = new ArrayList<String>();
		walkPackageTree(root,null,result);
		return result;
		
	}
	private static void walkPackageTree(File dir, String prefix, List<String> result) {
		File[] files = dir.listFiles();
		if(prefix == null){
			prefix = "";
		}else if(prefix.length() == 0){
			prefix = dir.getName();
		}else{
			prefix = prefix+'.'+dir.getName();
		}
		for (int i = 0; i < files.length; i++) {
			dir = files[i];
			String name = dir.getName();
			if (dir.isDirectory()) {
				if(!name.startsWith(".")){
					walkPackageTree(dir, prefix, result);
				}
			}else if(JSIPackage.PACKAGE_FILE_NAME.equals(name)){
				result.add(prefix);
			}
		}
		
	}
}
