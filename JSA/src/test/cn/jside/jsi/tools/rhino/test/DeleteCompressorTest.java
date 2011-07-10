package cn.jside.jsi.tools.rhino.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

public class DeleteCompressorTest {

	@Test
	public void testCompress() {

		ErrorHolder errorHolder = new ErrorHolder(true);
		Parser parser = RhinoTool.createParser(new CompilerEnvironsImpl(), errorHolder);
		DeleteCompressor dc = new DeleteCompressor(parser);
		JavaScriptCompressorConfig config = new JavaScriptCompressorConfig();
		config.setFeatures(Arrays.asList("test"));
		dc.setCompressorConfig(config);

		Map<String, String> caseMap = DebugTool.loadCase(DeleteCompressorTest.class,"delete-test.xml");
		List<String[]> failureList = new ArrayList<String[]>();
		for (String key : caseMap.keySet()) {
			String value = dc.compress(key, JavaScriptCompressorAdaptor.EMPTY_ADVISOR);
			if (value.equals(caseMap.get(key))) {
				DebugTool.info("******************");
				DebugTool.info(key);
				DebugTool.info(value);
				DebugTool.info("\n");
			} else {
				failureList.add(new String[] { key, value });
			}
		}

		if (failureList.size() > 0) {
			System.out.flush();
			System.err.println("\n删除压缩失败STF");
			for (String[] keyResult : failureList) {
				System.err.println("******************");
				String key = keyResult[0];
				String result = keyResult[1];
				System.err.println(key);
				System.err.println(caseMap.get(key));
				System.err.println(result);
				System.err.println();
				System.err.flush();
			}
			Assert.fail();
		}
	}

}
