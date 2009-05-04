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
		InputStream in = Java6ScriptPackagePaser.class.getResourceAsStream(
				"/org/xidea/jsidoc/util/find-globals.js"
				// "find-globals.js"
				);
		try {
			String source = loadText(in);
			try {
				engine.eval(source);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

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
	public void testFindFromDir() {
		File dir = new File(this.getClass().getResource("/").getFile());
		processDir(dir);
		//processDir(new File("D:\\eclipse\\workspace\\JSISide\\web\\scripts"));
		//processDir(new File("D:\\eclipse\\workspace\\JSI-thirdparty\\web\\scripts"));
		System.out.println("javaTime:" + this.javaTime);
		System.out.println("scriptTime:" + this.scriptTime);
	}

	private void processDir(final File files) {
		final Java6ScriptPackagePaser parser = new Java6ScriptPackagePaser(new DefaultPackage(null, "test"){
			@Override
			public String loadText(String scriptName) {
				if(JSIPackage.PACKAGE_FILE_NAME.equals(scriptName)){
					return "";
				}else{
					return scriptName;
				}
			}
			
		});
		files.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (file.isDirectory()) {
					file.listFiles(this);
				} else {
					if (file.getName().endsWith(".js")) {
						try {
							String source = loadText(new FileInputStream(file));
							System.out.println(file);
							long p1 = System.currentTimeMillis();
							Collection<String> result1 = null;
							@SuppressWarnings("unused")
							Collection<String> result2 = null;
							try {
								result1 = parser.findGlobals(source,"*");
							} catch (Exception e) {
								e.printStackTrace();
							}
							long p2 = System.currentTimeMillis();
							try {
								result2 = findGlobalsFromScript(source);
							} catch (Exception e) {
								e.printStackTrace();
							}
							long p3 = System.currentTimeMillis();
							javaTime += (int) (p2 - p1);
							scriptTime += (int) (p3 - p2);
							Assert.assertEquals("通过java方式和通过JS方式计算的全局变量应该相同",
									result1, result1);
						} catch (IOException e) {
							e.printStackTrace();
						}
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
