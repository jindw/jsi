package org.jside.jsi.tools.ui.frame;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.jside.jsi.tools.JSA;
import org.jside.jsi.tools.JavaScriptCompressionAdvisor;
import org.jside.ui.DesktopUtil;

@SuppressWarnings("serial")
public interface EditorCommand {
	public static final String UNDO_COMMAND = "Undo";
	public static final String REDO_COMMAND = "Redo";

	public static final String SAVE_COMMAND = "Save";
	public final static String COMPRESS_COMMAND = "Compress";
	public static final String FORMAT_COMMAND = "Format";

	public static final String CLOSE_COMMAND = "Close";
	public static final String CLOSE_ALL_COMMAND = "CloseAll";
	public static final String CLOSE_OTHER_COMMAND = "CloseOther";

	public static final Action SAVE_ACTION = new AbstractAction(
			EditorCommand.SAVE_COMMAND) { //$NON-NLS-1$ //$NON-NLS-2$
		public void actionPerformed(ActionEvent evt) {
			try {
				Editor editor = JSAFrame.getInstance().getCurrentEditor();
				CloseAction.save(editor);
			} catch (IOException e) {
				DesktopUtil.alert(e);
			}
		}
	};
	public static final Action REDO_ACTION = new AbstractAction(
			EditorCommand.REDO_COMMAND) { //$NON-NLS-1$ //$NON-NLS-2$
		public void actionPerformed(ActionEvent evt) {
			try {
				Editor editor = JSAFrame.getInstance().getCurrentEditor();
				if (editor.getUndoManager().canRedo()) {
					editor.getUndoManager().redo();
				}
			} catch (CannotRedoException e) {
			}
		}
	};
	public static final Action UNDO_ACTION = new AbstractAction(
			EditorCommand.UNDO_COMMAND) { //$NON-NLS-1$ //$NON-NLS-2$
		public void actionPerformed(ActionEvent evt) {
			try {
				Editor editor = JSAFrame.getInstance().getCurrentEditor();
				if (editor.getUndoManager().canUndo()) {
					editor.getUndoManager().undo();
				}
			} catch (CannotUndoException e) {
			}
		}
	};
	public static final Action COMPRESS_JS_ACTION = new AbstractAction(
			COMPRESS_COMMAND) { //$NON-NLS-1$ //$NON-NLS-2$
		public void actionPerformed(ActionEvent evt) {
			Editor editor = JSAFrame.getInstance().getCurrentEditor();
			JSAFrame.getInstance().showText("压缩结果", compress(editor));
		}

		public String compress(Editor editor) {
			try {
				String path = editor.getFile().getAbsolutePath();
				String source = editor.getText();
				JavaScriptCompressionAdvisor advice = ReplacerHolderDialog
						.getInstance().getGlobalReplacer(path, source);
				return JSA.getCompressor().compress(source, advice);
			} catch (Exception ex) {
				ex.printStackTrace();
				return "Compress Error:\r\n\r\n" + ex.toString();
			}
		}

	};
	public static final Action FORMAT_JS_ACTION = new AbstractAction(
			FORMAT_COMMAND) { //$NON-NLS-1$ //$NON-NLS-2$
		public void actionPerformed(ActionEvent evt) {
			Editor editor = JSAFrame.getInstance().getCurrentEditor();
			JSAFrame.getInstance().showText("格式化结果", format(editor));
		}

		public String format(Editor editor) {
			try {
				// p.压码条件 = 0.9;
				return JSA.getCompressor().format(editor.getText());
			} catch (Exception ex) {
				ex.printStackTrace();
				return "Format Error:\r\n\r\n" + ex.toString();
			}
		}
	};

	static class CloseAction extends AbstractAction {

		public CloseAction(String name) {
			super(name);
		}

		public void doClose() {
			ContentTabbedPane pane = ContentTabbedPane.getInstance();
			int count = pane.getTabCount();
			for (int index = count - 1; index >= 0; index--) {
				if (shouldClose(pane, index)) {
					closeTab(pane, index);
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			doClose();
		}

		static void save(Editor editor) throws IOException {
			File file = editor.getFile();
			if (file == null) {
				JSAFrame.getInstance().saveAsFile();
			} else {
				String text = editor.getText();
				String encoding = editor.getEncoding();
				if (!file.exists()) {
					file.createNewFile();
				}
				FileOutputStream out = new FileOutputStream(file);
				out.write(text.getBytes(encoding));
				out.close();
				editor.reset(file, text);
			}
		}

		public boolean closeTab(ContentTabbedPane pane, int index) {
			Editor editor = pane.getEditorAt(index);
			if (editor.isDirty()) {
				pane.setSelectedIndex(index);
				JSAFrame.showUI();
				int result = JOptionPane.showConfirmDialog(JSAFrame
						.getInstance(), "文件" + editor.getFile() + " 尚未保存\n"
						+ "是否保存文件？", "保存提醒", JOptionPane.YES_NO_CANCEL_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					try {
						save(editor);
					} catch (IOException e) {
						DesktopUtil.alert("文件保存失败，请手动处理？");
						JSAFrame.getInstance().showText(
								"文件:" + editor.getFile(), editor.getText());
					}
				} else if (result == JOptionPane.NO_OPTION) {
				} else if (result == JOptionPane.CANCEL_OPTION) {
					return false;
				}
			}
			pane.remove(index);
			return true;
		}

		protected boolean shouldClose(ContentTabbedPane pane, int index) {
			int selectedIndex = pane.getSelectedIndex();
			return selectedIndex == index;
		}

	}

	public static final CloseAction CLOSE_ACTION = new CloseAction(
			CLOSE_COMMAND);
	public static final CloseAction CLOSE_OTHER_ACTION = new CloseAction(
			CLOSE_OTHER_COMMAND) {
		protected boolean shouldClose(ContentTabbedPane pane, int index) {
			int selectedIndex = pane.getSelectedIndex();
			return selectedIndex != index;
		}
	};
	public static final CloseAction CLOSE_ALL_ACTION = new CloseAction(
			CLOSE_ALL_COMMAND) {
		protected boolean shouldClose(ContentTabbedPane pane, int index) {
			return true;
		}
	};
}
