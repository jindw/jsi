package org.jside.jsi.tools.ui.frame;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.swing.ActionMap;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.xidea.commons.i18n.TextResource;

public class ContentTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = 1L;
	private static ContentTabbedPane instance;
	private MouseListener tabTrigger = new MouseAdapter() {
		TabPopupMenu tabPopupMenu = new TabPopupMenu();

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				ContentTabbedPane contentPane = getInstance();
				for (int i = 0; i < contentPane.getTabCount(); i++) {
					Rectangle rect = contentPane.getBoundsAt(i);
					if (rect.contains(e.getX(), e.getY())) {
						tabPopupMenu.show(e.getComponent(), e.getX(), e.getY());
						return;
					}
				}

			}
		}
	};

	public static ContentTabbedPane getInstance() {
		if (instance == null) {
			instance = new ContentTabbedPane();
		}
		return instance;
	}

	private ContentTabbedPane() {
		initialize();
	}

	@Override
	public void paint(Graphics g) {
		if (this.getTabCount() > 0) {
			super.paint(g);
		} else {

			int left = 20;
			int top = 20;
			// g.drawImage(JSide.icon.getImage(),left,top,this);
			Font f = g.getFont();
			g.setFont(f.deriveFont((float) 20.0));
			g.drawString("欢迎使用JSA 客户端", left, top += 30);
			g.setFont(f.deriveFont((float) 12.0));
			g.drawString("您还没有打开文件......", left, top += 20);
			g.drawString("1.拖放脚本文件直接打开编辑", left, top += 40);
			g.drawString("2.项目文件树双击打开&编辑文本文件", left, top += 20);
			g.drawString("3.拖放目录切换网站根目录", left, top += 20);
			// g.drawString("4.单击鼠标，打开文件选择对话框",left,top+=20);
			g.setFont(f);
		}
	}

	private void initialize() {
		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		this.addMouseListener(tabTrigger);
		// this.setDropTarget(new FileOpenDropTarget());
		ActionMap actionMap = this.getActionMap();
		actionMap.put(EditorCommand.CLOSE_COMMAND, EditorCommand.CLOSE_ACTION);
		actionMap.put(EditorCommand.CLOSE_OTHER_COMMAND,
				EditorCommand.CLOSE_OTHER_ACTION);
		actionMap.put(EditorCommand.CLOSE_ALL_COMMAND,
				EditorCommand.CLOSE_ALL_ACTION);
	}

	public FileTab findTab(Editor editor) {
		for (int i = getTabCount() - 1; i >= 0; i--) {
			Editor editor2 = (Editor) getEditorAt(i);
			if (editor2 == editor) {
				return (FileTab) this.getTabComponentAt(i);
			}
		}
		return null;
	}

	public void openFile(File file) {
		String value;
		String encoding = "UTF-8";
		try {
			InputStream in = new FileInputStream(file);
			TextResource res = TextResource.create(in, Locale.getDefault(),
					JSAFrame.getInstance().charsetSelector);
			value = res.getText();
			encoding = res.getEncoding();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			value = "";
		}
		for (int i = getTabCount() - 1; i >= 0; i--) {
			Editor editor = (Editor) getEditorAt(i);
			if (editor != null && file.equals(editor.getFile())) {
				this.setSelectedIndex(i);
				editor.reset(file, value);
				return;
			}
		}
		Editor editor = EditorFactory.createEditor(file, value, encoding);
		if (editor != null) {
			this.add(new JScrollPane((Component) editor));
			int index = this.getTabCount() - 1;
			FileTab tab = new FileTab(editor, this);
			editor.addDocumentListener(tab);
			this.setTabComponentAt(index, tab);
			this.setSelectedIndex(index);
			// /this.setToolTipTextAt(index, "0000");
		}
	}

	public Editor getEditorAt(int i) {
		JScrollPane scrollPane = (JScrollPane) this.getComponentAt(i);
		return (Editor) scrollPane.getViewport().getView();
	}

	public Editor getCurrentEditor() {
		return getEditorAt(this.getSelectedIndex());
	}

}
