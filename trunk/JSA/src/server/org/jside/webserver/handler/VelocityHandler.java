package org.jside.webserver.handler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;


import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;
import org.jside.JSideWebServer;
import org.jside.webserver.RequestContext;

public class VelocityHandler {
	private LiteHandler lite;
	private RuntimeInstance engine;
	public static void main(String[] args) {
		JSideWebServer server = JSideWebServer.getInstance();
		LiteHandler lite = server.getHandler(LiteHandler.class);
		server.addAction("/**.vm", new VelocityHandler(lite));
	}

	public VelocityHandler(LiteHandler lite) {
		this.lite = lite;
		engine = VelocityResourceLoader.init();
		
	}

	public void execute() throws IOException, Exception {
		RequestContext context = lite.init();
		final String uri = context.getRequestURI();
		OutputStream os = context.getOutputStream();
		OutputStreamWriter out = new OutputStreamWriter(os,"utf-8");
		Template template = engine.getTemplate(uri,"utf-8");
		Map<String, Object> data = Util.loadData(new File(context.getServer().getWebBase()), uri);
		template.merge(new VelocityContext(data), out);
		
	}
}
