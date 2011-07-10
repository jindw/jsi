package org.jside.jsi.tools.generator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.xidea.jsi.JSIDependence;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.DefaultDependence;

class JSIPackageGeneratorUnits {
	private static final Pattern NS_PATTERN = Pattern.compile("^[a-zA-Z$_][\\w\\$_]*$");
	/**
	 * @see org.xidea.jsi.impl.FileRoot#findPackageList(File)
	 * @param dir
	 * @param prefix
	 * @param result
	 */
	static Map<String, Boolean>  getPackageFileExistMap(final File dir) {
		final Map<String, Boolean> result = new LinkedHashMap<String, Boolean>();
		dir.listFiles(new FileFilter() {
			private StringBuilder prefix = new StringBuilder();
			public boolean accept(File file) {
				int len = prefix.length();
				String fileName = file.getName();
				if(file.isDirectory()){
					if(NS_PATTERN.matcher(fileName).find()){
						if(len>0){
							prefix.append('.');
						}
						prefix.append(fileName);
						file.listFiles(this);
					}
				}else{
					if(fileName.equals(JSIPackage.PACKAGE_FILE_NAME)){
						result.put(prefix.toString(),Boolean.TRUE);
					}else if (!result.containsKey(fileName)
							&& fileName.length() > 0
							&& file.getName().endsWith(".js")) {
						result.put(prefix.toString(), Boolean.FALSE);
					}
				}
				prefix.setLength(len);
				return false;
			}
		});
		return result;
	}
	
	public static List<JSIDependence> optimizeDependenceList(
			JSIPackage thisPackage, List<JSIDependence> deps,
			boolean isAfterload) {
		Map<String, Set<String>> dependeceFileMap = new HashMap<String, Set<String>>();
		Map<String, JSIPackage> packageMap = new HashMap<String, JSIPackage>();
		for (JSIDependence item : deps) {
			if (item.isAfterLoad() == isAfterload) {
				packageMap.put(item.getTargetPackage().getName(), item
						.getTargetPackage());
				String path = item.getTargetPackage().getName() + ":"
						+ item.getTargetFileName();
				Set<String> objectNames = dependeceFileMap.get(path);
				if (objectNames == null) {
					objectNames = new HashSet<String>();
					dependeceFileMap.put(path, objectNames);
				} else if (objectNames.isEmpty()) {
					break;
				}
				String objectName = item.getTargetObjectName();
				if (objectName == null) {
					objectNames.clear();
				} else {
					objectNames.add(objectName);
				}
			}
		}
		List<JSIDependence> result = new ArrayList<JSIDependence>();
		for (String path : dependeceFileMap.keySet()) {
			String packageName = path.substring(0, path.lastIndexOf(':'));
			String fileName = path.substring(packageName.length() + 1);
			Set<String> objectNames = dependeceFileMap.get(path);
			if (objectNames.isEmpty()
					|| objectNames.size()>1 && containsAll(thisPackage, fileName, objectNames)) {
				result.add(new DefaultDependence(
						packageMap.get(packageName), fileName, null,
						isAfterload));
			} else {
				for (String objectName : objectNames) {
					result.add(new DefaultDependence(packageMap
							.get(packageName), fileName, objectName,
							isAfterload));
				}
			}
		}
		return result;
	}

	private static boolean containsAll(JSIPackage packageObject, String file,
			Set<String> objects) {
		return findScriptObjectNames(packageObject, file).equals(objects);

	}

	static Set<String> findScriptObjectNames(JSIPackage packageObject,
			String file) {
		Map<String, String> osm = packageObject.getObjectScriptMap();
		HashSet<String> objectNames = new HashSet<String>();

		for (String name : osm.keySet()) {
			if (osm.get(name).equals(file)) {
				objectNames.add(name);
			}
		}
		return objectNames;
	}

	static List<JSIDependence> findDependence(Collection<String> externalVars,
			JSIRoot root,List<JSIPackage> packageList, boolean isAfterLoad) {
		ArrayList<JSIDependence> list = new ArrayList<JSIDependence>();
		for (String target : externalVars) {
			boolean missed = true;
			for (JSIPackage targetPackage : packageList) {
				String file = targetPackage.getObjectScriptMap().get(target);
				if (file != null) {
					missed = false;
					list.add(new DefaultDependence(targetPackage, file,target,isAfterLoad));
					break;
				}
			}
			if (missed) {
				list.add(new DefaultDependence(root.requirePackage(MissedJSIPackage.NAME), MissedJSIPackage.FILE_NAME,target,isAfterLoad));
			}
		}
		return list;
	}
	
	
	
	

	static List<String> refindDependenceList(JSIPackage thisPackage,
			List<JSIDependence> deps, String fileName, boolean isAfterload) {
		List<String> result = new ArrayList<String>();
		deps = optimizeDependenceList(thisPackage, deps, isAfterload);
		for (JSIDependence item : deps) {
			JSIPackage targetPackage = item.getTargetPackage();
			String targetFileName = item.getTargetFileName();
			String targetObjectName = item.getTargetObjectName();

			if (targetPackage.getName().equals(thisPackage.getName())) {
				result.add(targetObjectName == null ? targetFileName
						: targetObjectName);
			} else {
				if (targetObjectName == null) {
					result.add(targetPackage.getName().replace('.', '/') + '/'
							+ targetFileName);
				} else {
					result
							.add(targetPackage.getName() + ':'
									+ targetObjectName);
				}
			}
		}
		
		return result;
	}

	static String loadText(Object resource, String encoding) {
		try {
			InputStream in;
			 if(resource ==null){
				 return null;
			 }else
			if (resource instanceof InputStream) {
				in = (InputStream) resource;
			} else{
				try{
				in = new FileInputStream((File) resource);
				}catch (Exception e) {
					return null;
				}
			}
			Reader reader = new InputStreamReader(in, encoding);
			StringBuilder buf = new StringBuilder();
			char[] cbuf = new char[1024];
			for (int len = reader.read(cbuf); len > 0; len = reader.read(cbuf)) {
				buf.append(cbuf, 0, len);
			}
			return buf.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	static void appendObject(StringBuilder buf, Collection<String> data) {
		boolean isFirst = true;
		if (data.size() == 1) {
			buf.append('\'');
			buf.append(data.iterator().next());
			buf.append('\'');
		} else {
			buf.append('[');
			for (String object : data) {
				if (isFirst) {
					isFirst = false;
				} else {
					buf.append(',');
				}
				buf.append('\'');
				buf.append(object);
				buf.append('\'');
			}
			buf.append(']');
		}
	}
}
