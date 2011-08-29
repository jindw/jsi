package org.jside.webserver.handler;

import java.io.IOException;
import java.io.OutputStream;


import org.jside.JSideWebServer;
import org.jside.webserver.RequestContext;
import org.jside.webserver.servlet.ServletContextImpl;

public class VelocityHandler {
	private LiteHandler lite;
	private JSideWebServer server = JSideWebServer.getInstance();
	public static void main(String[] args) {
		JSideWebServer server = JSideWebServer.getInstance();
		LiteHandler lite = server.getHandler(LiteHandler.class);
		server.addAction("/**.vm", new VelocityHandler(lite));
	}

	public VelocityHandler(LiteHandler lite) {
		this.lite = lite;
	}

	public void execute() throws IOException, Exception {
		RequestContext context = lite.init();
		final String uri = context.getRequestURI();
		OutputStream os = context.getOutputStream();
		ServletContextImpl sc = new ServletContextImpl();
	}
}
