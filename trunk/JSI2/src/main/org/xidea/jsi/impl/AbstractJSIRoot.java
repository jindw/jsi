package org.xidea.jsi.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.JSIRoot;

public abstract class AbstractJSIRoot implements JSIRoot {

	private Map<String, JSIPackage> packageMap = new HashMap<String, JSIPackage>();

	public abstract String loadText(String pkgName, String scriptName);

	public JSILoadContext $import(String path) {
		return $import(path, new DefaultJSILoadContext());
	}

	public JSILoadContext $import(String path, JSILoadContext context) {
		JSIPackage pkg = findPackageByPath(path);
		path = path.substring(pkg.getName().length() + 1);
		if ("*".equals(path)) {
			for (Iterator<String> it = pkg.getScriptObjectMap().keySet()
					.iterator(); it.hasNext();) {
				String fileName = it.next();
				context.loadScript(pkg, fileName, null, true);
			}
		} else if (pkg.getScriptObjectMap().get(path) != null) {
			// file
			context.loadScript(pkg, path, null, true);
		} else {
			// object
			String script = pkg.getObjectScriptMap().get(path);
			if (script != null) {
				context.loadScript(pkg, script, path, true);
			} else {
				throw new RuntimeException("无效脚本路径:" + path);
			}
		}
		return context;
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

	private synchronized JSIPackage findPackage(String name, boolean exact) {
		do {
			if (packageMap.containsKey(name)) {
				return packageMap.get(name);
			}
			String source = this.loadText(name, JSIPackage.PACKAGE_FILE_NAME);
			JSIPackage pkg = null;
			if (source != null) {
				pkg = new DefaultJSIPackage(this, name);
				createPackageParser(pkg).setup(pkg);
			}
			packageMap.put(name, pkg);
			return pkg;
		} while ((name = name.replace("\\.?[^\\.]+$", "")).length() > 0);
	}

	private PackageParser createPackageParser(JSIPackage pkg) {
		PackageParser parser = new RegexpPackagePaser();
		try {
			parser.parse(pkg);
		} catch (Exception e) {
			try {
				parser = new RhinoScriptPackagePaser();
				parser.parse(pkg);
			} catch (Throwable ex) {
				parser = new Java6ScriptPackagePaser();
				parser.parse(pkg);
			}
		}
		return parser;
	}

}