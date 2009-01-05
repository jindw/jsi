package org.xidea.jsi.impl.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.ScriptLoader;
import org.xidea.jsi.impl.DataJSIRoot;

public class CircularDependenceTest {

	private DataJSIRoot root;

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testFaild() throws UnsupportedEncodingException, IOException {
		try {
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("test/test1.js", "function test1(){alert(1)}");
			data.put("test/test2.js", "function test2(){alert(2)}");
			data
					.put(
							"test/__package__.js",
							"this.addScript('test1.js','test1','test2.js');this.addScript('test2.js','test2','test1.js')");
			root = new DataJSIRoot(data);
			root.$import("test/test1.js");
		} catch (StackOverflowError e) {
			return ;
		}
		Assert.fail("循环引用应该导出失败");
	}

	@Test
	public void testDoubleBeforeDependence() throws UnsupportedEncodingException, IOException {
		try {
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("test/test1.js", "print(1);function test1(){}");
			data.put("test/test2.js", "print(2);function test2(){}");
			data.put("test/test3.js", "print(3);function test3(){}");
			data
					.put(
							"test/__package__.js",
							"this.addScript('test1.js','test1',['test2.js','test3.js']);" +
							"this.addScript('test2.js','test2',0,'test1.js')" +
							"this.addScript('test3.js','test3',0,'test1.js')");
			root = new DataJSIRoot(data);
			JSILoadContext context = root.$import("test/test1.js");
			ArrayList<String> result = new ArrayList<String>();
			for (ScriptLoader s : context.getScriptList()) {
				result.add(s.getName());
			}
			int i1 = result.indexOf("test1.js");
			int i2 = result.indexOf("test2.js");
			int i3 = result.indexOf("test3.js");
			Assert.assertTrue(result.size()==3);
			Assert.assertTrue(i1>i2);
			Assert.assertTrue(i1>i3);
			System.out.println(result);
		} catch (StackOverflowError e) {
			return ;
		}
	}

}
