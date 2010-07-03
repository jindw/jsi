package org.xidea.jsi.runtime.test;

import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Assert;
import org.junit.Test;
import org.xidea.jsi.JSIRuntime;
import org.xidea.jsi.impl.RuntimeSupport;


public class RuntimeSupportTest {
	@Test
	public void testLog(){
		JSIRuntime rt = RuntimeSupport.create();
		rt.eval("$import('org.xidea.jsi:parse')");
		rt.eval("$import('org.xidea.jsi:$log')");
		rt.eval("function x(){$log.info(123)\n};x();");
	}

	@Test
	public void testJava6Proxy() throws Exception{
		test(Class.forName("org.xidea.jsi.impl.Java6Impl"));
	}
	@Test
	public void testRhinoProxy() throws Exception{
		test(Class.forName("org.xidea.jsi.impl.RhinoImpl"));
	}
	@Test
	public void testJava6() throws Exception{
		ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");

		engine.eval("this['javax.script.filename']='<boot.js>'");
		engine.eval(new InputStreamReader(this.getClass().getResourceAsStream(
				"/boot.js"), "utf-8"));
	}
	private void test(Class<? extends Object> c)throws Exception{
		Method method = c.getMethod("create", Boolean.TYPE);
		method.setAccessible(true);
		JSIRuntime p = (JSIRuntime) method.invoke(null, true);
		p.eval("var a = 1;");
		method = RuntimeSupport.class.getDeclaredMethod("initialize");
		method.setAccessible(true);
		method.invoke(p);
		Map<String, Object> varMap=new HashMap<String, Object>();
		varMap.put("b", 2.0);
		
		Assert.assertEquals("4",p.eval(null,"a = a+1;String(a +b)",".",varMap));
		Assert.assertEquals("2",p.eval(null,"String(a)",".",varMap));

	}
}
