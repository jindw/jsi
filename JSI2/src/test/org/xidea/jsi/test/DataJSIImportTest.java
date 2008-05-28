package org.xidea.jsi.test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;


import org.junit.Assert;
import org.junit.Test;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.ScriptLoader;
import org.xidea.jsi.impl.DataJSIRoot;
import org.xidea.jsi.impl.DefaultJSILoadContext;

public class DataJSIImportTest {
	@Test
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
		Assert.assertEquals("导出顺序错误 :","org/xidea/sandbox/util/browser-info.js,org/xidea/sandbox/util/style-util.js", buf.toString());
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

}
