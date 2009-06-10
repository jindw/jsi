package org.xidea.jsi.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.DefaultExportorFactory;
import org.xidea.jsi.impl.DefaultLoadContext;

class JSIScriptExportor{
	private JSIRoot root;
	private JSIExportor releaseExportor;

	public JSIScriptExportor(JSIRoot root){
		this.root = root;
		Map<String, String[]> exportConfig=new HashMap<String, String[]>();
		exportConfig.put("level", new String[]{"3"});
		exportConfig.put("internalPrefix", new String[]{"$_$"});
		releaseExportor = DefaultExportorFactory.getInstance().createExplorter(exportConfig);
	}

	public String doReleaseExport(String[] paths) {
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
		for(String path : paths){
			out.append("$import('");
			out.append(path);
			out.append("',true);");
		}
		return out.toString();
	}

	public PackageLoadContext buildLoadContext(String[] paths) {
		PackageLoadContext context = new PackageLoadContext();
		for(String path : paths){
			root.$import(path,context);
		}
		return context;
	}
	static class PackageLoadContext extends DefaultLoadContext{
		private List<JSIPackage> packageList = new ArrayList<JSIPackage>();
		@Override
		public void loadScript(JSIPackage pkg, String path,
				String objectName, boolean export) {
			this.packageList.add(pkg);
			super.loadScript(pkg, path, objectName, export);
		}
	}
	

}
