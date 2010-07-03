package org.xidea.jsi.impl.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.script.ScriptException;

import org.junit.Test;
import org.xidea.jsi.JSIRuntime;
import org.xidea.jsi.impl.RuntimeSupport;

public class CommentTest {

	@Test
	public void testRhino() throws UnsupportedEncodingException,
			ScriptException, IOException, URISyntaxException {
		JSIRuntime jp = RuntimeSupport.create();

		
		System.out.println(CommentTest.class.getClassLoader().getResource("org/xidea/"));

		URL boot = CommentTest.class.getClassLoader().getResource("boot.js");
		try {
			jp.eval(boot);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(boot);
		jp.eval("$import('example.incomment.*');sayHello('xx')");

		// System.out.println(engine.eval("$import('example:sayHello')"));
	}
}
