package org.jside.jsi.tools.ui.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jside.JSide;
import org.jside.jsi.tools.ui.JSAConfig;
import org.jside.jsi.tools.ui.Messages;

public class JSASettingDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JSASettingDialog instance;

	public static JSASettingDialog getInstance() {
		if (instance == null) {
			instance = new JSASettingDialog();
		}
		return instance;
	}

	private JPanel settingWindow = new JPanel();
	private JCheckBox textCompression = new JCheckBox(
			Messages.ui.doTextCompression); //$NON-NLS-1$
	private JCheckBox syntaxCompression = new JCheckBox(
			Messages.ui.doSyntaxCompression); //$NON-NLS-1$
	private JCheckBox textCompressionCompatible = new JCheckBox(
			Messages.ui.compatible); //$NON-NLS-1$
	private JPanel textCompressionOptions = new JPanel();
	private JTextField sizeCondition = new JTextField(); //$NON-NLS-1$
	private JTextField ratioCondition = new JTextField();

	private JSASettingDialog() {
		initialize();
	}

	private void initialize() {
		settingWindow.setSize(new Dimension(210, 60));
		settingWindow.setLayout(new BorderLayout());
		JPanel basicOptions = new JPanel();
		basicOptions.add(textCompression);
		basicOptions.add(syntaxCompression);
		basicOptions.add(syntaxCompression);
		basicOptions.add(textCompression);
		basicOptions.add(textCompressionCompatible);
		settingWindow.add(basicOptions, BorderLayout.NORTH);
		ActionListener checkChanged = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean enabled = textCompression.isSelected();
				textCompressionCompatible.setEnabled(enabled);
				for (int i = textCompressionOptions.getComponentCount() - 1; i >= 0; i--) {
					textCompressionOptions.getComponent(i).setEnabled(enabled);
				}
			};
		};
		checkChanged.actionPerformed(null);
		textCompression.addActionListener(checkChanged);
		textCompressionOptions.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory
						.createTitledBorder(Messages.ui.compressionOptions),
				BorderFactory //$NON-NLS-1$
						.createEmptyBorder(5, 5, 5, 5)));
		textCompressionOptions.setLayout(new GridLayout(2, 2));

		ratioCondition.setSize(new Dimension(20, 20));
		ratioCondition.setToolTipText(Messages.ui.ratioCondition); //$NON-NLS-1$
		ratioCondition.setInputVerifier(new InputVerifier() {
			public boolean verify(JComponent input) {
				String value = ((JTextField) input).getText();
				try {
					Float.parseFloat(value.trim());
					return true;
				} catch (Exception e) {
					input.requestFocus();
					JOptionPane.showMessageDialog(settingWindow,
							Messages.ui.ratioBeFloat); //$NON-NLS-1$
					return false;
				}
			}

		});
		sizeCondition.setSize(new Dimension(20, 20));
		sizeCondition.setInputVerifier(new InputVerifier() {
			public boolean verify(JComponent input) {
				String value = ((JTextField) input).getText();
				try {
					Integer.parseInt(value.trim());
					return true;
				} catch (Exception e) {
					input.requestFocus();
					JOptionPane.showMessageDialog(settingWindow,
							Messages.ui.sizeBeInteger); //$NON-NLS-1$
					return false;
				}
			}

		});
		ratioCondition.setToolTipText(Messages.ui.sizeCondition); //$NON-NLS-1$
		textCompressionOptions.add(new JLabel(Messages.ui.ratioConditionLabel)); //$NON-NLS-1$
		textCompressionOptions.add(ratioCondition);
		textCompressionOptions.add(new JLabel(Messages.ui.sizeConditionLabel)); //$NON-NLS-1$
		textCompressionOptions.add(sizeCondition);
		settingWindow.add(textCompressionOptions, BorderLayout.CENTER);
	}

	public void showDialog() {
		resetValues();
		int result = JOptionPane.showConfirmDialog(this, settingWindow,
				Messages.ui.setting, //$NON-NLS-1$
				JOptionPane.INFORMATION_MESSAGE & JOptionPane.OK_OPTION);
		// System.out.println("result:" + result);
		if (result != JOptionPane.CLOSED_OPTION) {
			JSAConfig config = JSAConfig.getInstance();
			config.setTextCompression(textCompression.getModel().isSelected());
			config.setCompatible(textCompressionCompatible.getModel()
					.isSelected());
			config.setSyntaxCompression(syntaxCompression.getModel()
					.isSelected());
			config.setSizeCondition(Integer.parseInt(sizeCondition.getText()
					.trim()));
			config.setRatioCondition(Float.parseFloat(ratioCondition.getText()
					.trim()));
			JSide.saveConfig(JSAConfig.class, config);
		}
	}

	private void resetValues() {
		JSAConfig config = JSAConfig.getInstance();
		syntaxCompression.getModel().setSelected(config.isSyntaxCompression());
		textCompression.getModel().setSelected(config.isTextCompression());
		textCompressionCompatible.getModel().setSelected(config.isCompatible());
		ratioCondition.setText(NumberFormat.getNumberInstance().format(
				config.getRatioCondition()));

		sizeCondition.setText("" + config.getSizeCondition());

	}

}
