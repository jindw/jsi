package org.xidea.jsi.impl.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.impl.DefaultPackage;
import org.xidea.jsi.impl.FileRoot;
import org.xidea.jsi.impl.Java6ScriptPackagePaser;

public class TestFindGlobals {
	public static final ScriptEngine engine = new javax.script.ScriptEngineManager()
			.getEngineByExtension("js");
	public static final String FIND_GLOBALS = "findGlobalsAsList";

	static {
		InputStream in = Java6ScriptPackagePaser.class
				.getResourceAsStream("/boot.js"
				// "find-globals.js"
				);
		try {
			String source = loadText(in);
			try {
				engine.eval(source);
				engine
						.eval("$import('org.xidea.jsidoc.util:findGlobalsAsList')");
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	final Java6ScriptPackagePaser parser = new Java6ScriptPackagePaser(
			new DefaultPackage(null, "test") {
				@Override
				public String loadText(String scriptName) {
					if (JSIPackage.PACKAGE_FILE_NAME.equals(scriptName)) {
						return "";
					} else {
						return scriptName;
					}
				}

			});

	private static String loadText(InputStream in)
			throws UnsupportedEncodingException, IOException {
		InputStreamReader reader = new InputStreamReader(in, "utf-8");
		StringWriter out = new StringWriter();
		char[] cbuf = new char[1024];
		int count;
		while ((count = reader.read(cbuf)) >= 0) {
			out.write(cbuf, 0, count);
		}
		out.flush();
		return out.toString();
	}

	@SuppressWarnings("unchecked")
	private Collection<String> findGlobalsFromScript(String source) {
		try {
			return (Collection<String>) ((Invocable) engine).invokeFunction(
					FIND_GLOBALS, source);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private int javaTime = 0;
	private int scriptTime = 0;

	@Test
	public void testFindFile() {
		testText("tagAlias[info.alias[j]] = n");
		testFile(new File(
				"C:/Users/jindw/workspace/JSI2/web/WEB-INF/classes/org/xidea/jsidoc/doc-entry.js"));
		// testFile( new
		// File("C:/Users/jindw/workspace/JSI2/web/WEB-INF/classes/example/internal/guest.js"));
	}

	@Test
	public void testFindFromDir() throws IOException {
		File dir = new File(this.getClass().getResource("/").getFile());

		processDir(new File("D:\\eclipse\\workspace\\JSISide\\web\\scripts"));
		// processDir(new
		// File("D:\\eclipse\\workspace\\JSI-thirdparty\\web\\scripts"));
		processDir(new File(dir, "../../../build"));
		System.out.println("javaTime:" + this.javaTime);
		System.out.println("scriptTime:" + this.scriptTime);
	}

	private void testFile(File file) {
		try {
			String source = loadText(new FileInputStream(file));
			System.out.println(file);
			testText(source);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void testText(String source) {
		long p1 = System.currentTimeMillis();
		Collection<String> result1 = Collections.EMPTY_LIST;
		@SuppressWarnings("unused")
		Collection<String> result2 = null;
		result1 = parser.findGlobals(source, "*");
		long p2 = System.currentTimeMillis();
		result2 = findGlobalsFromScript(source);
		long p3 = System.currentTimeMillis();
		javaTime += (int) (p2 - p1);
		scriptTime += (int) (p3 - p2);
		Assert.assertEquals("通过java方式和通过JS方式计算的全局变量应该相同", result1, result1);
		System.out.println("结果一致：" + result1);
	}

	private void processDir(final File files) throws IOException {
		while (true) {
			System.out.println("是否测试路径：" + files + "(y 继续 n 退出)");
			int in = System.in.read();
			System.out.println((char)in);
			if (in == 'y' || in == 'Y') {
				break;
			} else if (in == 'n' || in == 'N') {
				return;
			}
		}
		files.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (file.isDirectory()) {
					file.listFiles(this);
				} else {
					if (file.getName().endsWith(".js")) {
						testFile(file);
					}
				}
				return false;
			}

		});
	}

	@Before
	public void setUp() throws Exception {
		this.javaTime = 0;
		this.scriptTime = 0;
	}

	@After
	public void tearDown() throws Exception {
	}

}
