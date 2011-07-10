package cn.jside.jsi.tools.rhino.test;

import org.jside.jsi.tools.JSAToolkit;
import org.junit.Before;
import org.junit.Test;

public class RhinoToolTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPrintSourceString() {
		System.out.println(JSAToolkit.getInstance()
				.createJavaScriptCompressor().compress("({中文:1})", null));
	}

	@Test
	public void testPrintCENDString() {
		System.out.println(JSAToolkit.getInstance()
				.createJavaScriptCompressor()
				.compress("({中文:']]>'})", null));
	}

}
