package org.xidea.jsi;

import java.util.List;
import java.util.Map;


public interface JSILoadContext {

	/**
	 * 装载指定脚本
	 * @param pkg
	 * @param path
	 * @param object
	 * @param export
	 */
	public abstract void loadScript(JSIPackage pkg, final String path,
			final String object, final boolean export);

	/**
	 * 获取导出对象列表
	 * {objectName:objectPackage}
	 * @return
	 */
	public abstract Map<String, String> getExportMap();

	/**
	 * 获取导出文件清单
	 * 按照正确的可装载顺序列出
	 * @return
	 */
	public abstract List<ScriptLoader> getScriptList();

}