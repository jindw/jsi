package org.xidea.jsi.impl.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.impl.v2.FileRoot;
import org.xidea.jsi.impl.v2.JSIPackage;

public class FilePackageFinderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFindPackageList() {
		File root = new File(this.getClass().getResource("/").getFile());
		List<String> list1 = FileRoot.findPackageList(root);
		ArrayList<String> list2 = new ArrayList<String>();
		walkPackageTree(root,null,list2);

		Collections.sort(list1);
		Collections.sort(list2);
		System.out.println(list1);
		System.out.println(list2);
		assertEquals(list1, list2);
	}
	private static void walkPackageTree(final File dir, String prefix,
			final List<String> result) {
		final String subPrefix;
		if (prefix == null) {
			subPrefix = "";
		} else if (prefix.length() == 0) {
			subPrefix = dir.getName();
		} else {
			subPrefix = prefix + '.' + dir.getName();
		}
		File packageFile = new File(dir, JSIPackage.PACKAGE_FILE_NAME);
		if (packageFile.exists()) {
			result.add(subPrefix);
		}
		dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (file.isDirectory()) {
					String name = file.getName();
					if (!name.startsWith(".")) {
						walkPackageTree(file, subPrefix, result);
					}
				}
				return false;
			}
		});
	}
}
