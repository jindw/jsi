package org.xidea.jsi.impl.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.impl.v2.ClasspathRoot;
import org.xidea.jsi.impl.v2.DefaultLoadContext;
import org.xidea.jsi.impl.v2.FileRoot;


public class SelfLoadTest {

	private ClasspathRoot root;

	@Before
	public void setUp() throws Exception {
		root = new ClasspathRoot();
	}

	@Test
	public void testLoadText() {
		JSILoadContext context = new DefaultLoadContext();
		root.$export("example",context);
		root.$export("example/alias",context);
		root.$export("example/dependence",context);
		root.$export("example/internal",context);
		root.$export("org/xidea/jsidoc",context);
		root.$export("org/xidea/jsidoc/export",context);
//		root.$export("org/xidea/jsidoc/html",context);
//		root.$export("org/xidea/jsidoc/styles",context);
		root.$export("org/xidea/jsidoc/util",context);
		root.$export("org/xidea/test",context);
		root.$export("org/xidea/test/loader",context);
		System.out.println(context.getScriptList());
	}
}
