package org.jside.webserver.handler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.servlet.ServletException;

import org.jside.JSideWebServer;
import org.jside.webserver.RequestContext;
import org.jside.webserver.RequestUtil;
import org.xidea.lite.tools.ResourceManager;
import org.xidea.lite.tools.ResourceManagerImpl;

public class LiteHandler {
	static final String LITE_COMPILE_SERVICE = "/WEB-INF/service/lite-compile";
	private File root;
	ResourceManagerImpl manager;
	private TemplateServletImpl templateServlet;
	private long lastModifiedTime = 0;

	public static void main(String[] args) {
		JSideWebServer.getInstance().addAction("/**", new LiteHandler());
	}


	public void execute() throws IOException, ServletException {
		RequestContext context = init();
		final String uri = context.getRequestURI();
		if (uri.equals(LITE_COMPILE_SERVICE)) {
			
			templateServlet.compileLite(context);
		} else if (uri.endsWith(".xhtml")|| uri.equals("/WEB-INF/service/lite-service")) {
			templateServlet.service(context);
		} else {
			File file = new File(root, uri);
			if (file.isDirectory()) {
				templateServlet.service(context);
			} else {
				Object result = manager.getFilteredContent(uri);
				RequestUtil.printResource(result, null);
			}
		}
	}


	synchronized RequestContext init() throws IOException, ServletException {
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
		templateServlet = new TemplateServletImpl(manager);
		this.root = root;
		lastModifiedTime = System.currentTimeMillis();
		return context;
	}



	public ResourceManager getResourceManager() {
		return this.manager;
	}

}
