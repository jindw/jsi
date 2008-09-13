package org.xidea.jsi.impl.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.impl.ClasspathJSIRoot;
import org.xidea.jsi.impl.DataJSIRoot;
import org.xidea.jsi.impl.FileJSIRoot;

public class DataJSIRootTest {

	private DataJSIRoot root;
	private String packageName = this.getClass().getPackage().getName();

	@Before
	public void setUp() throws Exception {
		HashMap data = new HashMap();
		data.put(packageName.replace('.', '/') + "/utf8.js", "测试utf8");
		root = new DataJSIRoot(data);
	}

	@Test
	public void testLoadText() {
		assertEquals("测试utf8", root.loadText(packageName, "utf8.js"));

	}
}
