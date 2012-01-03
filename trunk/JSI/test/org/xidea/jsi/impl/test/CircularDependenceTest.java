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
import org.xidea.jsi.impl.v2.DataRoot;

public class CircularDependenceTest {

	private DataRoot root;

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
			root = new DataRoot(data);
			root.$export("test/test1");
		} catch (StackOverflowError e) {
			return;
		}
		Assert.fail("循环引用应该导出失败");
	}

	@Test
	public void testDoubleBeforeDependence()
			throws UnsupportedEncodingException, IOException {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("test/test1.js", "print(1);function test1(){}");
		data.put("test/test2.js", "print(2);function test2(){}");
		data.put("test/test3.js", "print(3);function test3(){}");
		data.put("test/test4.js", "print(4);function test4(){}");
		data.put("test/__package__.js",
				"this.addScript('test1.js','test1',['test2.js','test4']);"
						+ "this.addScript('test2.js','test2',0,'test3.js');"
						+ "this.addScript('test3.js','test3','test1.js');"
						+ "this.addScript('test4.js','test4',0,'test1.js');");
		root = new DataRoot(data);
		for (int i = 1; i < 4; i++) {
			JSILoadContext context = root.$export("test/test" + i + "");
			ArrayList<String> result = new ArrayList<String>();
			for (ScriptLoader s : context.getScriptList()) {
				result.add(s.getName());
			}
			int i1 = result.indexOf("test1.js");
			int i2 = result.indexOf("test2.js");
			int i3 = result.indexOf("test3.js");
			int i4 = result.indexOf("test4.js");
			System.out.println(result);
			Assert.assertTrue(result.size() == 4);
			Assert.assertTrue(i1 > i2);
			Assert.assertTrue(i1 > i4);
			Assert.assertTrue(i3 > i1);
		}

	}
}
