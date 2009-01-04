package org.xidea.jsi.impl.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.impl.DataJSIRoot;

public class CircularDependenceTest {

	private DataJSIRoot root;

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testImport() throws UnsupportedEncodingException, IOException {
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

}
