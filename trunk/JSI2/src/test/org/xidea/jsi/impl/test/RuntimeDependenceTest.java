package org.xidea.jsi.impl.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xidea.el.json.JSONEncoder;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.impl.DataJSIRoot;

public class RuntimeDependenceTest {
	ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
	Bindings context;

	@Before
	public void setUp() throws Exception {
		context = engine.createBindings();
		engine.eval(new InputStreamReader(this.getClass().getResourceAsStream(
				"/boot.js"), "utf-8"), context);
		eval("function XMLHttpRequest(){};" +
				"XMLHttpRequest.prototype=" +
				"{" +
				"open:function(method,url,asyn){print(url)}" +
				"};"+
				"var confirm = print;");
	}

	public void eval(String code) {
		try {
			System.out.println(code);
			engine.eval(code, context);
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}

	}

	public void addScriptMap(Map<String, String> data) throws Exception {
		for (String path : data.keySet()) {
			int i = path.lastIndexOf('/');
			String pkg = path.substring(0, i).replace('/', '.');
			String file = path.substring(i + 1);

			if("__package__.js".equals(file)){
				file= "";
			}
			eval("$JSI.preload('" + pkg + "','" + file + "',"
					+ JSONEncoder.encode(data.get(path)) + ")");
		}
	}

	@Test
	public void testDoubleBeforeDependence()
			throws UnsupportedEncodingException, Exception {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("test/test1.js", "result.add(this.name);function test1(){}");
		data.put("test/test2.js", "result.add(this.name);function test2(){}");
		data.put("test/test3.js", "result.add(this.name);function test3(){}");
		data.put("test/__package__.js",
				"this.addScript('test1.js','test1',['test2.js','test3.js']);"
						+ "this.addScript('test2.js','test2',0,'test1.js');"
						+ "this.addScript('test3.js','test3',0,'test1.js');");
		addScriptMap(data);
		ArrayList<Number> result = new ArrayList<Number>();
		context.put("result", result);
		eval("$import('test/test1.js')");
		int i1 = result.indexOf("test1.js");
		int i2 = result.indexOf("test2.js");
		int i3 = result.indexOf("test3.js");
		Assert.assertTrue(result.size()==3);
		Assert.assertTrue(i1>i2);
		Assert.assertTrue(i1>i3);
		System.out.println(result);
	}

}
