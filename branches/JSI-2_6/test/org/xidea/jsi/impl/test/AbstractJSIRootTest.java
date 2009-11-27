package org.xidea.jsi.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.ScriptNotFoundException;
import org.xidea.jsi.impl.AbstractRoot;
import org.xidea.jsi.impl.ClasspathRoot;
import org.xidea.jsi.impl.DefaultLoadContext;

public class AbstractJSIRootTest {
	private AbstractRoot root;
	public static Map<String, String> ALL_EXAMPLE_MAP = createObjectPackageMap(
			"example", "sayHello", "message");
	public static Map<String, String> ALL_EXAMPLE_INTERNAL_MAP = createObjectPackageMap(
			"example.internal", "Jindw", "Guest", "buildMessage");

	public static Map<String, String> ALL_EXAMPLE_DEPENDENCE_MAP = createObjectPackageMap(
			"example.dependence", "showDetail");

	public static Map<String, String> createObjectPackageMap(String pkgName,
			String... objects) {
		HashMap<String, String> data = new HashMap<String, String>();
		for (String name : objects) {
			data.put(name, pkgName);
		}
		return data;
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		root = new ClasspathRoot("utf-8");
	}

	/**
	 * {@link #test$importStringJSILoadContext()}
	 */
	@Test
	public void test$importString() {
		JSILoadContext context = root.$import("example.*");
		assertEquals(ALL_EXAMPLE_MAP, context.getExportMap());
		assertEquals(1, context.getScriptList().size());

		context = root.$import("example.internal.*");
		assertEquals(ALL_EXAMPLE_INTERNAL_MAP, context.getExportMap());
		assertEquals(2, context.getScriptList().size());

		context = root.$import("example.dependence.*");
		assertEquals(ALL_EXAMPLE_DEPENDENCE_MAP, context.getExportMap());
		assertEquals(2, context.getScriptList().size());

		context = root.$import("org.xidea.jsidoc.JSIDoc");
		assertTrue(context.getExportMap().size() > 0);
	}

	@Test
	public void test$importStringJSILoadContext() {
		JSILoadContext context = new DefaultLoadContext();
		Map<String, String> expect = new HashMap<String, String>(
				ALL_EXAMPLE_MAP);
		root.$import("example.*", context);
		assertEquals(expect, context.getExportMap());
		assertEquals(1, context.getScriptList().size());

		root.$import("example.internal.*", context);
		expect.putAll(ALL_EXAMPLE_INTERNAL_MAP);
		assertEquals(expect, context.getExportMap());
		assertEquals(3, context.getScriptList().size());

		expect.putAll(ALL_EXAMPLE_DEPENDENCE_MAP);
		root.$import("example.dependence.*", context);
		assertEquals(expect, context.getExportMap());
		assertEquals(5, context.getScriptList().size());

	}

	@Test
	public void testRequirePackage() {
		JSIPackage pkg = root.requirePackage("example.dependence");
		assertEquals("example.dependence", pkg.getName());
		pkg = root.findPackageByPath("example.dependence.xxx");
		assertEquals("example.dependence", pkg.getName());
		try {
			pkg = root.requirePackage("example.dependence.xxx");
			fail("无效包路径应该抛出异常");
		} catch (ScriptNotFoundException e) {

		}

	}

	@Test
	public void testFindPackageByPath() {
		assertEquals("example.dependence", root.findPackageByPath(
				"example.dependence.xxx").getName());
		assertEquals("example", root.findPackageByPath("example.dependence")
				.getName());
	}

}
