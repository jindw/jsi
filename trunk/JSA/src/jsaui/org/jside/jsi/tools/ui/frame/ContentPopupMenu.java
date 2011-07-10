package org.jside.jsi.tools.ui.frame;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.jside.jsi.tools.ui.Messages;

class ContentPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	private Editor text;
	private ActionMap actionMap;
	private MouseListener popAction = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				show(e.getComponent(), e.getX(), e
						.getY());
			}
		}
	};

	public ContentPopupMenu(Editor text) {
		this.text = text;
		this.actionMap = ((JComponent) text).getActionMap();
		this.initialize();
	}

	private boolean addMenu(String id, String title, String key) {
		Action action = actionMap.get(id);
		if (action != null) {
			JMenuItem menu = new JMenuItem(title); //$NON-NLS-1$
			menu.addActionListener(action);
			menu.setAccelerator(KeyStroke.getKeyStroke(key));
			this.add(menu);
			return true;
		}
		return false;
	}

	private void initialize() {
		JComponent text = (JComponent) this.text;

		{
			boolean canCompresse = addMenu(EditorCommand.COMPRESS_COMMAND, Messages.ui.compress,
			"control shift C");
			boolean canFormate = addMenu(EditorCommand.FORMAT_COMMAND,
					Messages.ui.format, "control shift F");
			if (canCompresse ||canFormate ) {
				this.addSeparator();
			}
		}

		{
			addMenu(EditorCommand.UNDO_COMMAND, Messages.ui.undo, "control Z");
			addMenu(EditorCommand.REDO_COMMAND, Messages.ui.redo, "control Y");
			addMenu(EditorCommand.SAVE_COMMAND, Messages.ui.save, "control S");
		}

		text.addMouseListener(popAction );

	}

	public static void addPopup(Editor contentArea) {
		new ContentPopupMenu(contentArea);
	}

}
