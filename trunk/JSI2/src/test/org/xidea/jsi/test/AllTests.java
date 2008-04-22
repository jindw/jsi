package org.xidea.jsi.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.xidea.jsi.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(DataJSIImportTest.class);
		suite.addTestSuite(FileJSIImportTest.class);
		suite.addTestSuite(JSILoadContextTest.class);
		suite.addTestSuite(RegexpPackagePaserTest.class);
		//$JUnit-END$
		return suite;
	}

}
