package org.xidea.jsi.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.UnsupportedSyntaxException;

public abstract class PackageParser {
	public static final String SET_IMPLEMENTATION = "setImplementation";
	public static final String ADD_SCRIPT = "addScript";
	public static final String ADD_DEPENDENCE = "addDependence";
	
	protected List<List<Object>> addScriptCall = new ArrayList<List<Object>>();
	protected List<List<Object>> addDependenceCall = new ArrayList<List<Object>>();
	protected String implementation;

	public PackageParser() {
	}

	public abstract void parse(JSIPackage packageObject);

	public Collection<String> findGlobals(String scriptName,String pattern) {
		throw new UnsupportedSyntaxException("不支持包定义格式");
	}

	public void addScript(String scriptName, Object objectNames,
			Object beforeLoadDependences, Object afterLoadDependences) {
		if(objectNames instanceof String){
			String pattern = (String)objectNames;
			if(pattern.indexOf('*')>=0){
				objectNames = findGlobals(scriptName,pattern);
			}
		}
		addScriptCall.add(Arrays.asList(scriptName, objectNames,
				beforeLoadDependences, afterLoadDependences));

	}

	public void addDependence(String thisPath, Object targetPath,
			boolean afterLoad) {
		addDependenceCall.add(Arrays.asList(thisPath, targetPath, afterLoad));
	}

	private void checkStrings(Object object) {// check type...
		try {
			if (object instanceof Collection) {
				for (String o : (Collection<String>) object)
					;
				return;
			}
			object = (String) object;
		} catch (Exception ex) {
			throw new UnsupportedSyntaxException("非法参数：" + object);
		}
	}

	public void setImplementation(String implementation) {
		if (this.implementation == null) {
			this.implementation = implementation;
		} else {
			throw new RuntimeException("不能多次设置实现包");
		}
	}

	public void setup(JSIPackage pkg) {
		if (this.implementation != null) {
			pkg.setImplementation(implementation);
		} else {
			for (Iterator<List<Object>> it = addScriptCall.iterator(); it
					.hasNext();) {
				List<Object> item = it.next();
				pkg.addScript((String) item.get(0), item.get(1), item.get(2),
						item.get(3));
			}
			for (Iterator<List<Object>> it = addDependenceCall.iterator(); it
					.hasNext();) {
				List<Object> item = it.next();
				pkg.addDependence((String) item.get(0), item.get(1),
						(Boolean) item.get(2));
			}
		}
	}
}