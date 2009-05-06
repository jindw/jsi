package org.xidea.jsi.web;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.ScriptLoader;
import org.xidea.jsi.impl.DefaultLoadContext;
import org.xidea.jsi.impl.JSIText;

public class JSICDNService{
	private JSIRoot root;

	public JSICDNService(JSIRoot root){
		this.root = root;
	}

	public void service(String[] paths, Writer out) throws IOException {
		JSIRoot root = this.getJSIRoot();
		String result = doExport(root,paths);
		//应该考虑加上字节流缓存
		out.append(result);
		out.flush();
	}

	protected String doExport(JSIRoot root,String[] paths) {
		List<ScriptLoader> result = getScriptLoaders(root, paths);
		StringBuilder out = new StringBuilder();
		out.append(root.loadText(null, "boot.js"));
		out.append(getPreloadScript(result));
		for(String path : paths){
			out.append("$import('");
			out.append(path);
			out.append("');");
		}
		return out.toString();
	}

	protected String getPreloadScript(List<ScriptLoader> result) {
		StringBuilder out = new StringBuilder();
		for (ScriptLoader loader : result) {
			String fileName = loader.getName();
			String packageName = loader.getPackage().getName();
			String source = loader.getPackage().loadText(loader.getName());
			out.append(JSIText.buildPreloadPerfix(packageName, fileName));
			out.append(source);
			out.append(JSIText.buildPreloadPostfix(source));
		}
		return out.toString();
	}

	protected List<ScriptLoader> getScriptLoaders(JSIRoot root, String[] paths) {
		JSILoadContext context = new DefaultLoadContext();
		for(String path : paths){
			root.$import(path,context);
		}
		List<ScriptLoader> result = context.getScriptList();
		return result;
	}

	protected JSIRoot getJSIRoot() {
		return root;
	}
}
