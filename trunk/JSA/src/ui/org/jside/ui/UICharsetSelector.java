package org.jside.ui;

import java.awt.Window;

import javax.swing.JOptionPane;

import org.xidea.commons.i18n.CharsetSelector;

public class UICharsetSelector implements CharsetSelector {
	private Window parent;
	public UICharsetSelector(Window parent){
		this.parent = parent;
	}
	public String selectCharset(String[] options) {
		return (String) JOptionPane.showInputDialog(
				parent,
				"可选字符集：",
				"选择字符集", JOptionPane.QUESTION_MESSAGE,
				null, options, options[0]);
	}

}
