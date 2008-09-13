package org.xidea.jsi.impl.test;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.impl.AbstractJSIRoot;
import org.xidea.jsi.impl.ClasspathJSIRoot;

public class AbstractJSIRootTest {
	private AbstractJSIRoot root;

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		root = new ClasspathJSIRoot("utf-8");
	}


	/**
	 * {@link #test$importStringJSILoadContext()}
	 */
	@Test
	public void test$importString() {
	}

	@Test
	public void test$importStringJSILoadContext() {
		HashMap<String, String> exampleMap = new HashMap<String, String>();
		exampleMap.put("sayHello", "example");
		exampleMap.put("message", "example");
		JSILoadContext context = root.$import("example.*");
		//assertEquals(exampleMap,context.getExportMap());
		assertEquals(1,context.getScriptList().size());
		

		HashMap<String, String> exampleInternalMap = new HashMap<String, String>();
		exampleInternalMap.put("Jindw", "example.internal");
		exampleInternalMap.put("Guest", "example.internal");
		exampleInternalMap.put("buildMessage", "example.internal");
		context = root.$import("example.internal.*");
		//assertEquals(exampleInternalMap,context.getExportMap());
		assertEquals(2,context.getScriptList().size());
		
		HashMap<String, String> exampleDependenceMap = new HashMap<String, String>();
		exampleDependenceMap.put("showDetail", "example.dependence");
		context = root.$import("example.dependence.*");
		//assertEquals(exampleInternalMap,context.getExportMap());
		assertEquals(2,context.getScriptList().size());
	}

	@Test
	public void testRequirePackage() {
		JSIPackage pkg = root.requirePackage("example.dependence", true);
		assertEquals("example.dependence", pkg.getName());
		pkg = root.requirePackage("example.dependence.xxx", false);
		assertEquals("example.dependence", pkg.getName());
		pkg = root.requirePackage("example.dependence.xxx", true);
		if(pkg != null){
			fail("无效包路径应该抛出异常");
		}
		
	}

	@Test
	public void testFindPackageByPath() {
		assertEquals("example.dependence",root.findPackageByPath("example.dependence.xxx").getName());
		assertEquals("example",root.findPackageByPath("example.dependence").getName());
	}

}
