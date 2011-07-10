package org.jside.jsi.tools.ui.frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.jside.JSideWebServer;
import org.jside.jsi.tools.JSA;
import org.jside.jsi.tools.ui.JSAConfig;
import org.jside.jsi.tools.ui.Messages;
import org.jside.ui.DesktopUtil;
import org.jside.ui.WebLinkAction;
import org.xidea.commons.i18n.swing.MessageBase;

public class JSAMenuBar extends JMenuBar {
	private static final long serialVersionUID = 1L;
	private JSAFrame parent;

	public JSAMenuBar(JSAFrame parent) {
		this.parent = parent;
		this.add(buildFileMenu());
		this.add(buildOperationMenu());

		{
			JMenu switchLanhuage = MessageBase.buildMenu(
					Messages.ui.switchLanguage, Messages.class,
					new Component[] { this,
							JSASettingDialog.getInstance(),
							AnalyserDialog.getInstance(),
							ReplacerHolderDialog.getInstance() }, new Locale[] {
							Locale.CHINA, Locale.ENGLISH }); //$NON-NLS-1$
			this.add(switchLanhuage);
		}

		this.add( buildServerMenu());
		this.add(buildHelpMenu());
	}

	private JMenu buildServerMenu() {

		JMenu serverMenu = new JMenu(Messages.ui.server); //$NON-NLS-1$
		serverMenu.add(buildActionMenu(Messages.ui.openHome, null, WebLinkAction.createLocalLink("/")));
		serverMenu.add(buildActionMenu(Messages.ui.openToolsHome, null,WebLinkAction.createScriptLink("tools.xhtml")));
		serverMenu.add(buildActionMenu(Messages.ui.openWebRoot, null,JSideWebServer.browseAction));
		return serverMenu;
	}
	private JMenuItem buildActionMenu(String name, String icon,
			ActionListener action) {
		JMenuItem mi = new JMenuItem(name);
		mi.addActionListener(action);
		if (icon!=null) {
			mi.setIcon(new ImageIcon(this.getClass().getResource(icon)));
		}
		return mi;
	}
	private JMenu buildOperationMenu() {

		JMenu operationMenu = new JMenu(Messages.ui.operation); //$NON-NLS-1$

		JMenuItem compressMenu = new JMenuItem(Messages.ui.compress); //$NON-NLS-1$
		compressMenu.setAccelerator(KeyStroke.getKeyStroke("control shift C"));
		compressMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JSAFrame frame = JSAFrame.getInstance();
				Action action = frame.getCurrentEditor().getAction(EditorCommand.COMPRESS_COMMAND);
				if(action!=null){
					action.actionPerformed(e);
				}
			}
		});
		operationMenu.add(compressMenu);

		JMenuItem formatMenu = new JMenuItem(Messages.ui.format); //$NON-NLS-1$
		formatMenu.setAccelerator(KeyStroke.getKeyStroke("control shift F"));

		formatMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JSAFrame frame = JSAFrame.getInstance();
				Action action = frame.getCurrentEditor().getAction(EditorCommand.FORMAT_COMMAND);
				if(action!=null){
					action.actionPerformed(e);
				}
			}
		});
		operationMenu.add(formatMenu);

		JMenuItem analyseMenu = new JMenuItem(Messages.ui.analyse); //$NON-NLS-1$
		analyseMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JSAFrame frame = JSAFrame.getInstance();

				Editor editor = frame.getCurrentEditor();
				AnalyserDialog.getInstance().showAnalyseResult(JSA.getCompressor(), editor.getText(),
						editor.getFile().getAbsolutePath());

			}
		});
		operationMenu.add(analyseMenu);
		JMenuItem settingMenu = new JMenuItem(Messages.ui.setting); //$NON-NLS-1$
		settingMenu.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JSASettingDialog.getInstance().showDialog();
			}

		});
		operationMenu.add(settingMenu);
		return operationMenu;
	}

	private JMenu buildFileMenu() {
		JMenu fileMenu = new JMenu(Messages.ui.file); //$NON-NLS-1$

		JMenuItem create = new JMenuItem(Messages.ui.createFile);
		create.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					JSAFrame.getInstance().createFile(null);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
		});
		fileMenu.add(create);
		
		JMenuItem openMenu = new JMenuItem(Messages.ui.open); //$NON-NLS-1$
		openMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					parent.openFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		fileMenu.add(openMenu);
		JMenuItem saveMenu = new JMenuItem(Messages.ui.save); //$NON-NLS-1$
		saveMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditorCommand.SAVE_ACTION.actionPerformed(null);
			}
		});
		fileMenu.add(saveMenu);
		JMenuItem saveAsMenu = new JMenuItem(Messages.ui.saveAs); //$NON-NLS-1$
		saveAsMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					parent.saveAsFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		fileMenu.add(saveAsMenu);
		return fileMenu;

	}

	private JMenu buildHelpMenu() {
		JMenu helpMenu = new JMenu(Messages.ui.help);
		JMenuItem aboutItem = new JMenuItem(Messages.ui.about); //$NON-NLS-1$
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAbout();
			}

		});
		helpMenu.add(aboutItem);

		JMenuItem helpItem = new JMenuItem(Messages.ui.helpContent); //$NON-NLS-1$
		helpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BrowserDialog.showHTML(parent, this.getClass().getPackage(),"help.html");
			}

		});
		helpMenu.add(helpItem);
		JMenuItem licenseItem = new JMenuItem(Messages.ui.license); //$NON-NLS-1$
		licenseItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BrowserDialog.showHTML(parent, this.getClass().getPackage(),"license-jsa.html");
			}

		});
		helpMenu.add(licenseItem);

		JMenuItem homeItem = new JMenuItem(Messages.ui.homePage); //$NON-NLS-1$
		homeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DesktopUtil.browse("http://www.xidea.org/");
			}

		});
		helpMenu.add(homeItem);
		return helpMenu;
	}

	public void showAbout() {
		JPanel aboutPane = new JPanel();
		aboutPane.setLayout(new BorderLayout());
		aboutPane.setPreferredSize(new Dimension(120, 80));
		aboutPane
				.add(
						new JLabel(
								"JavaScript Analyser " + JSAConfig.getInstance().getVersion()), BorderLayout.CENTER); //$NON-NLS-1$
		JLabel label = new JLabel();
		label.setText("© WWW.XIDEA.ORG");
		aboutPane.add(label, BorderLayout.SOUTH); //$NON-NLS-1$
		JOptionPane.showConfirmDialog(this, aboutPane, "About", //$NON-NLS-1$
				JOptionPane.INFORMATION_MESSAGE & JOptionPane.OK_OPTION);
	}

}
