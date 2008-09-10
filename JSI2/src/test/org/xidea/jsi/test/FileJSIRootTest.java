package org.xidea.jsi.test;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.DefaultJSILoadContext;
import org.xidea.jsi.impl.FileJSIRoot;

public class FileJSIRootTest{

	protected JSIRoot fileRoot;
	protected JSILoadContext loadContext;

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
	}
	@Test
	public void testJSISideImport(){
		System.out.println(111);
		fileRoot = new FileJSIRoot("D:\\eclipse\\workspace\\JSISide\\web\\scripts", "utf-8");
		//fileRoot = new FileJSIRoot(this.getClass().getResource("/").getFile(), "utf-8");
		loadContext = new DefaultJSILoadContext();
		fileRoot.$import("org.jside.decorator.*");
	}
	@After
	public void tearDown() throws Exception {
	}
}
