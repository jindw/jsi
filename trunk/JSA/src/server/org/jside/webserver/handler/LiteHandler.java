package org.jside.webserver.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.jside.JSideWebServer;
import org.jside.webserver.RequestContext;
import org.jside.webserver.RequestUtil;
import org.xidea.el.ExpressionFactory;
import org.xidea.lite.impl.HotTemplateEngine;
import org.xidea.lite.impl.ParseUtil;
import org.xidea.lite.parse.ParseConfig;
import org.xidea.lite.parse.ParseContext;
import org.xidea.lite.tools.ResourceManagerImpl;

public class LiteHandler {
	private File root;
	private ResourceManagerImpl manager;
	private HotTemplateEngine ht;
	private long lastModifiedTime = 0;

	public static void main(String[] args) {
		JSideWebServer.getInstance().addAction("/**", new LiteHandler());
	}

	public void execute() throws IOException {
		RequestContext context = init();
		final String uri = context.getRequestURI();
		if (uri.endsWith(".xhtml")) {
			OutputStream os = context.getOutputStream();
			Map<String, String> fm = ((ParseConfig) manager).getFeatureMap(uri);
			String encoding = fm.get(ParseContext.FEATURE_ENCODING);
			context.setEncoding(encoding);
			String mimeType = fm.get(ParseContext.FEATURE_MIME_TYPE);
			context.setMimeType(mimeType == null ? "text/html" : mimeType);
			OutputStreamWriter out = new OutputStreamWriter(os, encoding);
			Object data = loadData(root, uri);
			ht.render(uri, data, out);
			out.flush();
		} else {
			File file = new File(root, uri);
			if (file.isDirectory()) {
				RequestUtil.printResource();
			} else {
				Object result = manager.getFilteredContent(uri);
				RequestUtil.printResource(result, null);
			}
		}
	}

	private synchronized RequestContext init() throws IOException {
		RequestContext context = RequestUtil.get();
		URI base = context.getServer().getWebBase();
		File root = new File(base);
		out: if (root.equals(this.root)) {
			List<File> files = manager.getScriptFileList();
			long lastModifiedTime = 0;
			for (File f : files) {
				if (f.exists()) {
					lastModifiedTime = Math.max(f.lastModified(),
							lastModifiedTime);
				} else {
					break out;
				}
			}
			if (lastModifiedTime < this.lastModifiedTime) {
				return context;
			}

		}
		manager = new ResourceManagerImpl(base, base
				.resolve("WEB-INF/lite.xml"));
		ht = new HotTemplateEngine((ParseConfig) manager);
		this.root = root;
		lastModifiedTime = System.currentTimeMillis();
		return context;
	}

	private static Object loadData(final File root, String uri)
			throws IOException {
		String jsonpath = uri.replaceFirst(".\\w+$", ".json");
		Object data = new Object();
		if (jsonpath.endsWith(".json")) {
			File df = new File(root, jsonpath);
			if (df.exists()) {
				String source = ParseUtil.loadTextAndClose(new FileInputStream(
						df), null);
				data = ExpressionFactory.getInstance().create(source).evaluate(
						data);
			}
		}
		return data;
	}

}
