package org.jside.jsi.tools.ui.frame;


import java.awt.Window;

import org.jside.JSideWebServer;
import org.jside.jsi.tools.JSA;
import org.jside.ui.DesktopUtil;


public class BrowserDialog{

	public static void showHTML(Window frame,Package pkg, String path) {
		path = "/scripts/"+pkg.getName().replace('.', '/')+'/'+path;
		showHTML(frame,path);
	}

	public static void showHTML(Window instance, String path) {
		JSideWebServer ws = JSideWebServer.getInstance();
		path = "http://"+JSA.HOST+':'+ws.getPort()+path;
		DesktopUtil.browse(path);
	}

}
