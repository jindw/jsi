package org.xidea.jsi.test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.ScriptLoader;
import org.xidea.jsi.impl.DataJSIRoot;
import org.xidea.jsi.impl.FileJSIRoot;
import org.xidea.jsi.impl.DefaultJSILoadContext;
import org.xidea.jsi.JSIRoot;

import junit.framework.TestCase;

public class DataJSIImportTest extends TestCase {


	protected void setUp() throws Exception {
		super.setUp();
		
	}

	private String loadData(String file) throws UnsupportedEncodingException, IOException {
		java.io.InputStreamReader in = new java.io.InputStreamReader(this
				.getClass().getResourceAsStream(file), "utf-8");
		char[] buf = new char[1024];
		int count;
		StringWriter out = new StringWriter();
		while ((count = in.read(buf)) >= 0) {
			out.write(buf, 0, count);
		}
		return out.toString();
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testImport() throws UnsupportedEncodingException, IOException {
		String source = loadData("test2.xml");
		JSIRoot root = new DataJSIRoot(source);
		JSILoadContext loadContext = new DefaultJSILoadContext();
		String[] export = root.loadText("", "export").split("[,\\s]+");
		for (int i = 0; i < export.length; i++) {
			root.$import(export[i], loadContext);
		}
		StringBuilder buf = new StringBuilder(); 
		for (ScriptLoader file : loadContext.getScriptList()) {
			if(buf.length()>0){
				buf.append(',');
			}
			buf.append(file.getPath());
			
		}
		System.out.println(buf);
		assertEquals("导出顺序错误 :","org/xidea/sandbox/util/browser-info.js,org/xidea/sandbox/util/style-util.js", buf.toString());
	}
}
