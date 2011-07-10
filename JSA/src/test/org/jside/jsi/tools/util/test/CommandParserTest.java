package org.jside.jsi.tools.util.test;


import static org.junit.Assert.fail;

import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.xidea.el.impl.CommandParser;
import org.junit.Test;

public class CommandParserTest {

	@Test
	public void testSetupObjectStringArray() {
		JavaScriptCompressorConfig config = new JavaScriptCompressorConfig();
		CommandParser.setup(config, new String[]{"-debugCalls","aaa,bbb,ccc"});
		System.out.println(config.getDebugCalls());
	}

	@Test
	public void testParseArgs() {
		//fail("Not yet implemented");
	}

}
