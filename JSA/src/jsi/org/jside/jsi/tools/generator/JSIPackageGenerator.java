package org.jside.jsi.tools.generator;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIDependence;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.impl.ResourceRoot;

public class JSIPackageGenerator extends ResourceRoot {
	private static final Log log = LogFactory.getLog(JSIPackageGenerator.class);
	static final String ADD_SCRIPT = "this.addScript('";
	static final String ADD_SCRIPT_SPACE = "\r\n"
			+ "this.addScript('".replaceAll(".", " ");

	private JSIPackage missedPackage = new MissedJSIPackage();
	private Map<String, Collection<String>> dependancePackageMap = new HashMap<String, Collection<String>>();
	private ResourceRoot base;

	public JSIPackageGenerator(ResourceRoot base) {
		this.base = base;
	}

	public void addPackageDependanceList(String newPackageName,
			String... dependancePackageNames) {
		List<String> dependence = new ArrayList<String>();
		dependence.add(newPackageName);
		dependence.addAll(Arrays.asList(dependancePackageNames));
		dependancePackageMap.put(newPackageName, dependence);
	}

	public String genPackageSource(String packageName) {
		JSIPackage packageObject = this.requirePackage(packageName);
		packageObject.initialize();
		StringBuilder buf = new StringBuilder();
		for (String fileName : packageObject.getScriptObjectMap().keySet()) {
			List<JSIDependence> deps = packageObject.getDependenceMap().get(
					fileName);
			buf.append(ADD_SCRIPT);
			buf.append(fileName);
			buf.append("',");
			Collection<String> data = JSIPackageGeneratorUnits
					.findScriptObjectNames(packageObject, fileName);
			if (data.isEmpty()) {
				buf.append("[]");
			} else {
				JSIPackageGeneratorUnits.appendObject(buf, data);
			}
			if (deps == null) {
				buf.append(")");
			} else {

				List<String> data1 = JSIPackageGeneratorUnits
						.refindDependenceList(packageObject, deps, fileName,
								false);
				List<String> data2 = JSIPackageGeneratorUnits
						.refindDependenceList(packageObject, deps, fileName,
								true);
				if (data1.isEmpty()) {
					if (!data2.isEmpty()) {
						buf.append(ADD_SCRIPT_SPACE);
						buf.append(",0");
					}
				} else {
					buf.append(ADD_SCRIPT_SPACE);
					buf.append(",");
					JSIPackageGeneratorUnits.appendObject(buf, data1);
				}
				if (!data2.isEmpty()) {
					buf.append(ADD_SCRIPT_SPACE);
					buf.append(",");
					JSIPackageGeneratorUnits.appendObject(buf, data2);
				}
				buf.append(")");
			}
			buf.append(";\r\n\r\n");

		}
		return buf.toString();
	}

//	public String genPackageJSON(String packageName) {
//		return null;
//	}

	public JSIPackage findPackage(String packageName, boolean findParent) {
		if (MissedJSIPackage.NAME.equals(packageName)) {
			return missedPackage;
		}
		if (dependancePackageMap.containsKey(packageName)) {
			if (packageMap.containsKey(packageName)) {
				return packageMap.get(packageName);
			}
			String source = this.loadText(packageName,
					JSIPackage.PACKAGE_FILE_NAME);
			JSIPackageResource pkg = null;
			if (source != null) {
				pkg = new JSIPackageResource(this, packageName);
				createPackageParser(pkg).setup(pkg);
			}
			packageMap.put(packageName, pkg);
			return pkg;
		}
		return super.findPackage(packageName, findParent);
	}

	public String loadText(final String packageName, String scriptName) {
		if (MissedJSIPackage.NAME.equals(packageName)) {
			return "";
		}
		String path = packageName.replace('.', '/') + '/' + scriptName;
		URL resource = base.getResource(path);
		File file = null;
		try {
			file = new File(resource.toURI());
		} catch (Exception e) {
			log.warn(e);
		}
		if (JSIPackage.PACKAGE_FILE_NAME.equals(scriptName)
				&& dependancePackageMap.containsKey(packageName)) {
			String source = base.loadText(packageName, scriptName);
			final StringBuilder buf = new StringBuilder();
			final String advisorSource = source == null?"":source;;
			if(file != null){
				file.getParentFile().listFiles(new FileFilter() {
					public boolean accept(File file) {
						String name = file.getName();
						if (name.endsWith(".js")) {
							if (!JSIPackage.PACKAGE_FILE_NAME.equals(name)
									&& file.isFile()
									&& advisorSource.indexOf(name) < 0) {
								buf.append(ADD_SCRIPT + name + "','*');");
							}
						}
						return false;
					}
				});
			}
			buf.append(advisorSource);
			return buf.toString();
		} else {
			return base.loadText(packageName, scriptName);
		}
	}

	public List<JSIDependence> findDependence(JSIPackageResource sourcePackage,
			Collection<String> externalVars, boolean isAfterLoad) {
		if (!externalVars.isEmpty()) {
			ArrayList<JSIPackage> packageList = new ArrayList<JSIPackage>();
			for (String packageName : dependancePackageMap.get(sourcePackage
					.getName())) {
				JSIPackage dest = findPackage(packageName, true);
				if (dest != null) {
					packageList.add(dest);
				}
			}
			return JSIPackageGeneratorUnits.findDependence(externalVars, this,
					packageList, isAfterLoad);
		}
		return null;
	}
}
