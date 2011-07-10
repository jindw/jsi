package org.jside.jsi.tools.ui.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.jside.jsi.tools.JavaScriptAnalysisResult;
import org.jside.jsi.tools.JavaScriptCompressor;
import org.jside.jsi.tools.ui.Messages;

public class AnalyserDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static AnalyserDialog instance;

	public static AnalyserDialog getInstance() {
		if(instance == null){
			instance = new AnalyserDialog();
		}
		return instance;
	}
	private final JTextArea resultArea = new JTextArea();

	private AnalyserDialog()
			throws HeadlessException {
		super(JSAFrame.getInstance(), Messages.ui.analyserReport, true); //$NON-NLS-1$
		initialize();
		this.setSize(new Dimension(580, 460));
		// this.setPreferredSize(new Dimension(580, 460));
		this.setLocationRelativeTo(JSAFrame.getInstance());
		this.setVisible(false);
		this.setResizable(true);
	}

	public void showAnalyseResult(JavaScriptCompressor analyser, String source,
			String filePath) {
		// String filePath = selectedFile == null ? null : selectedFile
		// .getAbsolutePath();
		this.setSize(new Dimension(580, 460));
		doAnalyse(analyser, source, filePath, this.resultArea);
		this.setVisible(true);
		this.pack();
	}

	public static JavaScriptAnalysisResult doAnalyse(JavaScriptCompressor compressor, String source,
			String filePath, JTextArea resultArea) {
		JavaScriptAnalysisResult analyser = compressor.analyse(source);
		String text;
		try {
			// analyser.analyse(analyserUI.getScriptText(), filePath);
			String name = filePath;
			if (name == null) {
				name = Messages.ui.unknowFile; //$NON-NLS-1$
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

	public void initialize() {
		JRootPane rootPane = this.getRootPane();
		rootPane.setLayout(new BorderLayout());
		// rootPane.add(new JLabel("分析结果"), BorderLayout.NORTH);
		rootPane.add(new JScrollPane(resultArea), BorderLayout.CENTER);
		JButton ok = new JButton(Messages.ui.ok); //$NON-NLS-1$
		ok.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				AnalyserDialog.this.setVisible(false);
			}

		});
		rootPane.add(ok, BorderLayout.SOUTH);
		this.setRootPane(rootPane);
	}

}