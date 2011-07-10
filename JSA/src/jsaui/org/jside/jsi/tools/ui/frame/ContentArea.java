package org.jside.jsi.tools.ui.frame;

import java.awt.dnd.DropTarget;
import java.io.File;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import org.jside.ui.FileDropTargetAction;

class ContentArea extends JTextArea implements Editor {

	private static final long serialVersionUID = 1L;
	private File file;
	private String content;
	protected UndoManager undo = new UndoManager();

	public UndoManager getUndoManager() {
		return undo;
	}

	private UndoableEditListener undoableEditListener = new UndoableEditListener() {
		public void undoableEditHappened(UndoableEditEvent evt) {
			undo.addEdit(evt.getEdit());
		}
	};

	private ActionMap actionMap;
	private InputMap inputMap;



	private String encoding;
	public ContentArea(String text, String encoding) {
		this.setText(text);
		this.content = text;
		this.encoding = encoding;
		initialize();
	}

	private void initialize() {
		this.setDropTarget(new DropTarget(this, fileDropAction));
		Document doc = this.getDocument();
		doc.addUndoableEditListener(undoableEditListener);
		this.actionMap = this.getActionMap();
		this.inputMap = this.getInputMap();
		addAction(EditorCommand.UNDO_ACTION, "control Z");
		addAction(EditorCommand.REDO_ACTION, "control Y");
		addAction(EditorCommand.SAVE_ACTION, "control S");
	}

	public void addAction(Action action, String key) {
		Object id = action.getValue(Action.NAME);
		actionMap.put(id, action);
		inputMap.put(KeyStroke.getKeyStroke(key), id);
	}

	public Action getAction(String name) {
		return actionMap.get(name);
	}

	public void reset(File file, String content) {
		if (!file.equals(this.file)) {
			this.file = file;
		}
		if (content != null) {
			this.content = content;
		}
		FileTab tab = (FileTab)ContentTabbedPane.getInstance().findTab(this);
		if(tab!=null){
			tab.updateTitle();
		}
	}

	public File getFile() {
		return file;
	}

	public boolean isDirty() {
		return !content.equals(getText());
	}

	private FileDropTargetAction fileDropAction = new FileDropTargetAction() {
		@Override
		protected boolean accept(File file) {
			return file.getName().toLowerCase().endsWith(".js");
		}

		@Override
		protected void openFile(File file) {
			JSAFrame parent = JSAFrame.getInstance();
			parent.openFile(file);

		}

	};

	public void addDocumentListener(DocumentListener listener) {
		this.getDocument().addDocumentListener(listener);
	}

	public String getEncoding() {
		return encoding;
	}
}
