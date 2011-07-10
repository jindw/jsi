package org.jside.jsi.tools.ui.frame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;

@SuppressWarnings("serial")
public class FileTab extends JPanel implements DocumentListener {
	private final ContentTabbedPane pane;
	private final JLabel label;
	private boolean isDirty = false;
	private Editor editor;

	public FileTab(Editor editor, final ContentTabbedPane pane) {
		// unset default FlowLayout' gaps
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		this.pane = pane;
		this.editor = editor;
		setOpaque(false);
		// tab title
		label = new JLabel(getTitle());
		add(label);
		// tab close
		JButton close = new CloseButton();
		add(close);
		// add more space to the top of the component
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	}

	public void updateTitle() {
		String title = getTitle();
		if (isDirty != editor.isDirty()) {
			isDirty = !isDirty;
			if (isDirty) {
				title = "*" + title;
			}
			this.label.setText(title);
		}
	}

	public String getTitle() {
		return editor.getFile().getName();
	}

	private class CloseButton extends JButton implements ActionListener {
		public CloseButton() {
			int size = 17;
			setPreferredSize(new Dimension(size, size));
			setToolTipText("关闭文件");
			// Make the button looks the same for all Laf's
			setUI(new BasicButtonUI());
			// Make it transparent
			setContentAreaFilled(false);
			// No need to be focusable
			setFocusable(false);
			setBorder(BorderFactory.createEtchedBorder());
			setBorderPainted(false);
			// Making nice rollover effect
			// we use the same listener for all buttons
			addMouseListener(closerMouseListener);
			setRolloverEnabled(true);
			// Close the proper tab by clicking the button
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			int i = pane.indexOfTabComponent(FileTab.this);
			EditorCommand.CLOSE_ACTION.closeTab(pane,i);
		}

		// we don't want to update UI for this button
		public void updateUI() {
		}

		// paint the cross
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			// shift the image for pressed buttons
			if (getModel().isPressed()) {
				g2.translate(1, 1);
			}
			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.BLACK);
			if (getModel().isRollover()) {
				g2.setColor(Color.BLUE);
				// g2.setColor(Color.MAGENTA);
			}
			int delta = 6;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight()
					- delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight()
					- delta - 1);
			g2.dispose();
		}
	}

	private final static MouseListener closerMouseListener = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof CloseButton) {
				CloseButton button = (CloseButton) component;
				button.setBorderPainted(true);
			}
		}

		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof CloseButton) {
				CloseButton button = (CloseButton) component;
				button.setBorderPainted(false);
			}
		}
	};

	public void changedUpdate(DocumentEvent e) {
		updateTitle();
	}

	public void insertUpdate(DocumentEvent e) {
		updateTitle();
	}

	public void removeUpdate(DocumentEvent e) {
		updateTitle();
	}

}