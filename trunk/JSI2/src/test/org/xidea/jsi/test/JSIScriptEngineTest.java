package org.xidea.jsi.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptContext;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xidea.jsi.JSIScriptEngine;
import org.xidea.jsi.impl.ClasspathJSIRoot;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.WebResponseImpl;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.UrlUtils;


public class JSIScriptEngineTest {

	@Test
	public void testHomePage() throws Exception {
		JSIScriptEngine engine = new JSIScriptEngine(new ClasspathJSIRoot());
		//webClient.getJavaScriptEngine().execute(page, loadText("/boot.js"),"/boot.js", 0);
		engine.eval(loadText("/org/xidea/jsi/test/htmlunit-test.js"),(ScriptContext)null);
		// System.out.println(page.asXml());
		// assertEquals("HtmlUnit - Welcome to HtmlUnit", page.getTitleText());
	}
	private java.lang.String loadText(String path) throws IOException {
		java.io.InputStreamReader in = new java.io.InputStreamReader(
				String.class.getResourceAsStream(path));
		java.nio.CharBuffer cbuf = java.nio.CharBuffer.allocate(1024);
		java.lang.StringBuffer buf = new java.lang.StringBuffer();
		int count = 0;
		while ((count = in.read(cbuf)) > 0) {
			buf.append(cbuf.array(), 0, count);
			cbuf.clear();
		}
		return buf.toString();
	}
}
