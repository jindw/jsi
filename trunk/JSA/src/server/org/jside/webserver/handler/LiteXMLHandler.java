package org.jside.webserver.handler;

import org.jside.JSideWebServer;
import org.jside.webserver.action.TemplateAction;

public class LiteXMLHandler {
	public static void main(String[] args){
		JSideWebServer.getInstance().addAction("/**.xhtml", new TemplateAction(null));
	}

}
