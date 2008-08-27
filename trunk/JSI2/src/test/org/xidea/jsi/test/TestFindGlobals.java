package org.xidea.jsi.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.UnsupportedSyntaxException;
import org.xidea.jsi.impl.ScriptPackagePaser;



public class TestFindGlobals {
	public static final ScriptEngine engine = new javax.script.ScriptEngineManager()
			.getEngineByExtension("js");
	public static final String FIND_GLOBALS = "findGlobalsAsList";

	static {
		InputStream in = ScriptPackagePaser.class
				.getResourceAsStream("find-globals.js");
		try {
			String source = loadText(in);
			try {
				engine.eval(source);
			} catch (Exception e) {
				e.printStackTrace();
				throw new UnsupportedSyntaxException(e);
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

	private Collection<String> findGlobalsFromScript(String source) {
		try {
			return (Collection<String>) ((Invocable) engine).invokeFunction(
					FIND_GLOBALS, source);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private Collection<String> findGlobalsFromJava(String source) {
		sun.org.mozilla.javascript.internal.CompilerEnvirons env = new sun.org.mozilla.javascript.internal.CompilerEnvirons();
		env.setReservedKeywordAsIdentifier(true);
		sun.org.mozilla.javascript.internal.Parser parser = new sun.org.mozilla.javascript.internal.Parser(
				env,
				new sun.org.mozilla.javascript.internal.ErrorReporter(){

					public void error(String arg0, String arg1, int arg2,
							String arg3, int arg4) {
					}

					public sun.org.mozilla.javascript.internal.EvaluatorException runtimeError(String arg0,
							String arg1, int arg2, String arg3, int arg4) {
						return null;
					}

					public void warning(String arg0, String arg1, int arg2,
							String arg3, int arg4) {
						
					}
					
				}
		);
		sun.org.mozilla.javascript.internal.ScriptOrFnNode node = parser.parse(source, "<>", 0);
		HashSet<String> result = new HashSet<String>();
		result.addAll(Arrays.asList(node.getParamAndVarNames()));
		int count = node.getFunctionCount();
		while((count--)>0){
			String name = node.getFunctionNode(count).getFunctionName();
			if(name!=null && name.length()>0){
				result.add(name);
			}
		}
		return result;
	}

	@Test
	public void testFindFromDir(){
		File files = new File("D:\\eclipse\\workspace\\JSI2\\web\\scripts");
		files.listFiles(new FileFilter(){
			public boolean accept(File file) {
				if(file.isDirectory()){
					file.listFiles(this);
				}else{
					if(file.getName().endsWith(".js")){
						try {
							String source = loadText(new FileInputStream(file));
							System.out.println(file);
							Collection<String> result1 = findGlobalsFromJava(source);
							System.out.println(result1);
							Collection<String> result2 = findGlobalsFromScript(source);
							System.out.println(result2);
							Assert.assertEquals("通过java方式和通过JS方式计算的全局变量应该相同",result1, result1);
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
	}

	@After
	public void tearDown() throws Exception {
	}

}