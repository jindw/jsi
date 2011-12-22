package org.xidea.jsi.impl.v2;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.JSIRoot;

public class FileRoot extends AbstractRoot implements JSIRoot {
	private static final Pattern NS_PATTERN = Pattern.compile("^[a-zA-Z$_][\\w\\$_]*$");
	private static final Log log = LogFactory.getLog(FileRoot.class);
	private File scriptBase;
	private String encoding = "utf-8";

	public FileRoot(String scriptBase, String encoding) {
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
			log.warn(e);;
			return null;
		}
	}
	

	public static List<String> findPackageList(File root) {
		ArrayList<String> result = new ArrayList<String>();
		walkFileTree(root, result);
		return result;
	}
	private static void walkFileTree(File scriptBaseDirectory,final List<String> result) {
		scriptBaseDirectory.listFiles(new FileFilter(){
			private StringBuilder buf = new StringBuilder();
			public boolean accept(File file) {
				int len = buf.length();
				String name = file.getName();
				if(file.isDirectory()){
					if(NS_PATTERN.matcher(name).find()){
						if(len>0){
							buf.append('.');
						}
						buf.append(name);
						file.listFiles(this);
					}
				}else{
					if(name.equals(JSIPackage.PACKAGE_FILE_NAME)){
						result.add(buf.toString());
					}
				}
				buf.setLength(len);
				return false;
			}
			
		});
	}
}
