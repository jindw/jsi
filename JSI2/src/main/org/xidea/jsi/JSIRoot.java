package org.xidea.jsi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xidea.jsi.parser.RegexpPackagePaser;


public class JSIRoot {
	public static final String PACKAGE_FILE = "__package__.js";
	private File scriptBase;
	private String encoding = "utf-8";

	private Map<String, JSIPackage> packageMap = new HashMap<String, JSIPackage>();

	public JSIRoot(String scriptBase, String encoding) {
		this.scriptBase = new File(scriptBase);
		this.encoding = encoding;
	}
	public void $import(String path, JSILoadContext context) {
		JSIPackage pkg = findPackageByPath(path);
		path = path.substring(pkg.getName().length() + 1);
		if ("*".equals(path)) {
			for (Iterator<String> it = pkg.getScriptObjectMap().keySet()
					.iterator(); it.hasNext();) {
				String fileName = it.next();
				context.loadScript(pkg, fileName, null);
			}
		} else if (pkg.getScriptObjectMap().get(path) != null) {
			// file
			context.loadScript(pkg, path, null);
		} else {
			// object
			String script = pkg.getObjectScriptMap().get(path);
			if (script != null) {
				context.loadScript(pkg, script, path);
			} else {
				throw new RuntimeException("无效脚本路径:" + path);
			}
		}
	}

	public JSIPackage requirePackage(String name, boolean exact) {
		JSIPackage pkg = findPackage(name, exact);
		if (pkg.getImplementation() == null) {
			return pkg;
		} else {
			return this.requirePackage(pkg.getImplementation(), exact);
		}
	}

	public JSIPackage findPackageByPath(String path) {
		int splitPos = path.lastIndexOf('/');
		if (splitPos > 0) {
			path = path.substring(0, splitPos).replace('/', '.');
			return findPackage(path, true);
		} else {
			splitPos = path.indexOf(':');
			if (splitPos >= 0) {
				path = path.substring(0, splitPos);
				return findPackage(path, true);
			} else {
				splitPos = path.length();
				while ((splitPos = path.lastIndexOf('.', splitPos)) > 0) {
					JSIPackage pkg = findPackage(path = path.substring(0,
							splitPos), false);
					if (pkg != null) {
						return pkg;
					}
				}
				return null;
			}
		}

	}

	public void debug(String info) {
		System.err.println(info);
	}

	public void error(String info) {
		System.err.println(info);
	}
	
	
	
	
	protected String loadTextByPackageAndFile(String pkgName, String scriptName) {
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

	private synchronized JSIPackage findPackage(String name, boolean exact) {
		do {
			if (packageMap.containsKey(name)) {
				return packageMap.get(name);
			}
			String source = this.loadTextByPackageAndFile(name, PACKAGE_FILE);
			JSIPackage pkg = null;
			if (source != null) {
				pkg = new JSIPackage(this, name);
				new RegexpPackagePaser().parse(source,pkg);
			}
			packageMap.put(name, pkg);
			return pkg;
		} while ((name = name.replace("\\.?[^\\.]+$", "")).length() > 0);
	}


}
