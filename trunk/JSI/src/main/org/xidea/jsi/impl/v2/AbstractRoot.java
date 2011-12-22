package org.xidea.jsi.impl.v2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.ScriptNotFoundException;

public abstract class AbstractRoot implements JSIRoot {

	protected Map<String, JSIPackage> packageMap = new HashMap<String, JSIPackage>();

	protected void reset(){
		packageMap.clear();
	}
	public JSILoadContext $export(String path) {
		return $export(path, new DefaultLoadContext());
	}

	public JSILoadContext $export(String  path, JSILoadContext context) {
		JSIPackage pkg = findPackageByPath( path);
		String fileNames  =  path.substring(pkg.getName().length() + 1);
		pkg = requirePackage(pkg.getName());
		if(fileNames.length() == 0){
			//package import
			return context;
		}
		if ("*".equals( fileNames)) {
			for (Iterator<String> it = pkg.getScriptObjectMap().keySet()
					.iterator(); it.hasNext();) {
				String fileName = it.next();
				context.loadScript(pkg, fileName, null, true);
			}
		} else if (pkg.getScriptObjectMap().get( fileNames) != null) {
			// file
			context.loadScript(pkg,  fileNames, null, true);
		} else {
			// object
			String script = pkg.getObjectScriptMap().get( fileNames);
			if (script != null) {
				context.loadScript(pkg, script,  fileNames, true);
			} else {
				throw new ScriptNotFoundException("无效脚本路径:" +  path);
			}
		}
		return context;
	}

	/**
	 * 不能返回null
	 * @see org.xidea.jsi.JSIRoot#requirePackage(java.lang.String, boolean)
	 */
	public JSIPackage requirePackage(String name) {
		JSIPackage pkg = findPackage(name, false);
		if(pkg == null){
			throw new ScriptNotFoundException("package not find :"+name);
		}else if (pkg.getImplementation() == null) {
			return pkg;
		} else {
			return this.requirePackage(pkg.getImplementation());
		}
	}

	/**
	 * 不能返回null
	 * @see org.xidea.jsi.JSIRoot#findPackageByPath(java.lang.String)
	 */
	public JSIPackage findPackageByPath(String path) {
		int splitPos = path.lastIndexOf('/');
		if (splitPos > 0) {
			path = path.substring(0, splitPos).replace('/', '.');
			JSIPackage pkg = findPackage(path, false);
			if (pkg != null) {
				return pkg;
			}
		} else {
			splitPos = path.indexOf(':');
			if (splitPos >= 0) {
				path = path.substring(0, splitPos);
				JSIPackage pkg =  findPackage(path, false);
				if (pkg != null) {
					return pkg;
				}
			} else {
				splitPos = path.length();
				while ((splitPos = path.lastIndexOf('.', splitPos)) > 0) {
					JSIPackage pkg = findPackage(path = path.substring(0,
							splitPos), true);
					if (pkg != null) {
						return pkg;
					}
				}
			}
		}
		throw new ScriptNotFoundException("package not find :"+path);
	}

	protected synchronized JSIPackage findPackage(String name, boolean findParent) {
		do {
			if (packageMap.containsKey(name)) {
				return packageMap.get(name);
			}
			String source = this.loadText(name, JSIPackage.PACKAGE_FILE_NAME);
			if (source != null) {
				JSIPackage pkg  = new DefaultPackage(this, name);
				createPackageParser(pkg).setup(pkg);
				packageMap.put(name, pkg);
				return pkg;
			}
		//} while ((name = name.replace("\\.?[^\\.]+$", "")).length() > 0);
		} while (findParent && (name = name.substring(0,Math.max(name.lastIndexOf('.'), 0))).length() > 0);
		return null;
	}

	protected PackageParser createPackageParser(JSIPackage pkg) {
		PackageParser parser;
		try {
			parser  = new RegexpPackagePaser(pkg);
		} catch (Exception e) {
			try {
				parser = new RhinoScriptPackagePaser(pkg);
			} catch (Throwable ex) {
				parser = new Java6ScriptPackagePaser(pkg);
			}
		}
		return parser;
	}

	public abstract String loadText(String pkgName, String scriptName);
}