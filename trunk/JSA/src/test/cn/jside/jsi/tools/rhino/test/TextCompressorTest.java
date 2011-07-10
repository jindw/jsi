package cn.jside.jsi.tools.rhino.test;

import org.junit.Test;

import cn.jside.jsi.tools.rhino.DebugTool;
import cn.jside.jsi.tools.rhino.TextCompressor;

public class TextCompressorTest {

	@Test
	public void testCompress() {
		// DebugTool.println("sss $codes $code ".replace("$code", "X"));
		DebugTool.info(new TextCompressor().compress(
				"aa,d,d,d,d,b,cccc,cccc,b,b,b,d,d,d,d,d,d", true));
	}

}
