package org.xidea.jsi.impl;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSIPackage;

public abstract class JSIUtil {
	private static Map<String, JSIExportor> exportorFactoryMap = new HashMap<String, JSIExportor>();
	private final static String JSI_EXPORTOR_FACTORY_CLASS = "org.jside.jsi.tools.export.JSAExportorFactory";

	public static final String PRELOAD_CONTENT_POSTFIX = "\n}";
	public static final String PRELOAD_CONTENT_PREFIX = "function(){eval(this.varText);";
	public static final String PRELOAD_FILE_POSTFIX = "__preload__.js";
	public static final String PRELOAD_POSTFIX = ")";
	public static final String PRELOAD_PREFIX = "$JSI.preload(";

	public final static String buildPreloadPerfix(String path) {
		String pkg = path.substring(0, path.lastIndexOf('/')).replace('/', '.');
		String file = path.substring(pkg.length() + 1);
		StringBuffer buf = new StringBuffer();
		buf.append(JSIUtil.PRELOAD_PREFIX);
		buf.append("'" + pkg + "',");
		buf.append("'" + file + "',");
		buf.append(JSIUtil.PRELOAD_CONTENT_PREFIX);
		return buf.toString();
	}

	public static String buildPreloadPostfix() {
		return (PRELOAD_CONTENT_POSTFIX + PRELOAD_POSTFIX);
	}

	public static JSIExportor getExportor(String type) {
		if (exportorFactoryMap.containsKey(type)) {
			return exportorFactoryMap.get(type);
		}
		DefaultJSIExportorFactory exportorFactory = null;
		try {
			exportorFactory = (DefaultJSIExportorFactory) Class.forName(
					JSI_EXPORTOR_FACTORY_CLASS).getConstructor(String.class)
					.newInstance(type);

		} catch (Exception e) {
			exportorFactory = new DefaultJSIExportorFactory(type);
		}
		JSIExportor exportor = exportorFactory.createExplorter();
		exportorFactoryMap.put(type, exportor);
		return exportor;
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
