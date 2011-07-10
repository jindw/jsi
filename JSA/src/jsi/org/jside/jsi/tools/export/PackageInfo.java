package org.jside.jsi.tools.export;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.AbstractRoot;

public class PackageInfo {
	JSIRootProxy proxy;

	public PackageInfo(JSIRoot root, List<String> allImported,boolean addInvalidImportPackage) {
		proxy = new JSIRootProxy(root,addInvalidImportPackage);
		for (String path : allImported) {
			proxy.$import(path);
		}
	}

	public Set<String> getPackageSet() {
		return proxy.getPackageSet();
	}

	public Set<String> getValidPackageSet() {
		return proxy.getValidPackageSet();
	}
}

class JSIRootProxy extends AbstractRoot {
	private JSIRoot base;
	private Set<String> packageSet = new HashSet<String>();
	private Set<String> validPackageSet = new HashSet<String>();
	private boolean addInvalidImportPackage;

	JSIRootProxy(JSIRoot base, boolean addInvalidImportPackage) {
		this.base = base;
		this.addInvalidImportPackage = addInvalidImportPackage;
	}

	@Override
	public String loadText(String packageName, String scriptName) {
		String result = base.loadText(packageName, scriptName);
		if (result != null) {
			packageSet.add(packageName);
		} else {
			validPackageSet.add(packageName);
		}
		return result;
	}

	@Override
	public JSIPackage requirePackage(String name) {
		JSIPackage pkg = super.requirePackage(name);
		if (addInvalidImportPackage) {
			String exactName = pkg.getName();
			Set<String> names = pkg.getObjectScriptMap().keySet();
			for (String object : names) {
				int p = object.indexOf('.');
				while (p > 0) {
					validPackageSet.add(name + "." + object.substring(0, p));
					validPackageSet.add(exactName + "."
							+ object.substring(0, p));
					p = object.indexOf('.', p+1);
				}
			}
		}
		return pkg;
	}

	public Set<String> getPackageSet() {
		return packageSet;
	}

	public Set<String> getValidPackageSet() {
		return validPackageSet;
	}
}