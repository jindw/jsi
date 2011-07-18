package org.jside.jsi.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;

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
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

//import org.jside.jsi.tools.ui.Messages;
import org.jside.ui.ContextMenu;

public class JSA extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7901259629153741454L;
	public static final String HOST = "jsa.jside.org";
	public static JavaScriptCompressor compressor = JSAToolkit.getInstance().createJavaScriptCompressor();
//	static{
//		compressor.setCompressorConfig(JSAConfig.getInstance());
//	}

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
				doAnalyse(compressor, resultArea.getText(), "source.js", resultArea);
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
	public static JavaScriptAnalysisResult doAnalyse(JavaScriptCompressor compressor, String source,
			String filePath, JTextArea resultArea) {
		JavaScriptAnalysisResult analyser = compressor.analyse(source);
		String text;
		try {
			// analyser.analyse(analyserUI.getScriptText(), filePath);
			String name = filePath;
			if (name == null) {
				name = "unknow.js"; //$NON-NLS-1$
			} else {
				name = name.substring(Math.max(name.lastIndexOf('/'), name
						.lastIndexOf('\\')) + 1);
			}



			StringWriter buf = new StringWriter();
			PrintWriter out = new PrintWriter(buf);
			if (analyser.getLocalVars().isEmpty()) {
				out.println("//未申明任何变量："); //$NON-NLS-1$
				out.println("//JSI 中脚本描述参考："); //$NON-NLS-1$
				out.print(" this.addScript('"); //$NON-NLS-1$
				out.print(name);
				out.println("')"); //$NON-NLS-1$
			} else {
				printAddScript(out, name, analyser.getLocalVars());
			}
			if (!analyser.getExternalVars().isEmpty()) {
				out.print("\n\n//外部变量有（包含内置）："); //$NON-NLS-1$
				out.println(analyser.getExternalVars());
			}
			if (!analyser.getUnknowVars().isEmpty()) {
				out.print("\n\n//未知变量集（非内置且未申明,可能需要申明依赖）："); //$NON-NLS-1$
				out.println(analyser.getUnknowVars());
				printAddDependence(out);
			}
			resultArea.setForeground(Color.BLUE);
			out.flush();
			out.close();
			text = buf.toString();
		} catch (RuntimeException e) {
			e.printStackTrace();
			resultArea.setForeground(Color.RED);
			text = analyser.getErrors().toString();
		}
		try {
			resultArea.setText(text);
//			resultArea.selectAll();
//			resultArea.replaceSelection(text);
		} catch (NoSuchMethodError e) {
			// System.out.println("奇怪的问题");
			((JTextComponent) resultArea).setText(text);
			// e.printStackTrace();
		}
		return analyser;
	}


	@SuppressWarnings("unchecked")
	private static void printAddScript(PrintWriter out, String name,
			Collection set) {
		out.print("//申明变量有："); //$NON-NLS-1$
		out.println(set);
		out.println("//JSI 中脚本描述参考："); //$NON-NLS-1$
		out.print(" this.addScript('"); //$NON-NLS-1$
		out.print(name);
		out.print("',"); //$NON-NLS-1$
		out.print("["); //$NON-NLS-1$
		Iterator it = set.iterator();
		if (it.hasNext()) {
			while (true) {
				out.print("'"); //$NON-NLS-1$
				out.print(it.next());
				out.print("'"); //$NON-NLS-1$
				if (it.hasNext()) {
					out.print(","); //$NON-NLS-1$
				} else {
					break;
				}
			}
		}
		out.println("]);"); //$NON-NLS-1$
		out.println("//更多详细资料请参考：http://www.xidea.org/project/jsi/script.html");//$NON-NLS-1$
	}
/*

//JSI 中脚本依赖描述参考：

/*===========================================*\\
方式1：填加脚本时直接定义（JSI2.1+）
this.addScript('xx.js','xx',
                            beforeLoadDependences,
                            beforeLoadDependences)
方式2：在包文件后定义
 this.addDependence(object,
                                        dependenceObject,
                                        isBeforeLoadDependence);


更多详细资料请参考：http://www.xidea.org/project/jsi/dependence.html
\*===========================================*/

	private static void printAddDependence(PrintWriter out) {
		out.println();
		out.println();
		out.println("//JSI 中脚本依赖描述参考："); //$NON-NLS-1$
		out.println();
		out.println("/*===========================================*\\"); //$NON-NLS-1$
		out.println("方式1：填加脚本时直接定义（JSI2.1+）");//$NON-NLS-1$
		out.println("this.addScript('xx.js','xx',");//$NON-NLS-1$
		out.println("                            beforeLoadDependences,");//$NON-NLS-1$
		out.println("                            beforeLoadDependences)");//$NON-NLS-1$
		out.println("方式2：在包文件后定义");//$NON-NLS-1$
		out.println(" this.addDependence(object,");//$NON-NLS-1$
		out.println("                                        dependenceObject,"); //$NON-NLS-1$
		out.println("                                        isBeforeLoadDependence);"); //$NON-NLS-1$
		out.println(); //$NON-NLS-1$
		out.println("更多详细资料请参考：http://www.xidea.org/project/jsi/dependence.html"); //$NON-NLS-1$
		out.println("\\*===========================================*/"); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		ContextMenu cm = ContextMenu.getInstance();
		cm.addMenuSeparator();
		cm.addMenuItem("分析脚本",null,openUIAction);
	}

}
