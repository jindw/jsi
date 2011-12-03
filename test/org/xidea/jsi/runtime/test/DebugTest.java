package org.xidea.jsi.runtime.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;
import org.mozilla.javascript.tools.debugger.Dim;
import org.mozilla.javascript.tools.debugger.Main;
import org.mozilla.javascript.tools.debugger.ScopeProvider;
import org.mozilla.javascript.tools.shell.Global;
import org.xidea.jsi.JSIRuntime;
import org.xidea.jsi.impl.RuntimeSupport;

public class DebugTest {
	static RuntimeSupport rt = (RuntimeSupport) RuntimeSupport.create();

	private static Main init(ContextFactory factory,Scriptable scope) {
		Main main = new Main("Test");
		main.doBreak();
		main.attachTo(factory);
		main.setScope(scope);
		main.pack();
		main.setSize(600, 460);
		main.setVisible(true);
		return main;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(new Date()));
		final ContextFactory factory = ContextFactory.getGlobal();
		rt.setOptimizationLevel(-1);
		Scriptable global = (Scriptable) rt.getGlobals();
		init(factory, global);
		Context cx = Context.enter();
		Object test = rt.eval(
								"//\nfunction test(a,b){\njava.lang.System.out.println(a+b)\n};test(1,2)",
								"test.js");
		rt.eval("$import('org.xidea.jsidoc.util.*')");
		System.out.println(1111);
		//Context.call(factory, (Callable)test, global, null, new Object[]{1,2});
	}

	/**
	 * @param args
	 */
	public static void main2(String[] args) {
		final ContextFactory factory = ContextFactory.getGlobal();
		final Global global = new Global();
		init(factory, global);
		Context cx = Context.enter();
		global.init(cx);
		Object test = cx.evaluateString(
								global,
								"//\nfunction test(a,b){\njava.lang.System.out.println(a+b)\n};test",
								"test.js", 1, null);
		Context.call(factory, (Callable)test, global, null, new Object[]{1,2});
	}


}
