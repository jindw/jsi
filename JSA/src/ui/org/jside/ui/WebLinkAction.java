package org.jside.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import org.jside.JSideWebServer;
import org.jside.jsi.tools.JSA;

public class WebLinkAction extends MouseAdapter implements ActionListener {
	protected String path;
	private String host;

	private WebLinkAction(String host, String path) {
		this.host = host;
		this.path = path;
	}

	protected WebLinkAction() {
	}

	public void mouseClicked(MouseEvent e) {
		actionPerformed(null);
	}

	public void actionPerformed(ActionEvent e) {
		String url = path;
		if (url.length() > 0) {
			if (url.startsWith("/")) {
				url = "http://" + host + ':'
						+ JSideWebServer.getInstance().getPort() + url;
			}
			DesktopUtil.browse(url);
		}
	}

	private static class FileLinkAction extends WebLinkAction {
		public FileLinkAction(String path) {
			super(null, path);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				String url = this.path;
				URL base = JSideWebServer.getInstance().getWebBase().toURL();
				if(url.startsWith("/")){
					url = url.substring(1);
				}
				if (url.length() > 0) {
					base = new URL(base,url);
				}
				DesktopUtil.browse(base.toString());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	};

	public static WebLinkAction createFileLink(String path) {
		return new FileLinkAction(path);
	}

	public static WebLinkAction createLocalLink(String path) {
		return new WebLinkAction("localhost", path);
	}

	public static WebLinkAction createScriptLink(String path) {
		return new WebLinkAction(JSA.HOST, "/scripts/" + path);
	}
};