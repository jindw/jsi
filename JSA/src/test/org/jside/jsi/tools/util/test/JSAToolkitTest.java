package org.jside.jsi.tools.util.test;

import org.jside.jsi.tools.JSAToolkit;
import org.junit.Test;


public class JSAToolkitTest {

	@Test
	public void testGetCompressorImpl() {
		JSAToolkit.getInstance().createJavaScriptCompressor().compress("/[/]./", null);
	}

}
