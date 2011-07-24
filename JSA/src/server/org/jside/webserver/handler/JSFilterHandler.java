package org.jside.webserver.handler;

import java.io.IOException;
import java.net.URI;

import org.jside.JSideWebServer;
import org.jside.webserver.RequestContext;
import org.jside.webserver.RequestUtil;

public class JSFilterHandler extends SJSHandler{

	private static final String DEFAULT_SJS = "/WEB-INF/default-filter.s.js";
	private String script;

	public JSFilterHandler(String script) {
		this.script = script;
	}
	public void execute() throws IOException{
		RequestContext context = RequestUtil.get();
		URI resource = context.getResource(script);
		if(resource!=null){
			super.execute(context, script);
		}
	}
	public static void main(String[] args){
		JSFilterHandler handler;
		if(args == null || args.length==0){
			handler = new JSFilterHandler(DEFAULT_SJS);
		}else{
			handler = new JSFilterHandler(args[0]);
		}
		JSideWebServer.getInstance().addAction("/**", handler);
	}
}
