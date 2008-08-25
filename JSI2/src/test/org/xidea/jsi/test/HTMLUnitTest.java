package org.xidea.jsi.test;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HTMLUnitTest {

	@Test
	public void testHomePage() throws Exception {
		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_2);
		final HtmlPage page = (HtmlPage) webClient.getPage("about:blank");
				//.getPage("http://localhost/project/jsi/");//test/test-export.html");

		webClient.getJavaScriptEngine().execute(page, 
				"document.body.innerHTML = ('<script src=\"classpath:///boot.js\"></script>')",
				"/boot.js", 0);
		webClient.getJavaScriptEngine().execute(page, 
				loadText("/boot.js"),
				"/boot.js", 0);
		webClient.getJavaScriptEngine().execute(page, 
				loadText("/org/xidea/jsi/test/htmlunit-test.js"),
				"/org/xidea/jsi/test/htmlunit-test.js", 0);
		//System.out.println(page.asXml());
		// assertEquals("HtmlUnit - Welcome to HtmlUnit", page.getTitleText());
	}

	private java.lang.String loadText(String path) throws IOException {
		java.io.InputStreamReader in =  new java.io.InputStreamReader(String.class.getResourceAsStream(path));
		java.nio.CharBuffer cbuf = java.nio.CharBuffer.allocate(1024);
		java.lang.StringBuffer buf = new java.lang.StringBuffer();
        int count = 0;
        while((count = in.read(cbuf))>0){
            buf.append(cbuf.array(),0,count);
            cbuf.clear();
        }
		return buf.toString();
	}
}
