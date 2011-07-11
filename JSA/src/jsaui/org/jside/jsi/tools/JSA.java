package org.jside.jsi.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.jside.jsi.tools.ui.JSAConfig;
import org.jside.jsi.tools.ui.frame.AnalyserDialog;
import org.jside.ui.ContextMenu;

public class JSA extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7901259629153741454L;
	public static final String HOST = "jsa.jside.org";
	public static JavaScriptCompressor compressor = JSAToolkit.getInstance().createJavaScriptCompressor();
	static{
		compressor.setCompressorConfig(JSAConfig.getInstance());
	}

	public static JavaScriptCompressor getCompressor() {
		return compressor;
	}
	final JTextArea resultArea = new JTextArea();
	final UndoManager undo = new UndoManager();
	
	public final Action REDO_ACTION = new AbstractAction(
			"Redo") { //$NON-NLS-1$ //$NON-NLS-2$
		/**
		 * 
		 */
		private static final long serialVersionUID = -2278919739123543711L;

		public void actionPerformed(ActionEvent evt) {
			try {
				//if (undo.canRedo()) {
					undo.redo();
				//}
			} catch (CannotRedoException e) {
			}
		}
	};
	public final Action UNDO_ACTION = new AbstractAction("Undo") { //$NON-NLS-1$ //$NON-NLS-2$
		/**
		 * 
		 */
		private static final long serialVersionUID = 5502257739210542117L;

		public void actionPerformed(ActionEvent evt) {
			try {
				//if (undo.canUndo()) {
					undo.undo();
				//}
			} catch (CannotUndoException e) {
			}
		}
	};


	public void addAction(Action action, String key) {
		Object id = action.getValue(Action.NAME);
		resultArea.getActionMap().put(id, action);
		resultArea.getInputMap().put(KeyStroke.getKeyStroke(key), id);
	}
	public JSA(){
		super("JSI脚本分析窗口");
		undo.setLimit(100);
		Document doc = resultArea.getDocument();
		doc.addUndoableEditListener(undo);
		addAction(UNDO_ACTION, "control Z");
		addAction(REDO_ACTION, "control Y");
		
		JPopupMenu popup = new JPopupMenu();
		resultArea.setComponentPopupMenu(popup );
		JMenuItem undo = new JMenuItem();
		undo.setAction(UNDO_ACTION);
		undo.setText("撤销(Undo)");
		popup.add(undo);
		JMenuItem redo = new JMenuItem();
		redo.setAction(REDO_ACTION);
		redo.setText("重做(Redo)");
		popup.add(redo);
		this.setPreferredSize(new Dimension(400,400));
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(resultArea),BorderLayout.CENTER);
		JPanel jp = new JPanel(new java.awt.FlowLayout());
		final JButton abt = new JButton("分析");
		final JButton cbt = new JButton("压缩");
		jp.add(abt);
		jp.add(cbt);
		this.add(jp,BorderLayout.SOUTH);
		abt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AnalyserDialog.doAnalyse(compressor, resultArea.getText(), "source.js", resultArea);
				//bt.setEnabled(false);
			}
		});
		cbt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = compressor.compress(resultArea.getText(), null);
				resultArea.setText(text);
			}
		});
	}
	private static ActionListener openUIAction = new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			JSA jsa = new JSA();
			jsa.pack();
			jsa.setVisible(true);
			
		}
	};
	/**
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		ContextMenu cm = ContextMenu.getInstance();
		cm.addMenuSeparator();
		cm.addMenuItem("分析脚本",null,openUIAction);
	}

}
