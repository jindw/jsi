package cn.jside.jsi.tools.test;

import java.io.IOException;

import org.jside.jsi.tools.JSAToolkit;
import org.jside.jsi.tools.JavaScriptCompressor;
import org.junit.Test;
import org.xidea.jsi.impl.JSIText;

import cn.jside.jsi.tools.rhino.PreTextProcess;


public class FormatTest {
	@Test
	public void testFormat() throws IOException{
		String script = JSIText.loadText(FormatTest.class.getResourceAsStream("main.js"), "utf-8");
		JavaScriptCompressor formator = JSAToolkit.getInstance().createJavaScriptCompressor();
		formator.format(script);
	}
	@Test
	public void testPreFormat() throws IOException{
		String script = JSIText.loadText(FormatTest.class.getResourceAsStream("main2.js"), "utf-8");
		PreTextProcess formator = new PreTextProcess();
		formator.encodeComment(script,true);
	}

}
