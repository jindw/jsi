package org.xidea.jsi.impl.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.script.ScriptEngine;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.ScriptableObject;
import org.xidea.jsi.impl.RhinoSupport;

public class RhinoSupportTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testLoadText() throws IOException {
		assertEquals("测试utf8", RhinoSupport.loadText("org/xidea/jsi/impl/test/utf8.js"));
	}

	@Test
	public void testBuildEvaler() {
		try{
			Context cx = Context.enter();
			cx.evaluateString(
				ScriptRuntime.getGlobal(cx),
				"Packages.org.xidea.jsi.impl.RhinoSupport.buildEvaler({scriptBase:'/dir/',name:'file.js'})('...')", "#", 1, null);
		}catch (Exception e) {
			checkLogContains(e,"dir/file.js");
		}finally{
			Context.exit();
		}
	}

	@Test
	public void testJSI() throws IOException {
		try{
			Context cx = Context.enter();
			ScriptableObject globals = ScriptRuntime.getGlobal(cx);
			cx.evaluateString(
					globals,
					RhinoSupport.loadText("boot.js"), "#", 1, null);
			cx.evaluateString(
					globals,
					"$JSI.preload('mytest',{'':\"this.addScript('test-file2009.js',['len','a','b'])\",'test-file2009.js':'var a,b;function len(arg){return (arg.length)}'});", "???", 1, null);
			cx.evaluateString(
					globals,
					"$import('mytest.*')", "#", 1, null);	
			assertEquals(3, cx.evaluateString(
					globals,
					"len([1,'1',[1,2,3]])", "#", 1, null));
			try{
			cx.evaluateString(
					globals,
					"len()", "#", 1, null);
			fail("");
			}catch (Exception e) {
				checkLogContains(e,"mytest/test-file2009.js");
			}
	
		}finally{
			Context.exit();
		}
	}

	private void checkLogContains(Exception e, String key) {
		StringWriter buf = new StringWriter();
		PrintWriter out = new PrintWriter(buf);
		e.printStackTrace(out);
		out.flush();
		buf.flush();
		System.out.println(buf);
		assertTrue(buf.toString().indexOf(key)>0);
	}

}
