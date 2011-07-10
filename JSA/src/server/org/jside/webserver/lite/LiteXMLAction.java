package org.jside.webserver.lite;

import org.jside.JSideWebServer;
import org.jside.webserver.action.TemplateAction;

public class LiteXMLAction {
	public static void main(String[] args){
		JSideWebServer.getInstance().addAction("/**.xhtml", new TemplateAction(null));
	}

}
