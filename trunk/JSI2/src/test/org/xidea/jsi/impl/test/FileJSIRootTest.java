package org.xidea.jsi.impl.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.impl.FileJSIRoot;

public class FileJSIRootTest {
	private FileJSIRoot root;

	@Before
	public void setUp() throws Exception {

		root = new FileJSIRoot(this.getClass().getResource("/").getFile(),
				"utf-8");
	}

	@Test
	public void testLoadText() {
		assertEquals("测试utf8", root.loadText(this.getClass().getPackage()
				.getName(), "utf8.js"));

	}

}
