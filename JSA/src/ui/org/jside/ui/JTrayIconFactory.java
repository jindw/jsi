package org.jside.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * @see https://swinghelper.dev.java.net/source/browse/swinghelper/src/java/org/jdesktop/swinghelper/tray/JXTrayIcon.java?view=markup
 * @author jindw
 * 
 */
public class JTrayIconFactory{
	private static JDialog dialog;
	static {
		dialog = new JDialog((Frame) null);
		dialog.setAlwaysOnTop(true);
		dialog.setUndecorated(true);
		dialog.setBounds(0, 0, 0, 0);
	}
	private static PopupMenuListener popupListener = new PopupMenuListener() {
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			dialog.setVisible(false);
			// dialog.toBack();
		}

		public void popupMenuCanceled(PopupMenuEvent e) {
		}
	};

	public static TrayIcon createTrayIcon(Image image,final JPopupMenu menu) {
		TrayIcon ti = new TrayIcon(image);
		ti.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger() && menu != null) {
					Dimension size = menu.getPreferredSize();
					showJPopupMenu(menu,e.getX() - size.width, e.getY() - size.height);
				}
			}

			public void mouseReleased(MouseEvent e) {
				mousePressed(e);
			}
		});

		menu.addPopupMenuListener(popupListener);
		return ti;
	}

	private static void showJPopupMenu(JPopupMenu menu,int x, int y) {
		if (!dialog.isVisible()) {
			dialog.setLocation(x, y);
			dialog.setVisible(true);
			menu.show(dialog.getContentPane(), 0, 0);
			// popup works only for focused windows
			dialog.toFront();
		}
	}

}