package org.jside.jsi.tools.ui.frame;

import java.io.File;

import javax.swing.Action;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;

public interface Editor {
	public void reset(File file,String content);
	public void setText(String text);
	public void addDocumentListener(DocumentListener listener);
	public boolean isDirty();
	public String getText();
	public UndoManager getUndoManager();
	public File getFile();
	public Action getAction(String name);
	public String getEncoding();
}
