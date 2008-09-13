package org.xidea.jsi.impl.test;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.impl.ClasspathJSIRoot;
import static org.xidea.jsi.impl.test.AbstractJSIRootTest.*;

public class DefaultJSILoadContextTest {

	private ClasspathJSIRoot root;

	@Before
	public void setUp() throws Exception {
		root = new ClasspathJSIRoot("utf-8");
	}

	@Test
	public void testLoadScript() {
		fail("麻烦：（");

	}

	@Test
	public void testGetExportMap() {
		JSILoadContext context = root.$import("example.sayHello");
		assertEquals(createObjectPackageMap("example","sayHello"),context.getExportMap());

		context = root.$import("example.internal.*");
		assertEquals(ALL_EXAMPLE_INTERNAL_MAP,context.getExportMap());
		
		context = root.$import("example.dependence.*");
		assertEquals(ALL_EXAMPLE_DEPENDENCE_MAP,context.getExportMap());
	}

	@Test
	public void testGetScriptList() {
		JSILoadContext context = root.$import("example.*");
		assertEquals(1,context.getScriptList().size());
		
		context = root.$import("example.internal.*");
		assertEquals(2,context.getScriptList().size());
		
		context = root.$import("example.dependence.*");
		assertEquals(2,context.getScriptList().size());
	}

}
