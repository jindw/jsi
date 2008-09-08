package org.xidea.jsi.test;

import static org.junit.Assert.fail;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.impl.DefaultJSIPackage;
import org.xidea.jsi.impl.Java6ScriptPackagePaser;

public class ScriptPackagePaserTest extends AbstractFileJSIRootTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testScript() throws ScriptException{
		//System.out.println(System.getProperty("java.endorsed.dirs"));
		ScriptEngine engine = new javax.script.ScriptEngineManager()
		.getEngineByExtension("js");
		System.out.println(engine.eval("/[/]/.test('/')"));
	}
	@Test
	public void testParse() {
		Java6ScriptPackagePaser paser = new Java6ScriptPackagePaser();
		paser
				.parse("this.addScript('a.js',['Class1','Class2'],'xxx','Base');"
						+ "this.addDependence('a.js','b.js');"
						+ "if(/is/.test(this.navigator))this.setImplementation(\"_v1_2\")");
		paser.setup(new DefaultJSIPackage(null, "pkg.test"));
	}

}
