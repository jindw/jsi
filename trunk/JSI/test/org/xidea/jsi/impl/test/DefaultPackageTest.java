package org.xidea.jsi.impl.test;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.xidea.jsi.impl.v2.DefaultPackage;

public class DefaultPackageTest {

	@Test
	public void testPackageOptimize() throws Exception {
		DefaultPackage dp = new DefaultPackage(null, "org.xidea.current");
		Method method = DefaultPackage.class.getDeclaredMethod(
				"optimizeTargetPath", String.class, String.class);
		method.setAccessible(true);
		Assert.assertEquals("org/xidea/test.js", method.invoke(dp,
				"org.xidea.current", "../test.js"));
		Assert.assertEquals("org/test.js", method.invoke(dp,
				"org.xidea.current", "../../test.js"));
		Assert.assertEquals("org/xidea/util/test.js", method.invoke(dp,
				"org.xidea.current", "../util/test.js"));
		Assert.assertEquals("org/xidea/current/util/test.js", method.invoke(dp,
				"org.xidea.current", "./util/test.js"));

		Assert.assertEquals("org.xidea.Test", method.invoke(dp,
				"org.xidea.current", "..Test"));
		Assert.assertEquals("org.Test", method.invoke(dp,
				"org.xidea.current", "....Test"));
		Assert.assertEquals("org.xidea.util.Test", method.invoke(dp,
				"org.xidea.util", "..util.Test"));
		Assert.assertEquals("org.xidea.current.util.Test", method.invoke(dp,
				"org.xidea.current", ".util.Test"));

	}
}
