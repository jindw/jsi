package org.xidea.jsi.runtime.test;

import org.junit.Test;
import org.xidea.jsi.JSIRuntime;
import org.xidea.jsi.impl.RuntimeSupport;


public class RuntimeSupportTest {
	@Test
	public void testLog(){
		JSIRuntime rt = RuntimeSupport.create();
		rt.eval("$import('org.xidea.jsidoc.util:$log')");
		rt.eval("$import('org.xidea.jsi:parse')");
		rt.eval("$import('org.xidea.jsi:$log')");
		rt.eval("function x(){$log.info(123)\n};x();");
	}

}
