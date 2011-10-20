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

/**
 * @deprecated
 * @author jindawei
 *
 */
public class JSIHandler {
	///static/js/**
	///scripts/**
	private static String[] prefixes = {"/static/js/","/scripts/"} ;
	private Map<URI, JSIService> serviceMap = new HashMap<URI, JSIService>(); 

	public void execute() throws IOException {
		RequestContext context = RequestUtil.get();
		String uri = context.getRequestURI();
		URI base = context.getServer().getWebBase();
		for (String prefix : prefixes) {
			if (uri.startsWith(prefix)) {
				URI file = base.resolve(prefix.substring(1));
				JSIService js = serviceMap.get(file);
				if(js == null){
					js = new JSIService();
					js.addSource(new File(file));
					js.addSource(new File(new File(base),"WEB-INF/classes"));
					js.addLib(new File(new File(base),"WEB-INF/lib"));
				}
				js.service(uri.substring(prefix.length()), context.getParams(),
						context.getOutputStream(), context);
			}
		}
	}

	public static void main(String[] args) {
		JSideWebServer.getInstance().addAction("/**", new JSIHandler());
	}

}
