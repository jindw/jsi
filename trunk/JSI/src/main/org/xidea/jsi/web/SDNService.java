package org.xidea.jsi.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.DefaultExportorFactory;
import org.xidea.jsi.impl.DefaultLoadContext;

class SDNService {
	public static final String CDN_DEBUG_TOKEN_NAME = "CDN_DEBUG";
	protected Map<String, String> cachedMap;// = new WeakHashMap<String,
	private JSIRoot root;
	Map<String, String[]> exportConfig = new HashMap<String, String[]>();
	private int ips;
	
	public SDNService(JSIRoot root) {
		this.root = root;
		exportConfig.put("level", new String[] { "3" });
	}

	public void service(String path, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String[] paths = path.split("[^\\w\\/\\.\\-\\:\\*\\$]");
		// TODO:以后应该使用Stream，应该使用成熟的缓存系统
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain;charset=utf-8");
		String result;
		if (isDebug(request)) {
			result = this.doDebugExport(paths);
		} else {
			if (cachedMap == null) {
				result = this.doReleaseExport(paths);
			} else {
				result = cachedMap.get(path);
				if (result == null) {
					result = this.doReleaseExport(paths);
					cachedMap.put(path, result);
				}
			}
		}
		// 应该考虑加上字节流缓孄1�7
		out.append(result);
		out.flush();
	}

	public String doReleaseExport(String[] paths) {
		exportConfig.put("internalPrefix", new String[] { "$"+Integer.toString(ips++,32) });
		JSIExportor releaseExportor = DefaultExportorFactory.getInstance().createExplorter(
				exportConfig);
		
		return releaseExportor.export(buildLoadContext(paths));
	}

	public String doDebugExport(String[] paths) {
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
		for (String path : paths) {
			out.append("$import('");
			out.append(path);
			out.append("',true);");
		}
		return out.toString();
	}

	private boolean isDebug(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (CDN_DEBUG_TOKEN_NAME.equals(cookie.getName())) {
					String value = cookie.getValue();
					if(value.length() == 0){
						return false;
					}else if(value.equals("0")) {
						return false;
					}else if(value.equals("false")) {
						return false;
					}
					return true;
				}
			}
		}
		return false;
	}

	public PackageLoadContext buildLoadContext(String[] paths) {
		PackageLoadContext context = new PackageLoadContext();
		for (String path : paths) {
			root.$import(path, context);
		}
		return context;
	}

	static class PackageLoadContext extends DefaultLoadContext {
		private List<JSIPackage> packageList = new ArrayList<JSIPackage>();

		@Override
		public void loadScript(JSIPackage pkg, String path, String objectName,
				boolean export) {
			this.packageList.add(pkg);
			super.loadScript(pkg, path, objectName, export);
		}
	}

}
