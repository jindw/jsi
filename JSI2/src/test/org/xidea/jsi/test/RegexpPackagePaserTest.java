package org.xidea.jsi.test;

import junit.framework.TestCase;

import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.FileJSIRoot;

public class RegexpPackagePaserTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testSplit() {
		assertEquals("111".split("[,]").length,1);
		assertEquals("111,12".split("[,]").length,2);
		assertEquals("111,12".split("[\\s,]")[0], "111");
		assertEquals("111".split("[\\s,]")[0], "111");
	}

	public void testParse() {
		JSIRoot context = new FileJSIRoot(RegexpPackagePaserTest.class
				.getResource("/").getFile(), "utf-8");
//		 String source = context.("org.xidea.jsdoc",
//		 JSIContext.PACKAGE_FILE);
//				
//		 new RegexpPackagePaser()
//		 .parse(source,context.requirePackage("org.xidea.jsdoc", true));
//		 // XMLEncoder out = new XMLEncoder(System.out);
//		 // out.writeObject(map);
//		 // out.flush();
//		 for (Iterator it = map.keySet().iterator(); it.hasNext();) {
//		 String method = (String) it.next();
//		 List<List<Object>> calls = map.get(method);
//		 System.out.println(method);
//		 for (Iterator it2 = calls.iterator(); it2.hasNext();) {
//		 System.out.println(it2.next());
//		 }
//		 System.out.println();
//		 System.out.println();
//					
//		 }
	}

}
