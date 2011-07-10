package org.jside.jsi.tools.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.xidea.jsi.JSIDependence;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.ScriptLoader;
import org.xidea.jsi.impl.DefaultDependence;
import org.xidea.jsi.impl.DefaultLoadContext;

public class DependenceFinder {
	JSIRoot root;

	public DependenceFinder(JSIRoot root) {
		this.root = root;
	}

	public String find(String[] imports, String[] externalImport) {

		Collection<String> beforeLoad = new HashSet<String>();
		Collection<String> afterLoad = new HashSet<String>();

		List<ScriptLoader> importList = getLoaders(imports);

		List<ScriptLoader> externalList = getLoaders(externalImport);

		List<ScriptLoader> exportList = new ArrayList<ScriptLoader>(importList);
		exportList.removeAll(externalList);

		for (String path : imports) {

			JSIDependenceLoadContextProxy importContext = new JSIDependenceLoadContextProxy(
					externalList);
			root.$import(path, importContext);
			for (JSIDependence dep : importContext.dependenceInfos) {
				String item = getDependenceItem(dep,dep.getTargetPackage().equals(importContext.fistPackage));
				if (dep.isAfterLoad()) {
					afterLoad.add(item);
				} else {
					beforeLoad.add(item);
				}
			}
		}

		afterLoad.removeAll(beforeLoad);
		if (beforeLoad.isEmpty()) {
			if (afterLoad.isEmpty()) {
				return null;
			} else {
				return "0," + this.joinDependence(afterLoad);
			}
		} else {
			if (afterLoad.isEmpty()) {
				return joinDependence(beforeLoad);
			} else {
				return joinDependence(beforeLoad) + ','
						+ this.joinDependence(afterLoad);
			}
		}

	}

	private List<ScriptLoader> getLoaders(String[] externalImport) {
		if (externalImport == null) {
			return new ArrayList<ScriptLoader>();
		} else {
			DefaultLoadContext externalContext = new DefaultLoadContext();
			for (String path : externalImport) {
				root.$import(path, externalContext);
			}
			List<ScriptLoader> externalList = externalContext.getScriptList();
			return externalList;
		}
	}

	private String joinDependence(Collection<String> beforeLoad) {
		StringBuilder buf = new StringBuilder();
		for (String item : beforeLoad) {
			if (buf.length() == 0) {
				buf.append("['");
			} else {
				buf.append(",'");
			}
			buf.append(item);
			buf.append("'");
		}

		if (buf.length() > 0) {
			buf.append("]");
		} else {
			buf.append("[]");
		}
		return buf.toString();
	}

	private String getDependenceItem(JSIDependence dep, boolean samePackage) {
		if (samePackage) {
			if (dep.getTargetObjectName() == null) {
				return dep.getTargetFileName();
			} else {
				return dep.getTargetObjectName();
			}
		} else {
			if (dep.getTargetObjectName() == null) {
				return dep.getTargetPackage().getName().replace('.', '/') + "/"
						+ dep.getTargetFileName();
			} else {
				return dep.getTargetPackage().getName() + ":"
						+ dep.getTargetObjectName();
			}
		}
	}

	static class JSIDependenceLoadContextProxy extends DefaultLoadContext {
		private List<JSIDependence> dependenceInfos = new ArrayList<JSIDependence>();
		private ScriptLoader currentScriptLoader;
		private int depth = -1;
		private ArrayList<Boolean> afterLoads = new ArrayList<Boolean>();
		private List<ScriptLoader> externalList;
		private JSIPackage fistPackage;

		public JSIDependenceLoadContextProxy(List<ScriptLoader> externalList) {
			this.externalList = externalList;
		}

		private void setAfterLoad(boolean value) {
			if(depth== afterLoads.size()){
				afterLoads.add(value);
			}else{
				afterLoads.set(depth, value);
			}
		}

		private boolean isAfterLoad() {
			int i = depth - 1;
			while (i-- > 0) {
				if (!afterLoads.get(i)) {
					return false;
				}
			}
			return true;
		}

		public void loadScript(JSIPackage pkg, String path, String object,
				boolean export) {
			depth++;
			setAfterLoad(false);
			ScriptLoader currentScriptLoader = pkg.getLoaderMap().get(path);
			ScriptLoader parentScriptLoader = this.currentScriptLoader;
			if(parentScriptLoader == null){
				this.fistPackage = currentScriptLoader.getPackage();
			}
			if (externalList.contains(currentScriptLoader)) {
				this.dependenceInfos.add(new DefaultDependence(pkg, path,
						object, isAfterLoad()));
				// 跳过装载
				//TODO .外部依賴再次依賴內部，內部再次依賴外部，這是，這條路線沒有走到，有問題
				//但是，如果super.loadScript.也有問題，算了先放過吧，太複雜了
				//o ，不会的，外面的依赖及其相关依赖都会被剔除掉，不存在外面在回来的的问题！！
			} else {
				super.loadScript(pkg, path, object, export);
			}
			this.currentScriptLoader = parentScriptLoader;
		}

		protected void loadAfter(final String objectName,
				List<JSIDependence> list) {
			setAfterLoad(true);
			super.loadAfter(objectName, list);
		}
	}
}
