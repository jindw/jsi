package org.jside.jsi.tools.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class JavaScriptConstants {
	public static final Collection<String> ECMA_VARIBALES;
	public static final Collection<String> BROWSER_VARIBALES;
	public static final Collection<String> ALL_VARIBALES;
	public static final Collection<String> JSI_VARIBALES;
	public static final Collection<String> JAVA_VARIBALES;

	static {
		HashSet<String> attributes = new HashSet<String>();

		// ECMA
		attributes.addAll(Arrays.asList(new String[] { "Array", "Boolean",
				"Date", "Error", "Function", "Math", "Number", "Object",
				"RegExp", "String" }));
		attributes.addAll(Arrays.asList(new String[] { "undefined", "Infinity",
				"NaN", "decodeURI", "decodeURIComponent", "encodeURI",
				"encodeURIComponent", "RangeError", "ReferenceError",
				"SyntaxError", "TypeError", "URIError" }));
		attributes.addAll(Arrays.asList(new String[] { "escape", "eval","uneval",
				"isFinite", "isNaN", "parseFloat", "parseInt", "unescape" }));

		// [, ObjectRange, , , , depth, , , , key, ]
		ECMA_VARIBALES = attributes;

		attributes = new HashSet<String>();
		String[] windowAttribute = {
				"window",
				// 属性
				"closed",
				"defaultStatus",
				"dialogArguments",
				"dialogHeight",
				"dialogLeft",
				"dialogTop",
				"dialogWidth",
				"frameElement",
				"length",
				"name",
				"offscreenBuffering",
				"opener",
				"parent",
				"returnValue",
				"screenLeft",
				"screenTop",
				"self",
				"status",
				"top",
				// 集合
				"frames",
				// 事件
				// 方法
				"alert", "attachEvent", "blur", "clearInterval",
				"clearTimeout", "close", "confirm", "createPopup",
				"detachEvent", "execScript", "focus", "moveBy", "moveTo",
				"navigate", "open", "print", "prompt", "resizeBy", "resizeTo",
				"scroll", "scrollBy", "scrollTo", "setActive", "setInterval",
				"setTimeout", "showHelp", "showModalDialog",
				"showModelessDialog",
				// 对象
				"clientInformation", "clipboardData", "document", "event",
				"external", "history", "location", "navigator", "screen", };
		attributes.addAll(Arrays.asList(windowAttribute));

		attributes.addAll(Arrays.asList(new String[] { "XMLHttpRequest",
				"XPathResult", "ActiveXObject", "DOMParser" ,"XPathEvaluator","XMLSerializer"}));
		// html
		attributes.addAll(Arrays.asList(new String[] { "HTMLFormElement",
				"HTMLInputElement", "HTMLSelectElement", "ActiveXObject",
				"HTMLTextAreaElement", "HTMLElement", "Image" }));
		BROWSER_VARIBALES = attributes;

		JAVA_VARIBALES = new HashSet<String>(Arrays.asList(new String[] {
				"Packages", "java", "javax" }));

		JSI_VARIBALES = new HashSet<String>(Arrays.asList(new String[] {
				"$JSI", "$require","$log", "$import" }));

		attributes = new HashSet<String>();
		attributes.addAll(ECMA_VARIBALES);
		attributes.addAll(BROWSER_VARIBALES);
		attributes.addAll(JAVA_VARIBALES);
		attributes.addAll(JSI_VARIBALES);
		ALL_VARIBALES = attributes;
	}

}