package org.xidea.jsi.impl.test;


import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;


public class CurrentContexxtTest {

	@Before
	public void setUp() throws Exception {
	}
	@Test
	public void test() throws Exception {
		Object result = null;
		Context c1 = Context.enter();
		System.out.println(c1);
		Scriptable scope = ScriptRuntime.getGlobal(c1);
		try{
			Context c2 = Context.enter();
			System.out.println(c2);
			try{
				result = c2.evaluateString(scope, "Packages.org.mozilla.javascript.Context.getCurrentContext()", "", 1,null);
				System.out.println(result);
			}finally{
				Context.exit();
			}
			result = c2.evaluateString(scope, "Packages.org.mozilla.javascript.Context.getCurrentContext()", "", 1,null);
			System.out.println(result);
		}finally{
			Context.exit();
		}
	}

}
