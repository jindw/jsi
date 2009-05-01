package org.xidea.jsi.test;

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
import org.xidea.jsi.impl.RhinoScriptPackagePaser;


public class RuntimeTest {
	@Test
	public void testJava6() throws UnsupportedEncodingException, ScriptException, IOException{
		ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
		engine.eval(new ClasspathRoot().loadText("boot.js"));
		System.out.println(engine.eval("$import('example:sayHello')"));
		System.out.println(engine.eval("$import('org.xidea.lite:Template')"));
		//System.out.println(engine.eval("$import('example:sayHello')"));
	}
	@Test
	public void testRhino() throws UnsupportedEncodingException, ScriptException, IOException{
		final ClasspathRoot cp = new ClasspathRoot();
		Object result = Context.call(new ContextAction() {
			public Object run(final Context cx) {
				Scriptable scope = ScriptRuntime.getGlobal(cx);
				try {
					cx.evaluateString(scope, cp.loadText("boot.js"), "<package-wrapper.js>", 1, null);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cx.evaluateString(scope,"$import('example:sayHello')", "1.js", 1, null);
				cx.evaluateString(scope,"$import('org.xidea.lite:Template')", "1.js", 1, null);
				return null;
			}
		});
		
		//System.out.println(engine.eval("$import('example:sayHello')"));
	}
}
