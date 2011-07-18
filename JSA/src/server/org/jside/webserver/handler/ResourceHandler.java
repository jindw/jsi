package org.jside.webserver.handler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.jside.JSideWebServer;
import org.jside.webserver.RequestContext;
import org.jside.webserver.RequestUtil;
import org.xidea.jsi.web.JSIService;

public class ResourceHandler {
	public void execute() throws IOException {
		
	}

	public static void main(String[] args) {
		JSideWebServer.getInstance().addAction("/**", new ResourceHandler());
	}

}
