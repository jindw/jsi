package org.xidea.jsi.test;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HTMLUnitTest {

	@Test
	public void testHomePage() throws Exception {
		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_2);
		final HtmlPage page = (HtmlPage) webClient
				.getPage("http://localhost/project/jsi/test/test-export.html");
		//System.out.println(page.asXml());
		// assertEquals("HtmlUnit - Welcome to HtmlUnit", page.getTitleText());
	}
}
