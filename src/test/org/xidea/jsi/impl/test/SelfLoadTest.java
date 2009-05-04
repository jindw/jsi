package org.xidea.jsi.impl.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.impl.ClasspathRoot;
import org.xidea.jsi.impl.DefaultLoadContext;
import org.xidea.jsi.impl.FileRoot;


public class SelfLoadTest {

	private ClasspathRoot root;

	@Before
	public void setUp() throws Exception {
		root = new ClasspathRoot();
	}

	@Test
	public void testLoadText() {
		JSILoadContext context = new DefaultLoadContext();
		root.$import("example.*",context);
		root.$import("example.alias.*",context);
		root.$import("example.dependence.*",context);
		root.$import("example.internal.*",context);
		root.$import("org.xidea.jsidoc.*",context);
		root.$import("org.xidea.jsidoc.export.*",context);
//		root.$import("org.xidea.jsidoc.html.*",context);
//		root.$import("org.xidea.jsidoc.styles.*",context);
		root.$import("org.xidea.jsidoc.util.*",context);
		root.$import("org.xidea.test.*",context);
		root.$import("org.xidea.test.loader.*",context);
		System.out.println(context.getScriptList());
	}
}
