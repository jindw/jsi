package org.jside.jsi.tools.ui.frame;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jside.jsi.tools.ui.Messages;

public class TabPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	public TabPopupMenu() {
		this.initialize();
	}
	private void initialize() {

		JMenuItem closeMenu = new JMenuItem(Messages.ui.closeFile); //$NON-NLS-1$
		closeMenu.setActionCommand(EditorCommand.CLOSE_COMMAND); //$NON-NLS-1$
		closeMenu.addActionListener(EditorCommand.CLOSE_ACTION);
		this.add(closeMenu);
		
		JMenuItem closeOtherMenu = new JMenuItem(Messages.ui.closeOther); //$NON-NLS-1$
		closeOtherMenu.setActionCommand(EditorCommand.CLOSE_OTHER_COMMAND); //$NON-NLS-1$
		closeOtherMenu.addActionListener(EditorCommand.CLOSE_OTHER_ACTION);
		this.add(closeOtherMenu);
		
		JMenuItem closeAll = new JMenuItem(Messages.ui.closeAll); //$NON-NLS-1$
		closeAll.setActionCommand(EditorCommand.CLOSE_ALL_COMMAND); //$NON-NLS-1$
		closeAll.addActionListener(EditorCommand.CLOSE_ALL_ACTION);
		this.add(closeAll);

	}

}