package org.xidea.jsi.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collection;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.UnsupportedSyntaxException;
import org.xidea.jsi.impl.ScriptPackagePaser;
import org.xidea.jsi.impl.ScriptPackagePaser.PackageWapper;

import sun.org.mozilla.javascript.internal.EvaluatorException;
import sun.org.mozilla.javascript.internal.ScriptOrFnNode;


public class TestFindGlobals {
	public static final ScriptEngine engine = new javax.script.ScriptEngineManager()
			.getEngineByExtension("js");
	public static final String FIND_GLOBALS = "findGlobalsAsList";

	static {
		InputStream in = ScriptPackagePaser.class
				.getResourceAsStream("package-parser.js");
		try {
			InputStreamReader reader = new InputStreamReader(in, "utf-8");
			StringWriter out = new StringWriter();
			char[] cbuf = new char[1024];
			int count;
			while ((count = reader.read(cbuf)) >= 0) {
				out.write(cbuf, 0, count);
			}
			out.flush();
			
//			javax.script.SimpleScriptContext context = new javax.script.SimpleScriptContext();
			try {
				engine.eval(out.toString());
				engine.eval("function " + FIND_GLOBALS
						+ "(source){return toJavaObject(findGlobals(source))}"
						);
//				System.out.println(engine.createBindings());
//				System.out.println(engine.getContext().getClass());
//				System.out.println(engine.createBindings() instanceof Invocable);

			} catch (Exception e) {
				e.printStackTrace();
				throw new UnsupportedSyntaxException(e);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

					public EvaluatorException runtimeError(String arg0,
							String arg1, int arg2, String arg3, int arg4) {
						return null;
					}

					public void warning(String arg0, String arg1, int arg2,
							String arg3, int arg4) {
						
					}
					
				}
		);
		ScriptOrFnNode node = parser.parse(source, "<>", 0);
		node.getParamAndVarNames();
		node.getFunctionCount();
		
		return null;
	}

	@Test
	public void testX() {
		System.out.println(findGlobalsFromScript("var a,b,csdsd,sds,d"));
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}
