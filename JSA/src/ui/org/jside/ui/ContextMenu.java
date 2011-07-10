package org.jside.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jside.JSide;
import org.jside.JSideListener;

public class ContextMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;

	public ContextMenu() {
//		super.add(buildTrayActionMenu("首页", "icon/home.gif", homeAction));
//		super.add(buildTrayActionMenu("管理", "icon/tools.gif", toolsAction));
		super.addSeparator();
		super.add(buildTrayActionMenu("退出", "icon/exit.gif", exitAction));
	}

	protected void addMenuItem(JComponent item){
		this.insert(item, this.getComponentCount()-2);
	}
	public void addMenuSeparator() {
		addMenuItem(new JPopupMenu.Separator() );
	}
	public void addMenuItem(String title, Icon icon,
			ActionListener action) {
		JMenuItem item = new JMenuItem(title,icon);
		item.addActionListener(action);
		addMenuItem(item);
		
	}
	private JMenuItem buildTrayActionMenu(String name, String icon,
			ActionListener action) {
		JMenuItem mi = new JMenuItem(name);
		mi.addActionListener(action);
		mi.setIcon(new ImageIcon(this.getClass().getResource(icon)));
		return mi;
	}

	final public static ActionListener homeAction = WebLinkAction.createLocalLink("/");

	final public static WebLinkAction toolsAction = WebLinkAction.createScriptLink("tools.xhtml");

	static ActionListener exitAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JSide.fireEvent(JSideListener.Exit.class, this);
			System.exit(1);
		}
	};

	private static ContextMenu instance = new ContextMenu();

	public static ContextMenu getInstance() {
		return instance ;
	}

}

