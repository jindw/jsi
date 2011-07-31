package org.jside;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jside.jsi.tools.ui.frame.JSFormatFilter;
import org.jside.ui.ContextMenu;
import org.jside.ui.DockUI;
import org.jside.ui.SplashWindow;
import org.jside.ui.DesktopUtil;
import org.jside.ui.WebLinkAction;
import org.jside.webserver.action.ActionWebServer;
import org.jside.webserver.proxy.ProxyHandler;

public class JSideWebServer extends ActionWebServer {
	/**
	 * 发出方：JSideWebServer#openListener
	 * 参数 File
	 */
	public static JSideListener<File> rootChangedAction = new JSideListener.WebRootChange(){
		public boolean execute(File source){
			try {
				JSideConfig.getInstance().setWebRoot(source.getCanonicalPath());
				JSideConfig.getInstance().save();
				instance.setWebBase(source);
				instance.reset();

				DockUI dock = DockUI.getInstance();
				dock.updateMessage("成功切换目录:", source.getName());
				// UIUtil.alert(dock, "调试服务器主目录修改为:\r\n" +
				// file.getAbsolutePath());
				if (DesktopUtil.confirm("文件切换成功是否打开完展首页")) {
					dock.updateMessage("打开首页:", "....");
					DesktopUtil.browse(JSideWebServer.getInstance()
							.getHomePage());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
	};
	private static final JSideListener<File> openListener = new JSideListener.FileOpen() {
		public boolean execute(File file ) {
			if (file.isDirectory()) {
				rootChangedAction.execute(file);
				return true;
			} else {
				return false;
			}
		}
	};

	private static final JSideListener<File> beforeOpenListener = new JSideListener.BeforeFileOpen() {
		public boolean execute(File file) {
			if (file.isDirectory()) {
				DockUI.getInstance().updateMessage("切换主目录");
				return true;
			} else {
				return false;
			}
		}

	};

	public static WebLinkAction browseAction = WebLinkAction
			.createFileLink("/");
	public static final String SCRIPT_BASE_KEY = "scriptBase";
	private static Log log = LogFactory.getLog(JSideWebServer.class);
	private static JSideWebServer instance;

	protected JSideWebServer(File webBase) throws MalformedURLException {
		super(webBase.toURI());
		instance = this;
		String port = System.getProperty("jside.port");
		if (port != null) {
			this.defaultPort = Integer.parseInt(port);
		}
		invocationList.add(ProxyHandler.getInstance());
		ProxyHandler.getInstance().addResponseContentFilter("**.js", new JSFormatFilter());
		ContextMenu dock = ContextMenu.getInstance();
		dock.addMenuItem("浏览网站", null, WebLinkAction.createLocalLink("/"));
		dock.addMenuItem("浏览文件", null, browseAction);
		JSide.addListener(rootChangedAction);
		JSide.addListener( beforeOpenListener);
		// jside.addListener(JSideListener.DOCK_CLICK, new WebLinkAction("/"));
		JSide.addListener(openListener);
	}

	@Override
	public void start(){
		SplashWindow.showSplash("打开web服务器...");
		String thread = System.getProperty("jside.web.thread");
		if(thread!=null){
			super.start(Integer.parseInt(thread));
		}else{
			super.start();
		}
		while (true) {
			if (this.getPort() > 0) {
				break;
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				log.warn(e);
			}
		}
		SplashWindow.closeSplash();
	}

	public void reset() {
		// super.reset();
		JSide.fireEvent(JSideListener.WebServerReset.class, this);
	}

	public String getHomePage() {
		return "http://localhost:" + this.getPort();
	}

	public void setWebBase(File webBase) {
		super.webBase = webBase.toURI();
	}

	public static JSideWebServer getInstance() {
		if(instance == null){
			String root = JSideConfig.getInstance().getWebRoot();
			if (root == null) {
				root = ".";
			}
			try {
				instance = new JSideWebServer(new File(root)
						.getCanonicalFile());
			} catch (IOException e) {
				log.error(e);
			}
			instance.start();
		}
		return instance;
	}

	public static void main(String[] args) throws Exception,
			MalformedURLException, IOException {
		getInstance();
	}
}
