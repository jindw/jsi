package org.xidea.jsi.impl.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.impl.FileRoot;

public class FileJSIRootTest {
	private FileRoot root;

	@Before
	public void setUp() throws Exception {

		root = new FileRoot(this.getClass().getResource("/").getFile(),
				"utf-8");
	}

	@Test
	public void testLoadText() {
		assertEquals("测试utf8", root.loadText(this.getClass().getPackage()
				.getName(), "utf8.js"));

	}

}
