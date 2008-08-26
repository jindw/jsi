package org.xidea.jsi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class JSIScriptEngine extends AbstractScriptEngine {
	private final WebClient webClient;
	private final HtmlPage htmlPage;

	public JSIScriptEngine(final JSIRoot root) {
		webClient = new WebClient(BrowserVersion.FIREFOX_2) {
			String charset = "utf-8";

			public WebResponse loadWebResponse(
					final WebRequestSettings webRequestSettings)
					throws IOException {
				final URL url = webRequestSettings.getUrl();
				if (url.getProtocol().equals("file")
						&& url.getHost().equals("classpath")) {
					return makeWebResponseForClasspath(webRequestSettings
							.getUrl());
				} else {
					return super.loadWebResponse(webRequestSettings);
				}
			}

			private WebResponse makeWebResponseForClasspath(final URL url)
					throws IOException {
				String path = url.toString();
				if (path.length() > 17) {
					path = path.substring(17);
					if (path.startsWith("?path=")) {
						path = path.substring(6);
					}
				}
				int pos = path.lastIndexOf('/');
				String packageName = path.substring(0, Math.max(0, pos))
						.replace('/', '.');
				String source = root.loadText(packageName, path
						.substring(pos + 1));

				// BufferedInputStream inputStream = new
				// BufferedInputStream(String.class.getResourceAsStream(path));
				// String contentType =
				// URLConnection.guessContentTypeFromStream(inputStream);
				// String contentType =
				// URLConnection.guessContentTypeFromName(path);
				return new StringWebResponse(source, charset, url);
			}
		};
		try {
			htmlPage = (HtmlPage) webClient
					.getPage("file://classpath/org/xidea/jsi/test/htmlunit-test.html");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public Bindings createBindings() {
		return null;
	}

	public Object eval(String script, ScriptContext context)
			throws ScriptException {
		return webClient.getJavaScriptEngine().execute(htmlPage, script,
				"<unknow>", 1);
	}

	public Object eval(Reader reader, ScriptContext context)
			throws ScriptException {
		return null;
	}

	public ScriptEngineFactory getFactory() {
		return null;
	}

}
