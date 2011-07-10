package org.jside.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

import org.xidea.commons.i18n.swing.MessageBase;


public class SplashWindow extends JWindow {
	private static int WIDTH = 240;
	private static int HEIGHT = 80;
	protected JLabel title = new JLabel("JSI 工具集正在启动", SwingConstants.LEFT);
	protected JLabel description = new JLabel("JSI 工具集正在启动", SwingConstants.LEFT);
	protected Icon logo = MessageBase.loadIcon(SplashWindow.class,
			"icon/jside.png");
	
	public Icon getLogo() {
		return logo;
	}
	private static SplashWindow instance;
	private static SplashWindow getInstance(){
		if(instance == null){
			instance = new SplashWindow(null);
		}
		return instance;
	}

	public static void showSplash(Window parent,String description) {
		if(instance != null){
			instance.dispose();
			//System.out.println("dispose:"+instance.description.getText());
			
			instance =  new SplashWindow(parent);
		}
		showSplash(description);
	}
	public static void showSplash(String description) {
		SplashWindow windowSplash = getInstance();
		windowSplash.description.setText(description);
		//System.out.println(windowSplash.description.getText());
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension scmSize = toolkit.getScreenSize();
		windowSplash.pack();
		windowSplash.setSize(WIDTH, HEIGHT);
		windowSplash.setLocation(scmSize.width / 2 - WIDTH/2,
				scmSize.height / 2 - HEIGHT);
		windowSplash.setAlwaysOnTop(true);
		windowSplash.setVisible(true);
		windowSplash.toFront();
	}

	public static void closeSplash() {
		SplashWindow windowSplash = getInstance();
		
		windowSplash.setVisible(false);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected SplashWindow(Window parent) {
		super(parent);
		instance = this;
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		DesktopUtil.setOpacity(this,0.8f);
		Container container = this.getContentPane();
		container.setBackground(Color.WHITE);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 14));
		container.add(title, BorderLayout.CENTER); // 增加图片
		description.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.YELLOW));
		description.setFont(description.getFont().deriveFont(Font.ITALIC, 12));
		container.add(description, BorderLayout.SOUTH);
		title.setIcon(getLogo());
		title.setOpaque(true);
	}

}
