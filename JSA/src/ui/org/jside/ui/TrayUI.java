package org.jside.ui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JPopupMenu;

import org.jside.JSide;
import org.jside.JSideListener;
import org.jside.JSideWebServer;


public class TrayUI {
	protected final String serverName = "JSA 压缩服务器";
	protected static TrayUI instance;
	protected static boolean supported;
	static {
		try {
			supported = java.awt.SystemTray.isSupported();
		} catch (Throwable ex) {
		}
	}
	
	protected TrayIcon trayIcon;
	protected JPopupMenu popup = ContextMenu.getInstance();
	
	protected MouseListener clickAction = new MouseAdapter(){
		@Override
		public void mouseClicked(MouseEvent e) {
			JSide.fireEvent(JSideListener.TrayClick.class, e);
		}
		
	};
	public static TrayUI getInstance() {
		if (instance == null) {
			new TrayUI().initialize();
		}
		return instance;
	}
	public static boolean isRunning() {
		return instance != null;
	}

	public static boolean isSupported() {
		return supported;
	}
	public static void showTrayMessage(String title, String message) {
		((TrayIcon)getInstance().trayIcon).displayMessage(title, message,
				java.awt.TrayIcon.MessageType.INFO);
	}


	protected TrayUI() {
		instance = this;
	}
	private void initialize() {
		final JSideWebServer ws = JSideWebServer.getInstance();
		java.awt.SystemTray st = java.awt.SystemTray.getSystemTray();
		
		Image image = getLogo();
		Dimension size = st.getTrayIconSize();
		image = image.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_DEFAULT);
		this.trayIcon = JTrayIconFactory.createTrayIcon(image,popup);
		trayIcon.addMouseListener(clickAction );
		trayIcon.setToolTip(serverName);
		// ti.setPopupMenu(popup);
		try {
			st.add(trayIcon);
		} catch (AWTException e1) {
			e1.printStackTrace();
		}
		showTrayMessage("系统已启动", "服务地址:" + ws.getHomePage()+"\r\n"+"资源目录："+ws.getWebBase());
	}
	protected Image getLogo() {
//		Image image = java.awt.Toolkit.getDefaultToolkit().getImage(
//				getClass().getResource(
//						"/org/xidea/jsidoc/styles/item-package-ref.gif"));
		int width = 88;
		int height = 100;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE );
		Graphics g = image.getGraphics();
		//g.setColor(Color.WHITE);
		//g.fillRect(0,0,100,100);
		g.setColor(new Color(0,0x44,0x88));
		g.fill3DRect(0,0,19,100,true);
		g.fillRect(0,81,42,19);
		g.fillRect(23,0,19,19);
		g.fillRect(23,23,19,54);
		g.fillRect(46,0,42,19);
		g.fillRect(46,23,19,77);
		g.fillRect(69,81,19,19);//88
		g.finalize();
		return image;
	}

}
