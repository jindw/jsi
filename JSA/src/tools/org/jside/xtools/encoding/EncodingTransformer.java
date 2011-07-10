package org.jside.xtools.encoding;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jside.ui.ContextMenu;
import org.jside.xtools.Transformer;
import org.xidea.commons.i18n.DefaultCharsetSelector;
import org.xidea.commons.i18n.TextResource;
import org.xidea.jsi.JSIRuntime;
import org.xidea.jsi.impl.RuntimeSupport;

public class EncodingTransformer extends Transformer {
	private static final long serialVersionUID = 199254361390410195L;

	private JSIRuntime jp = RuntimeSupport.create();
	private JComboBox sourceEncoding = new JComboBox(new String[] { "UTF-8",
			"GBK" });
	private JComboBox destEncoding = new JComboBox(new String[] { "UTF-8",
			"GBK" });
	private JCheckBox trimBOM = new JCheckBox("清理BOM");
	private final JTextArea replacer = new JTextArea(
			"source = source.replace("
					+ "\n\t/(<meta\\s+http-equiv=['\"]?Content-Type['\"]?\\s+content=[\"']?\\w+\\/\\w+;)([\\w\\-]+)/ig,"
					+ "\n\tfunction(match,prefixPart,encodingPart){"
					+ "\n\t\tif(encodingPart.toLowerCase() == encoding.toLowerCase()){"
					+ "\n\t\t\treturn match;"
					+ "\n\t\t}"
					+ "\n\t\tvar text = prefixPart+encoding;"
					+ "\n\t\tlog('INFO:找到文本并替换：\\n'+match+'\\n=>\\n'+text);" 
					+ "\n\t\treturn text;"
					+ "\n\t}" + "\n)" + "\n\n\n\n");

	private Object replacerScript;

	public EncodingTransformer() {
		this.setTitle("批量编码工具");
		initializeUI();
		initializeEvent();
	}

	protected JPanel buildSetting() {
		JPanel encodingPanel = super.buildSetting();

		encodingPanel.add(new JLabel("原始编码"));
		encodingPanel.add(sourceEncoding);
		encodingPanel.add(new JLabel("->目标编码"));
		encodingPanel.add(destEncoding);

		JButton script = new JButton("替换脚本");
		script.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String old = replacer.getText();
				int ret = JOptionPane.showConfirmDialog(
						EncodingTransformer.this, new JScrollPane(replacer),
						"设置替换脚本", JOptionPane.OK_CANCEL_OPTION);
				String ns = transformCode();
				try {
					if (ret == JOptionPane.YES_OPTION) {
						jp.eval(ns);
					}
					return;
				} catch (Exception e2) {
					// TODO: handle exception
					replacer.setText(old);
					JOptionPane.showMessageDialog(EncodingTransformer.this, e2,
							"设置失败(语法错误)", JOptionPane.ERROR_MESSAGE);
					System.out.println(ns);
				}
			}
		});
		encodingPanel.add(trimBOM);
		encodingPanel.add(script);
		// this.add(new JLabel("正则替换(每行是一个替换规则,需要且只能有一个group,为目标编码所在)"), c);
		// this.add(new JScrollPane(replacer), c);
		return encodingPanel;
	}

	protected void transform() {
		this.replacerScript = jp.eval(transformCode());
		super.transform();
	}

	private String transformCode() {
		return "({encoding:'" + destEncoding.getSelectedItem() + "',"
				+ "replace:function(source,encoding,impl){"
				+"this.source = source;var log = function(msg){impl.log(msg)};"
				+ replacer.getText() + "\n;return source;}" + "})";
	}

	protected void transform(final File source, File dest) throws IOException {
		String destEncoding = String.valueOf(this.destEncoding
				.getSelectedItem());
		DefaultCharsetSelector charserSelector = new DefaultCharsetSelector(
				String.valueOf(this.sourceEncoding.getSelectedItem())) {

			public String selectCharset(String[] options) {
				log("WARNING:文件<" + source + ">存在多种编码可能。");
				if (defaultCharset != null) {
					for (int i = 0; i < options.length; i++) {
						if (defaultCharset.equalsIgnoreCase(options[i])) {
							log("采用默认编码处理：" + defaultCharset);
							return defaultCharset;
						}
					}
				}
				log("没有找到匹配的默认编码，选取第一个可能的字符集：" + options[0]);
				return options[0];
			}
		};
		InputStream in = new FileInputStream(source);
		String result = TextResource.getText(in, Locale.CHINESE,
				charserSelector);
		if(trimBOM.isSelected() && result.startsWith("\uFEFF")){
			result = result.substring(1);
		}
		in.close();
		result = replace(result, destEncoding);

		if (!dest.exists()) {
			dest.createNewFile();
		}
		Writer out = new OutputStreamWriter(new FileOutputStream(dest),
				destEncoding);
		out.write(result);
		log("INFO:文件“" + dest + "”  转换成功");
		out.flush();
		out.close();

	}

	protected String replace(String result, String destEncoding) {
		return (String) jp.invoke(replacerScript, "replace", result,
				destEncoding,this);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ContextMenu.getInstance().addMenuSeparator();
		ContextMenu.getInstance().addMenuItem("批量转码", null,
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showUI();
					}

				});
	}

	private static EncodingTransformer instance;

	public static void showUI() {
		getInstance().setVisible(true);
	}

	public static EncodingTransformer getInstance() {
		if (instance == null) {
			instance = new EncodingTransformer();
			instance.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					instance.setVisible(true);
				}
			});
		}
		return instance;
	}
}
