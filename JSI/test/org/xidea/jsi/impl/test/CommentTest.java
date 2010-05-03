package org.xidea.jsi.impl.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.xidea.jsi.impl.ClasspathRoot;
import org.xidea.jsi.impl.RhinoScriptPackagePaser;
import org.xidea.jsi.impl.RhinoSupport;
import org.xidea.lite.parser.impl.JSProxy;

public class CommentTest {

	@Test
	public void testRhino() throws UnsupportedEncodingException,
			ScriptException, IOException, URISyntaxException {
		JSProxy jp = JSProxy.newProxy();
		System.out.println(jp.isJSIAvailable());

		
		System.out.println(JSProxy.class.getClassLoader().getResource("org/xidea/"));

		URL boot = JSProxy.class.getClassLoader().getResource("boot.js");
		try {
			jp.eval(boot);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(boot);
		jp.eval("$import('example.incomment.*');sayHello('xx')");

		// System.out.println(engine.eval("$import('example:sayHello')"));
	}
}
