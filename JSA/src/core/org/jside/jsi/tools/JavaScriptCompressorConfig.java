package org.jside.jsi.tools;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class JavaScriptCompressorConfig {

	public static final Collection<String> DEFAULT_FEATURES;
	// public static final Collection<String> SYSTEM_FEATURES;
	public static final Collection<String> DEFAULT_DEBUG_CALLS;

	public static final String FEATURE_DEBUG = ":Debug";
	public static final String FEATURE_JSI_COL = "org.xidea.jsi:COL";
	// @Deprecated
	// public static final String FEATURE_JSI_LOG = "org.xidea.jsi.boot:$log";
	// public static final String FEATURE_JSI_TEMPLATE_COMPILE =
	// "org.jside.template:compile";

	@Deprecated
	public static final String DEBUG_CALL_LOG_TRACE = "$log.trace";
	@Deprecated
	public static final String DEBUG_CALL_LOG_DEBUG = "$log.debug";
	@Deprecated
	public static final String DEBUG_CALL_LOG_INFO = "$log.info";
	@Deprecated
	public static final String DEBUG_CALL_LOG_ERROR = "$log.error";
	@Deprecated
	public static final String DEBUG_CALL_LOG_FATAL = "$log.fatal";
	public static final int UNSAFE_GLOBAL = 1;
	public static final int UNSAFE_SCOPE = 2;

	static {
		DEFAULT_FEATURES = Collections.unmodifiableSet(new HashSet<String>(
				Arrays.asList(FEATURE_JSI_COL)));
		DEFAULT_DEBUG_CALLS = Collections.unmodifiableSet(new HashSet<String>(
				Arrays.asList(DEBUG_CALL_LOG_TRACE, DEBUG_CALL_LOG_DEBUG)));
		// SYSTEM_FEATURES = Collections.unmodifiableSet(new
		// HashSet<String>(Arrays
		// .asList(FEATURE_DEBUG,FEATURE_JSI_COL,
		// FEATURE_JSI_LOG,FEATURE_JSI_TEMPLATE_COMPILE)));
	}

	private boolean syntaxCompression = true;
	private boolean textCompression = false;
	private boolean trimBracket = false;
	private double ratioCondition = 0.9;
	private int sizeCondition = 20 * 1024;
	private boolean compatible = true;
	protected Collection<String> features = new HashSet<String>(DEFAULT_FEATURES);
	private Collection<String> debugCalls = new HashSet<String>(
			DEFAULT_DEBUG_CALLS);
	private String key = "ee258bb8af876af6b81021fe98e0af7c4,ff59665e0729f1c68427f37910f56e4";
	private String copyright = "Compressed by JSA(www.xidea.org)";
	private boolean escapeScriptTag = true;
	private boolean ascii = true;

	public JavaScriptCompressorConfig() {
	}

	public boolean containsFeature(String key) {
		if (features != null) {
			Iterator<String> e = features.iterator();
			while (e.hasNext()) {
				if (key.equalsIgnoreCase(String.valueOf(e.next()))) {
					return true;
				}
			}
		}
		return false;
	}

	public void setFeatures(Collection<String> featureFlags) {
		this.features = featureFlags;
	}

	public Collection<String> getDebugCalls() {
		return debugCalls;
	}

	public void setDebugCalls(Collection<String> debugCalls) {
		this.debugCalls = debugCalls;
	}

	public void setSyntaxCompression(boolean syntaxCompress) {
		this.syntaxCompression = syntaxCompress;
	}

	/**
	 * 是否启用语法压缩
	 * 
	 * @return
	 */
	public boolean isSyntaxCompression() {
		return this.syntaxCompression;
	}

	/**
	 * 是否剔除多余花括弧
	 * 
	 * @return
	 */
	public boolean isTrimBracket() {
		return trimBracket;
	}

	public void setTrimBracket(boolean trimBracket) {
		this.trimBracket = trimBracket;
	}

	public void setTextCompression(boolean textCompress) {
		this.textCompression = textCompress;
	}

	public boolean isTextCompression() {
		return this.textCompression;
	}

	public void setRatioCondition(double ratio) {
		this.ratioCondition = ratio;
	}

	/**
	 * 只有达到某一相对压缩比例时，才启用文本压缩，默认为0.6
	 * 
	 * @return
	 */
	public double getRatioCondition() {
		return this.ratioCondition;
	}

	public void setSizeCondition(int size) {
		this.sizeCondition = size;
	}

	/**
	 * 只有达到某一大小时，才启用文本压缩，默认为20k
	 * 
	 * @return
	 */
	public int getSizeCondition() {
		return sizeCondition;
	}

	public void setCompatible(boolean compatible) {
		this.compatible = compatible;
	}

	/**
	 * 文本压缩时，兼容老版本（IE）
	 * 
	 * @return
	 */
	public boolean isCompatible() {
		return this.compatible;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getVersion() {
		return "2.1Alpha";
	}

	public boolean isEscapeScriptTag() {
		return escapeScriptTag;
	}

	public void setEscapeScriptTag(boolean escapeScriptTag) {
		this.escapeScriptTag = escapeScriptTag;
	}

	public void setAscii(boolean ascii) {
		this.ascii = ascii;
	}

	public boolean isAscii() {
		return ascii;
	}
}
