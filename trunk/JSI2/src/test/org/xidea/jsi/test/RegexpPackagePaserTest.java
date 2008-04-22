package org.xidea.jsi.test;

import java.beans.XMLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.FileJSIRoot;

import junit.framework.TestCase;

public class RegexpPackagePaserTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testParse() {
		JSIRoot context = new FileJSIRoot(RegexpPackagePaserTest.class
				.getResource("/").getFile(), "utf-8");
//		String source = context.("org.xidea.jsdoc",
//				JSIContext.PACKAGE_FILE);
//		
//		new RegexpPackagePaser()
//				.parse(source,context.requirePackage("org.xidea.jsdoc", true));
////		XMLEncoder out = new XMLEncoder(System.out);
////		out.writeObject(map);
////		out.flush();
//		for (Iterator it = map.keySet().iterator(); it.hasNext();) {
//			String method = (String) it.next();
//			List<List<Object>> calls = map.get(method);
//			System.out.println(method);
//			for (Iterator it2 = calls.iterator(); it2.hasNext();) {
//				System.out.println(it2.next());
//			}
//			System.out.println();
//			System.out.println();
//			
//		}
	}

}
