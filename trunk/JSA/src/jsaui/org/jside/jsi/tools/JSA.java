package org.jside.jsi.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.jside.jsi.tools.ui.JSAConfig;
import org.jside.jsi.tools.ui.frame.AnalyserDialog;
import org.jside.ui.ContextMenu;

public class JSA {
	public static final String HOST = "jsa.jside.org";
	public static JavaScriptCompressor compressor = JSAToolkit.getInstance().createJavaScriptCompressor();
	static{
		compressor.setCompressorConfig(JSAConfig.getInstance());
	}

	public static JavaScriptCompressor getCompressor() {
		return compressor;
	}
	private static ActionListener openUIAction = new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			final JTextArea resultArea = new JTextArea();
			JFrame frame = new JFrame("JSI脚本分析窗口");
			frame.setPreferredSize(new Dimension(400,400));
			frame.setLayout(new BorderLayout());
			frame.add(resultArea,BorderLayout.CENTER);
			JPanel jp = new JPanel(new java.awt.FlowLayout());
			final JButton abt = new JButton("分析");
			final JButton cbt = new JButton("压缩");
			jp.add(abt);
			jp.add(cbt);
			frame.add(jp,BorderLayout.SOUTH);
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
			frame.pack();
			frame.setVisible(true);
			
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
