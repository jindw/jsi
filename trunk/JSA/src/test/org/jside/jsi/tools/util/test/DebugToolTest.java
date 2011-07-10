package org.jside.jsi.tools.util.test;

import org.junit.Test;
import org.mozilla.javascript.ScriptOrFnNode;

import cn.jside.jsi.tools.rhino.DebugTool;

public class DebugToolTest {

	@Test
	public void testPrintScriptOrFnNode() {

		ScriptOrFnNode root = DebugTool.parse("if(''){}else if(!''){}");
		DebugTool.print(root);
	}

}
