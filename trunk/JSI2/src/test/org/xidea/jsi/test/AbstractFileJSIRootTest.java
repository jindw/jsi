package org.xidea.jsi.test;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.DefaultJSILoadContext;
import org.xidea.jsi.impl.FileJSIRoot;

public class AbstractFileJSIRootTest{

	protected JSIRoot fileRoot;
	protected JSILoadContext loadContext;

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	protected void setUp() throws Exception {
		fileRoot = new FileJSIRoot(this.getClass().getResource("/").getFile(), "utf-8");
		loadContext = new DefaultJSILoadContext();
	}
	@After
	protected void tearDown() throws Exception {
	}
}
