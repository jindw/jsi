package org.xidea.jsi.impl.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.xidea.jsi.impl.ClasspathRoot;
import org.xidea.jsi.impl.JSIText;
import org.xidea.jsi.impl.RhinoScriptPackagePaser;
import org.xidea.jsi.impl.RhinoSupport;

public class RuntimeTest{
	
	@Test
	public void testJava6() throws UnsupportedEncodingException,
			ScriptException, IOException {
		ScriptEngine engine = new ScriptEngineManager()
				.getEngineByExtension("js");
		engine.eval(JSIText.loadText(this.getClass().getResource("/boot.js"),"utf-8"));
		System.out.println(engine.eval("$import('example:sayHello')"));
		System.out.println(engine.eval("$import('org.xidea.lite:Template')"));
		// System.out.println(engine.eval("$import('example:sayHello')"));
		try{
			engine.eval("new Template().render()");
		} catch (Exception e) {
			RhinoSupportTest.checkLogContains(e,"classpath:///org/xidea/lite/template.js");
		}
	}

	@Test
	public void testRhino() throws UnsupportedEncodingException, ScriptException, IOException{
		final ClasspathRoot cp = new ClasspathRoot();
		Object result = Context.call(new ContextAction() {
			final String boot = JSIText.loadText(this.getClass().getResource("/boot.js"),"utf-8");
			public Object run(final Context cx) {
				Scriptable scope = ScriptRuntime.getGlobal(cx);
				cx.evaluateString(scope,"this.x=1;for(n in this){java.lang.System.out.print(n)}", "1.js", 1, null);

				cx.evaluateString(scope, boot, "<package-wrapper.js>", 1, null);
				cx.evaluateString(scope,"$import('example:sayHello')", "1.js", 1, null);
				cx.evaluateString(scope,"$import('org.xidea.lite:Template')", "1.js", 1, null);
				try{
					cx.evaluateString(scope,"new Template().render()", "1.js", 1, null);
				} catch (Exception e) {
					RhinoSupportTest.checkLogContains(e,"classpath:///org/xidea/lite/template.js");
				}
				return null;
			}
		});
		
		// System.out.println(engine.eval("$import('example:sayHello')"));
	}
}
