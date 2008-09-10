package org.xidea.jsi.test;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.xidea.jsi.ScriptLoader;

public class TestImport extends FileJSIRootTest{
	@Test
	public void testImport() {
		fileRoot.$import("org.xidea.jsidoc.JSIDoc", loadContext);
		ArrayList<String> result = new ArrayList<String>();
		for (ScriptLoader file : loadContext.getScriptList()) {
			result.add(file.getPath());
		}
		Assert.assertTrue("导入数据不能为空", result.size()>0);
	}
}
