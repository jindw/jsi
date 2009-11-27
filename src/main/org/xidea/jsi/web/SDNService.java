package org.xidea.jsi.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.xidea.el.json.JSONEncoder;
import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.DefaultExportorFactory;
import org.xidea.jsi.impl.DefaultLoadContext;

class SDNService {
	public static final String CDN_DEBUG_TOKEN_NAME = "CDN_DEBUG";
	public static final Pattern CDN_PATH_SPLITER = Pattern
			.compile("(?:%20|%09|%0d|%0a|[^\\w\\/\\.\\-\\:\\*\\$])+");
	private JSIRoot root;
	private Map<String, String[]> exportConfig = new HashMap<String, String[]>();
	private int ips;

	public SDNService(JSIRoot root) {
		this.root = root;
		exportConfig.put("level", new String[] { "3" });
	}

	public String queryExportInfo(String path) {
		String[] paths = CDN_PATH_SPLITER.split(path);
		HashMap<String, Object> result = new LinkedHashMap<String, Object>();
		HashMap<String, Object> packageMap = new LinkedHashMap<String, Object>();
		LinkedHashSet<String> objectSet = new LinkedHashSet<String>();
		LinkedHashSet<String> exactList = new LinkedHashSet<String>();
		LinkedHashSet<String> packageList = new LinkedHashSet<String>();
		for (String item:paths) {
			if(item.endsWith("*")){
				String name = item.substring(0,item.length()-2);
				name = root.requirePackage(name).getName();
				packageList.add(name);
			}else{
				JSIPackage pkg = root.findPackageByPath(item);
				String name = pkg.getName();
				String object = item.substring(item.length()+1);
				name = root.requirePackage(name).getName();
				exactList.add(name + ":"+object);
				objectSet.add(object);
			}
		}
		for (String packageName: packageList) {
			JSIPackage pkg = root.requirePackage(packageName);
			ArrayList<String> value = new ArrayList<String>();
			packageMap.put(pkg.getName(), value);
			for(String object : pkg.getObjectScriptMap().keySet()){
				if(!objectSet.contains(object)){
					objectSet.add(object);
					value.add(object);
				}
			}
		}
		result.put("pathList",exactList);
		result.put("packageMap",packageMap);
		return JSONEncoder.encode(result);

	}

	public String doReleaseExport(String path) {
		exportConfig.put("internalPrefix", new String[] { "$"
				+ Integer.toString(ips++, 32) });
		JSIExportor releaseExportor = DefaultExportorFactory.getInstance()
				.createExplorter(exportConfig);

		return releaseExportor.export(buildLoadContext(CDN_PATH_SPLITER
				.split(path)));
	}

	public String doDebugExport(String path) {
		String[] paths = SDNService.CDN_PATH_SPLITER.split(path);
		StringBuilder out = new StringBuilder();
		out.append(root.loadText(null, "boot.js"));

		PackageLoadContext context = buildLoadContext(paths);
		for (JSIPackage pkg : context.packageList) {
			out.append("$JSI.preload('");
			out.append(pkg.getName());
			out.append("','',function(){");
			out.append(pkg.loadText(JSIPackage.PACKAGE_FILE_NAME));
			out.append("});");
		}
		for (String ip : paths) {
			out.append("$import('");
			out.append(ip);
			out.append("',true);");
		}
		return out.toString();
	}

	public PackageLoadContext buildLoadContext(String[] paths) {
		PackageLoadContext context = new PackageLoadContext();
		for (String path : paths) {
			root.$import(path, context);
		}
		return context;
	}

	static class PackageLoadContext extends DefaultLoadContext {
		List<JSIPackage> packageList = new ArrayList<JSIPackage>();

		@Override
		public void loadScript(JSIPackage pkg, String path, String objectName,
				boolean export) {
			this.packageList.add(pkg);
			super.loadScript(pkg, path, objectName, export);
		}
	}

}
