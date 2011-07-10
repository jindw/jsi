package org.jside.jsi.tools.export;

import java.io.File;

import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.xidea.jsi.web.JSIService;

abstract class ExportBase extends JSIService {

	protected File scriptBase = new File(".");
	protected File[] sources = new File[0];
	protected File[] libs = sources;
	protected String encoding = "utf-8";
	protected String[] bootPackage;
	protected String[] exports;
	protected String[] bootCached;
	protected String lineSeparator = "\r\n";
	protected String internalPrefix = "__$";
	protected boolean preserve = false;

	/**
	 * 输出路径参数
	 */
	protected File outputExported;
	protected File outputJAR;
	protected File outputBoot;
	protected File outputPackage;
	protected File outputPreload;

	protected JavaScriptCompressorConfig config = new JavaScriptCompressorConfig();
	
	public File getScriptBase() {
		return scriptBase;
	}

	public void setScriptBase(File scriptBase){
		this.scriptBase = scriptBase;
	}

	public void setSources(File[] sourceBases){
		this.sources = sourceBases;
	}

	public void setLibs(File[] libs){
		this.libs = libs;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String[] getBootPackage() {
		return bootPackage;
	}

	public void setBootPackage(String[] bootPackage) {
		this.bootPackage = bootPackage;
	}

	public String[] getExports() {
		return exports;
	}

	public void setExports(String[] exports) {
		this.exports = exports;
	}

	public String[] getBootCached() {
		return bootCached;
	}

	public void setBootCached(String[] bootCached) {
		this.bootCached = bootCached;
	}

	public String getLineSeparator() {
		return lineSeparator;
	}

	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

	public String getInternalPrefix() {
		return internalPrefix;
	}

	public void setInternalPrefix(String internalPrefix) {
		this.internalPrefix = internalPrefix;
	}

	public boolean isPreserve() {
		return preserve;
	}

	public void setPreserve(boolean preserve) {
		this.preserve = preserve;
	}

	public File getOutputExported() {
		return outputExported;
	}

	public void setOutputExported(File outputExported) {
		this.outputExported = outputExported;
	}


	public File getOutputJAR() {
		return outputJAR;
	}

	public void setOutputJAR(File outputJAR) {
		this.outputJAR = outputJAR;
	}

	public File getOutputBoot() {
		return outputBoot;
	}

	public void setOutputBoot(File outputBoot) {
		this.outputBoot = outputBoot;
	}

	public File getOutputPreload() {
		return outputPreload;
	}

	public void setOutputPreload(File outputPreload) {
		this.outputPreload = outputPreload;
	}

	public File getOutputPackage() {
		return outputPackage;
	}

	public void setOutputPackage(File outputPackage) {
		this.outputPackage = outputPackage;
	}

	public JavaScriptCompressorConfig getConfig() {
		return config;
	}

	public void setConfig(JavaScriptCompressorConfig config) {
		this.config = config;
	}

}