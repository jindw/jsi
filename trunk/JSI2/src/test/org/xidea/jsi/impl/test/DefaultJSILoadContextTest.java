package org.xidea.jsi.impl.test;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.impl.ClasspathJSIRoot;

public class DefaultJSILoadContextTest {

	private ClasspathJSIRoot root;

	@Before
	public void setUp() throws Exception {
		root = new ClasspathJSIRoot("utf-8");
	}

	@Test
	public void testLoadScript() {

	}

	@Test
	public void testGetExportMap() {
		HashMap<String, String> exampleMap = new HashMap<String, String>();
		exampleMap.put("sayHello", "example");
		JSILoadContext context = root.$import("example.sayHello");
		assertEquals(exampleMap,context.getExportMap());
		

		HashMap<String, String> exampleInternalMap = new HashMap<String, String>();
		exampleInternalMap.put("Jindw", "example.internal");
		exampleInternalMap.put("Guest", "example.internal");
		exampleInternalMap.put("buildMessage", "example.internal");
		context = root.$import("example.internal.*");
		assertEquals(exampleInternalMap,context.getExportMap());
		
		HashMap<String, String> exampleDependenceMap = new HashMap<String, String>();
		exampleDependenceMap.put("showDetail", "example.dependence");
		context = root.$import("example.dependence.*");
		assertEquals(exampleDependenceMap,context.getExportMap());
	}

	@Test
	public void testGetScriptList() {
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

}
