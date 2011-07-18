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

import org.jside.jsi.tools.JSA;
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
		JSA.doAnalyse(analyser, source, filePath, this.resultArea);
		this.setVisible(true);
		this.pack();
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