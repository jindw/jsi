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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

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

class BinaryWebResponse extends WebResponseImpl {

	private static final long serialVersionUID = 8000117717229261957L;

	private final byte[] data_;

	private static WebResponseData getWebResponseData(final byte[] data,
			final String contentType) {
		final List<NameValuePair> compiledHeaders = new ArrayList<NameValuePair>();
		compiledHeaders.add(new NameValuePair("Content-Type", contentType));
		return new WebResponseData(data, HttpStatus.SC_OK, "OK",
				compiledHeaders);
	}

	BinaryWebResponse(final byte[] data, final URL originatingURL,
			final String contentType) {
		super(getWebResponseData(data, contentType), originatingURL,
				HttpMethod.GET, 0);
		data_ = data;
	}

	@Override
	public InputStream getContentAsStream() {
		return new ByteArrayInputStream(data_);
	}
}

public class HTMLUnitTest {

	@Test
	public void testHomePage() throws Exception {
		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_2) {
			String charset = "utf-8";
			public WebResponse loadWebResponse(
					final WebRequestSettings webRequestSettings)
					throws IOException {

				final String protocol = webRequestSettings.getUrl()
						.getProtocol();
				if (protocol.equals("classpath")) {
					return makeWebResponseForClasspath(webRequestSettings.getUrl());
				}else{
					return super.loadWebResponse(webRequestSettings);
				}
			}

			private WebResponse makeWebResponseForClasspath(final URL url) throws IOException {
				String path = url.toString();
				path = path.substring(12);
				if(path.startsWith("/?path=")){
					path = '/'+path.substring(7);
				}

				BufferedInputStream inputStream = new BufferedInputStream(String.class.getResourceAsStream(path));
	            String contentType = URLConnection.guessContentTypeFromStream(inputStream);
	            if(contentType == null){
	            	contentType = URLConnection.guessContentTypeFromName(path);
	            }
				final byte[] data = IOUtils.toByteArray(inputStream);
				return new BinaryWebResponse(data, url, contentType);
			}
		};
		final HtmlPage page = (HtmlPage) webClient.getPage("jar:///org/xidea/jsi/test/htmlunit-test.html");
		// .getPage("http://localhost/project/jsi/");//test/test-export.html");

		//webClient.getJavaScriptEngine().execute(page, loadText("/boot.js"),"/boot.js", 0);
		webClient.getJavaScriptEngine().execute(page,
				loadText("/org/xidea/jsi/test/htmlunit-test.js"),
				"/org/xidea/jsi/test/htmlunit-test.js", 0);
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
