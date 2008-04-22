package org.xidea.jsi;

import java.util.List;
import java.util.Map;


public interface JSILoadContext {

	/**
	 * 直接合并
	 */
	public static final int JOIN_DIRECT = 0;
	/**
	 * 合并内部冲突
	 */
	public static final int JOIN_WITHOUT_INNER_CONFLICTION = 1;
	/**
	 * 合并全部冲突
	 */
	public static final int JOIN_WITHOUT_ALL_CONFLICTION = 2;

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
	 * 执行导出操作
	 * @param joinLevel
	 * @return
	 */
	public abstract String export(int joinLevel);

	/**
	 * 获取导出对象列表
	 * @return
	 */
	public abstract Map<String, String> getExportMap();

	/**
	 * 获取导出文件清单
	 * @return
	 */
	public abstract List<ScriptLoader> getScriptList();

}