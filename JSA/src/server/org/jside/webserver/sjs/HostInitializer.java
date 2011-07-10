//package org.jside.webserver.sjs;
//
//import java.io.IOException;
//import java.net.URL;
//
//import org.jside.JSideWebServer;
//import org.jside.webserver.RequestContext;
//
//public class HostInitializer extends JSFilterHandler{
//
//	private static final String DEFAULT_SJS = "/WEB-INF/initializer.s.js";
//
//	public HostInitializer(String script) {
//		super(script);
//	}
//	public void execute() throws IOException{
//		RequestContext context = RequestContext.get();
//		context.getHeaders()
//		
//	}
//	public static void main(String[] args){
//		JSFilterHandler handler;
//		if(args == null || args.length==0){
//			handler = new JSFilterHandler(DEFAULT_SJS);
//		}else{
//			handler = new JSFilterHandler(args[0]);
//		}
//		JSideWebServer.getInstance().addAction("/**", new HostInitializer());
//	}
//}
