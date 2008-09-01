package org.xidea.jsi;

public interface JSIExportorFactory {
	/**
	 * 创建简单的直接导出合并实现
	 * @return 简单的直接导出合并实现
	 */
	public abstract JSIExportor createSimpleExplorter();

	/**
	 * 创建混淆隔离支持的导出合并实现
	 * 若不支持返回 <b>null</b>
	 * @return 混淆隔离支持的导出合并实现
	 */
	public abstract JSIExportor createConfuseExplorter();

	/**
	 * 创建问题报告实现
	 * 若不支持返回 <b>null</b>
	 * @return
	 */
	public abstract JSIExportor createReportExplorter();
	/**
	 * 创建XML数据打包实现
	 * 若不支持返回 <b>null</b>
	 * @return
	 */
	public abstract JSIExportor createXMLExplorter();
	
	/**
	 * 创建JSIDoc导出器实现
	 * 若不支持返回 <b>null</b>
	 * @return
	 */
	public abstract JSIExportor createJSIDocExplorter();


}