package org.jside.ui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;

import org.jside.JSide;
import org.jside.JSideListener;
import org.xidea.commons.i18n.CharsetSelector;
import org.xidea.commons.i18n.swing.MessageBase;

public class DockUI extends JDialog {
	protected Image icon = ((ImageIcon) MessageBase.loadIcon(DockUI.class,
			"icon/jside.png")).getImage();
	private static int WIDTH = 80;
	private static int HEIGHT = 100;
	private static final long serialVersionUID = 1L;
	private static DockUI instance;

	private JPopupMenu popup = ContextMenu.getInstance();
	protected final CharsetSelector charsetSelector = new UICharsetSelector(
			this);
	private Font titleFont = new Font(Font.DIALOG, Font.BOLD, 13);
	private Font messageFont = new Font(Font.DIALOG, Font.ITALIC, 9);
	private String[] messages;

	protected DockUI() {
		instance = this;
		enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
		this.setDropTarget(new DropTarget(this, dropAction));
		this.addMouseListener(mouseAction);
		this.addMouseMotionListener(mouseAction);
		this.setUndecorated(true);
	}

	public static DockUI getInstance() {
		if (instance == null) {
			instance = new DockUI();
		}
		return instance;
	}

	public void updateMessage(String... messages) {
		this.messages = messages;
		this.repaint();
	}

	@Override
	public void paint(Graphics g) {
		//g.drawImage(getImage(), 0, 0, this);
		g.setColor(new Color(0,0x44,0x88));
		g.fill3DRect(0,0,19,100,true);
		g.fillRect(0,81,42,19);
		g.fillRect(23,0,19,19);
		g.fillRect(23,23,19,54);
		g.fillRect(46,0,42,19);
		g.fillRect(46,23,19,77);
		g.fillRect(69,81,19,19);
		
//		g.drawRect(x, y, width, height)
		
		if (this.messages != null && this.messages.length > 0) {
			g.setColor(Color.BLACK);
			int offsetTop = g.getFont().getSize() + 10;
			g.setFont(titleFont);
			for (String message : messages) {
				g.drawString(message, 10, offsetTop);
				offsetTop += g.getFont().getSize() + 10;
				g.setFont(messageFont);
			}
		}
	}
	protected Image getImage() {
		return icon;
	}

	public static void showDock() {
		DockUI dock = getInstance();
		DesktopUtil.setOpacity(dock, 3000.9, 3000.85, 0.3);
		DesktopUtil.setOpaque(dock, false);
		// dock.pack();
		dock.setSize(WIDTH, HEIGHT);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension scmSize = toolkit.getScreenSize();
		dock.setLocation(scmSize.width - WIDTH, scmSize.height / 2 - HEIGHT);
		dock.setAlwaysOnTop(true);
		dock.setVisible(true);
		dock.toFront();
	}

	public DropTargetListener dropAction = new FileDropTargetAction() {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			DesktopUtil.setOpacity(DockUI.this, 100.3, 1);
			super.dragEnter(dtde);
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
			super.dragExit(dte);
			DesktopUtil.setOpacity(DockUI.this, 100.99, 0.3);
			// TODO:remove hack
			if (messages != null && messages.length == 1) {
				updateMessage();
			}
		}

		@Override
		protected boolean accept(File file) {
			return JSide.fireEvent(
					JSideListener.BeforeFileOpen.class, file);
		}

		protected void openFile(final File file) {
			new Thread() {
				public void run() {
					try {
						JSide.fireEvent(
								JSideListener.FileOpen.class, file);
						Thread.sleep(3000);
						updateMessage();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();

		}

	};

	private MouseAction mouseAction = new MouseAction();

	private class MouseAction implements MouseMotionListener, MouseListener {
		private int x;
		private int y;
		private int h;
		private int w;

		public void mouseClicked(final MouseEvent e) {
			JSide.fireEvent(JSideListener.DockClick.class, e);
		}

		public void mousePressed(MouseEvent e) {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Dimension scmSize = toolkit.getScreenSize();
			this.h = (int)scmSize.getHeight();
			this.w = (int)scmSize.getWidth();
			this.x = getXOnScreen(e);
			this.y = getYOnScreen(e);
			menuPress(e);
		}

		public void mouseReleased(MouseEvent e) {
			menuPress(e);
		}

		private void menuPress(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(DockUI.this, e.getX(), e.getY());
				// DockUI.this.toFront();
			} else {
				e.consume();
			}
		}

		public void mouseEntered(MouseEvent e) {
			DesktopUtil.setOpacity(DockUI.this, 500.3, 1);
		}

		public void mouseExited(MouseEvent e) {
			DesktopUtil.setOpacity(DockUI.this, 300.99, 0.3);
		}


		int getXOnScreen(MouseEvent e) {
			DockUI ui = DockUI.this;
			Point loc = ui.getLocation();
			return (int) loc.getX() + e.getX();
		}

		int getYOnScreen(MouseEvent e) {
			DockUI ui = DockUI.this;
			Point loc = ui.getLocation();
			return (int) loc.getY() + e.getY();
		}

		public void mouseDragged(MouseEvent e) {
			if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) > 0) {
				DockUI ui = DockUI.this;
				Point loc = ui.getLocation();
				int x2 = getXOnScreen(e);
				int y2 = getYOnScreen(e);
				int top = Math.abs(y2);
				int right = Math.abs(w-x2);
				int buttom = Math.abs(h-y2);
				int left = Math.abs(x2);
				int min = Math.min(top,Math.min(right,Math.min(buttom,left)));
				int lx = (int)loc.getX();
				int ly = (int)loc.getY();
				if(top == min){
					ui.setLocation(lx+x2-x, Math.min(ly,0));
				}else if(right == min){
					ui.setLocation(w - WIDTH, ly + y2 - y);
				}else if(buttom == min){
					//System.out.println(loc+"/"+lx+"/"+x2+"/"+x);
					ui.setLocation(lx+x2-x, h - HEIGHT);
				}else if(left == min){
					ui.setLocation(Math.min(lx, 0), ly + y2 - y);
				}
				x = x2;
				y = y2;
			}
		}

		public void mouseMoved(MouseEvent e) {

		}
	}


}
