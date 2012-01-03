package org.xidea.jsi.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.impl.v2.DefaultExportorFactory;
import org.xidea.jsi.impl.v2.DefaultLoadContext;
import org.xidea.jsi.impl.v2.JSIPackage;
import org.xidea.jsi.impl.v2.ResourceRoot;

class SDNService {
	public static final String SDN_DEBUG_TOKEN_NAME = "SDN_DEBUG";
	public static final Pattern CDN_PATH_SPLITER = Pattern
			.compile("(?:%20|%09|%0d|%0a|[^\\w\\/\\.\\-\\:\\*\\$])+");
	public static final Pattern SDN_DEBUG_PATTERN = Pattern
			.compile("\\b"+SDN_DEBUG_TOKEN_NAME + "=(1|true|TRUE)\\b");

	private ResourceRoot root;
	private Map<String, String> cachedMap = new WeakHashMap<String,String>();
	private Map<String, String[]> exportConfig = new HashMap<String, String[]>();
	private int ips;
	private long lastModified = -1;

	public SDNService(ResourceRoot root) {
		this.root = root;
		exportConfig.put("level", new String[] { "3" });
	}

	public void process(String service, String cookie, OutputStream out)
			throws IOException {
		if (cookie != null && SDN_DEBUG_PATTERN.matcher(cookie).find()){
			writeSDNDebug(service, out);
		} else {
			writeSDNRelease(service, out);
		}
	}

	protected void writeSDNRelease(String path, OutputStream out)
			throws IOException {
		String result = null;
		// 每次都清理一下吧，CPU足够强悍，反正这只是一个调试程序
		long t = root.getLastModified();
		if(lastModified != t){
			lastModified = t;
			cachedMap.clear();
		}
		
		if (cachedMap != null) {
			result = cachedMap.get(path);
		}
		if (result == null) {
			result = this.doReleaseExport(path);
			if (cachedMap != null) {
				cachedMap.put(path, result);
			}
		}
		out.write(result.getBytes(root.getEncoding()));
	}
	protected void writeSDNDebug(String path, OutputStream out)
			throws IOException {
		out.write(this.doDebugExport(path).getBytes(root.getEncoding()));
	}


	public Map<String,Object> queryExportInfo(String path) {
		String[] paths = CDN_PATH_SPLITER.split(path);
		HashMap<String, Object> result = new LinkedHashMap<String, Object>();
		HashMap<String, Object> packageMap = new LinkedHashMap<String, Object>();
		LinkedHashSet<String> objectSet = new LinkedHashSet<String>();
		LinkedHashSet<String> exactList = new LinkedHashSet<String>();
		LinkedHashSet<String> packageList = new LinkedHashSet<String>();
		for (String item : paths) {
			if (item.endsWith("*")) {
				String name = item.substring(0, item.length() - 2);
				name = root.requirePackage(name).getName();
				packageList.add(name);
			} else {
				JSIPackage pkg = root.findPackageByPath(item);
				String name = pkg.getName();
				String object = item.substring(item.length() + 1);
				name = root.requirePackage(name).getName();
				exactList.add(name + ":" + object);
				objectSet.add(object);
			}
		}
		for (String packageName : packageList) {
			JSIPackage pkg = root.requirePackage(packageName);
			ArrayList<String> value = new ArrayList<String>();
			packageMap.put(pkg.getName(), value);
			for (String object : pkg.getObjectScriptMap().keySet()) {
				if (!objectSet.contains(object)) {
					objectSet.add(object);
					value.add(object);
				}
			}
		}
		result.put("pathList", exactList);
		result.put("packageMap", packageMap);
		return result;

	}

	public String doReleaseExport(String path) {
		exportConfig.put("internalPrefix", new String[] { "$"
				+ Integer.toString(ips++, 32) });
		JSIExportor releaseExportor = DefaultExportorFactory.getInstance()
				.createExplorter(DefaultExportorFactory.TYPE_EXPORT_CONFUSE,exportConfig);

		return releaseExportor.export(buildLoadContext(CDN_PATH_SPLITER
				.split(path)));
	}

	public String doDebugExport(String path) {
		String[] paths = SDNService.CDN_PATH_SPLITER.split(path);
		StringBuilder out = new StringBuilder();
		String boot = root.loadText(null, "boot.js");
		//hack
		boot = boot.replaceFirst("=\\s*(\\$JSI\\.scriptBase)\\b", "=($1=$1.replace(/\\\\/export\\\\/\\$/,'/'))");
		out.append("if(!window.$JSI || !$JSI.scriptBase){");
		out.append(boot);
		out.append("\n}");

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
			root.$export(path, context);
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
