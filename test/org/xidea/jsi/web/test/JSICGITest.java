package org.xidea.jsi.web.test;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.web.JSICGI;

public class JSICGITest {
	private HashMap<String, String> envmap = new HashMap<String, String>();
	@Before
	public void setUp(){
		envmap.put("PATH_INFO", "/d/boot.js");
		envmap.put("DOCUMENT_ROOT", "d:/");
		envmap.put("REQUEST_METHOD", "GET");
		envmap.put("QUERY_STRING", "trace=1");
	}

	@Test
	public void testJSICGI() {
		new JSICGI(envmap);
	}

	@Test
	public void testExecute() throws IOException {
		new JSICGI(envmap).execute();
	}
//
//	@Test
//	public void testMain() throws IOException {
//		JSICGI.main("");
//	}

}
