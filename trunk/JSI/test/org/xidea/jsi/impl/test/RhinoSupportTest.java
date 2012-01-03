package org.xidea.jsi.impl.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.ScriptableObject;
import org.xidea.jsi.JSIRuntime;
import org.xidea.jsi.impl.v2.JSIText;
import org.xidea.jsi.impl.v2.RuntimeSupport;

public class RhinoSupportTest {

	private JSIRuntime rs;

	@Before
	public void setUp() throws Exception {
		rs = RuntimeSupport.create();
	}

	@Test
	public void testScope() throws IOException {
		rs.eval("var a = 1");
		System.out.println(rs.eval("a"));
		rs.eval("var a = 2;b=2");
		System.out.println(rs.eval("a"));
		rs.eval(null,"a ++","a",new HashMap<String, Object>());
		System.out.println(rs.eval("a+b"));
		Assert.assertEquals("变量可以改变，传入的新变量不影响父域，新域可以修改父域数据",5.0d,((Number)rs.eval("a+b")).doubleValue(),0);
	}

	@Test
	public void testRhinoSupport() throws IOException {
		rs.eval("$import('org.xidea.jsidoc.util.$log')(String(123))");
	}
	@Test
	public void testLoadText() throws IOException {
		assertEquals("测试utf8",  JSIText.loadText(this.getClass().getResource("/org/xidea/jsi/impl/test/utf8.js"),"utf-8")
				);
	}

	@Test
	public void testBuildEvaler() {
		try {
			Context cx = Context.enter();
			cx
					.evaluateString(
							ScriptRuntime.getGlobal(cx),
							"Packages.org.xidea.jsi.impl.RuntimeSupport.createEvaler(this,'dir/file.js').call({scriptBase:'/dir/',name:'file.js'},'//\\n//\\n>line2...')",
							"<file>", 1, null);

			Assert.fail("");
		} catch (Exception e) {
			//e.printStackTrace();
			checkLogContains(e, "dir/file.js","3");
		} finally {
			Context.exit();
		}
	}

	@Test
	public void testJava6BuildEvaler() {
		try {
			ScriptEngine engine = new ScriptEngineManager()
					.getEngineByExtension("js");
			System.out.println(engine);
			engine.eval(
							"Packages.org.xidea.jsi.impl.RuntimeSupport.createEvaler(this,'dir/file.js').call({scriptBase:'/dir/',name:'file.js'},'...')");

			Assert.fail("");
		} catch (Exception e) {
			checkLogContains(e, "dir/file.js");
		} finally {
		}
	}

	@Test
	public void testJSI() throws IOException {
		try {
			Context cx = Context.enter();
			ScriptableObject globals = ScriptRuntime.getGlobal(cx);
			cx.evaluateString(globals, JSIText.loadText(this.getClass().getResource("/boot.js"),"utf-8"), "#",
					1, null);
			cx
					.evaluateString(
							globals,
							"$JSI.preload('mytest',{'':\"this.addScript('test-file2009.js',['len','a','b'])\",'test-file2009.js':'var a,b;function len(arg){return (arg.length)}'});",
							"???", 1, null);
			cx.evaluateString(globals, "$import('mytest.*')", "#", 1, null);
			assertEquals(3, cx.evaluateString(globals, "len([1,'1',[1,2,3]])",
					"#", 1, null));
			try {
				cx.evaluateString(globals, "len()", "#", 1, null);
				fail("");
			} catch (Exception e) {
				checkLogContains(e, "mytest/test-file2009.js");
			}

		} finally {
			Context.exit();
		}
	}

	public static void checkLogContains(Exception e, String... keys) {
		StringWriter buf = new StringWriter();
		PrintWriter out = new PrintWriter(buf);
		e.printStackTrace(out);
		out.flush();
		buf.flush();
		//System.out.println(buf);
		for(String key:keys){
			assertTrue(buf.toString().indexOf(key) > 0);
		}
	}

}
