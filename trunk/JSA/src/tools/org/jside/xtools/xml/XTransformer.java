//package org.jside.xtools.xml;
//
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.StringWriter;
//import java.io.Writer;
//
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//
//import org.jside.ui.ContextMenu;
//import org.jside.xtools.Transformer;
//
//public class XTransformer extends Transformer {
//	private static final long serialVersionUID = 1L;
//	private HTML2XHTML h2x = new HTML2XHTML();
//
//	private JComboBox destEncoding = new JComboBox(new String[] { "GBK",
//			"UTF-8" });
//
//
//	public XTransformer() {
//		this.setTitle("批量html2XHTML工具");
//		initializeUI();
//		initializeEvent();
//	}
//
//
//	@Override
//	protected JPanel buildSetting() {
//		JPanel encodingPanel = new JPanel();
//		JLabel extinfo = new JLabel("接受文件");
//		extinfo.setToolTipText("扩展名','隔开");
//		encodingPanel.add(extinfo);
//		extInput.setToolTipText("扩展名','隔开");
//		encodingPanel.add(extInput);
//		encodingPanel.add(destEncoding);
//		return encodingPanel;
//	}
//
//
//	protected void transform(final File source, File dest) throws IOException {
//		String destEncoding = String.valueOf(this.destEncoding
//				.getSelectedItem());
//
//		// TODO:ask
//		if (!dest.exists()) {
//			dest.createNewFile();
//		}
//		StringWriter out1 = new StringWriter();
//		boolean success = h2x.doTransform(source.toURI().toURL(), out1);
//		if (success) {
//			log("INFO:文件“" + dest + "”  转换成功");
//			Writer out = new OutputStreamWriter(new FileOutputStream(dest),
//					destEncoding);
//			out.write(out1.toString());
//			out.flush();
//			out.close();
//		} else {
//			log("ERROR:文件“" + dest + "”  转换失败:"+out1);
//		}
//
//	}
//
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		ContextMenu.getInstance().addMenuItem("HTML2XHTML", null,
//				new ActionListener() {
//					public void actionPerformed(ActionEvent e) {
//						showUI();
//					}
//
//				});
//	}
//
//	private static XTransformer instance;
//
//	public static void showUI() {
//		getInstance().setVisible(true);
//	}
//
//	public static XTransformer getInstance() {
//		if (instance == null) {
//			instance = new XTransformer();
//			instance.addWindowListener(new WindowAdapter() {
//				public void windowClosing(WindowEvent e) {
//					instance.setVisible(true);
//				}
//				@Override
//				public void windowClosed(WindowEvent e) {
//					//System.exit(1);
//				}
//			});
//		}
//		return instance;
//	}
//}
