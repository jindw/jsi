package org.jside.webserver.handler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import org.jside.JSideWebServer;
import org.jside.webserver.RequestContext;
import org.jside.webserver.RequestUtil;
import org.jside.webserver.sjs.JSExcutor;

public class SJSHandler {

	public void execute() throws IOException{
		RequestContext context = RequestUtil.get();
		String uri = context.getRequestURI();
		execute(context, uri);
	}

	protected void execute(RequestContext context, String uri) throws IOException {
		URI resource = context.getResource(uri);
		HashMap<String, Object> globals = new HashMap<String, Object>();
		globals.put("context", context);
		if("file".equals(resource.getScheme())){
			if(!new File(resource).exists()){
				return;
			}
		}
		JSExcutor.getCurrentInstance().eval(resource.toURL(),globals);
	}
	public static void main(String[] args){
		JSideWebServer.getInstance().addAction("/**.s.js", new SJSHandler());
	}
}
