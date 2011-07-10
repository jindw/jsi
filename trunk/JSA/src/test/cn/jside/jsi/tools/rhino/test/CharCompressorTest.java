package cn.jside.jsi.tools.rhino.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jside.jsi.tools.JSAToolkit;
import org.jside.jsi.tools.JavaScriptCompressor;
import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Parser;

import cn.jside.jsi.tools.JavaScriptCompressorAdaptor;
import cn.jside.jsi.tools.rhino.CompilerEnvironsImpl;
import cn.jside.jsi.tools.rhino.DebugTool;
import cn.jside.jsi.tools.rhino.DeleteCompressor;
import cn.jside.jsi.tools.rhino.ErrorHolder;
import cn.jside.jsi.tools.rhino.RhinoTool;

public class CharCompressorTest {

	@Test
	public void testCompress() {

		String textSource = "var a=/^(\\s|\\u00A0)+|(\\s|\\u00A0)+$/g;";
		JavaScriptCompressor c = JSAToolkit.getInstance().createJavaScriptCompressor();
		String result = c.compress(textSource, null);
		System.out.println(result);
	}@Test
	public void testForIf() {

		String textSource = "if(i){" +
				"for(var i;i<10;i++){if(i){break;}}" +
				"}else{i++}";
		JavaScriptCompressor c = JSAToolkit.getInstance().createJavaScriptCompressor();
		JavaScriptCompressorConfig config = JSAToolkit.getInstance().createJavaScriptCompressorConfig();
		config.setTrimBracket(false);
		c.setCompressorConfig(config);
		String result = c.compress(textSource, null);
		System.out.println(result);
	}

}
