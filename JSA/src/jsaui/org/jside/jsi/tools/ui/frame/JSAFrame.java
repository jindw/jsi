package org.jside.jsi.tools.ui.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.jside.JSide;
import org.jside.JSideConfig;
import org.jside.JSideListener;
import org.jside.jsi.tools.ui.JSAConfig;
import org.jside.jsi.tools.ui.Messages;
import org.jside.jsi.tools.ui.frame.project.ProjectTree;
import org.jside.ui.DockUI;
import org.jside.ui.SplashWindow;
import org.jside.ui.TrayUI;
import org.jside.ui.UICharsetSelector;
import org.jside.ui.DesktopUtil;
import org.xidea.commons.i18n.CharsetSelector;

public class JSAFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static JSAFrame instance;

	public static JSAFrame getInstance() {
		if (instance == null) {
			SplashWindow.showSplash("UI 界面初始化...");
			try {
				instance = new JSAFrame();
				instance.setState(ICONIFIED);
			} catch (Exception e) {
				e.printStackTrace();
			}
			SplashWindow.closeSplash();

		}
		return instance;
	}

	public static void showUI() {
		JSAFrame frame = getInstance();
		//frame.pack();
		frame.setVisible(true);
		frame.setState(NORMAL);
		frame.toBack();
		frame.toFront();
		frame.setState(NORMAL);

		JSAConfig ac = JSAConfig.getInstance();
		ac.setFrameVisible(true);
		ac.save();
	}

	private ContentTabbedPane content = ContentTabbedPane.getInstance();
	protected JPanel topAds = new ADPanel();
	protected final CharsetSelector charsetSelector = new UICharsetSelector(
			this);

	private WindowListener windowCloseListener = new WindowAdapter() {
		boolean isNotifyed = false;

		public void windowClosing(WindowEvent e) {
			windowIconified(e);
		}


		@Override
		public void windowIconified(WindowEvent e) {
			JSAConfig ac = JSAConfig.getInstance();
			ac.setFrameVisible(false);
			ac.save();
			if (isNotifyed) {
				setVisible(false);
			} else {
				if (!isNotifyed) {
					isNotifyed = true;
					TrayUI.showTrayMessage("其实我还在这里", "如果你希望我彻底消失,您可以点击右键退出");
				}
				setVisible(false);
			}

		}

	};


	private JSAFrame() {
		if (instance == null) {
			instance = this;
		} else {
			throw new RuntimeException("CompressorFrame 不能启动多实例");
		}
		this
				.setDropTarget(new DropTarget(this,
						DockUI.getInstance().dropAction));
		JSide.addListener( new JSideListener.Exit(){
			public boolean execute(Object source) {
				EditorCommand.CLOSE_ALL_ACTION.doClose();
				return false;
			}
			
		});
		this.setTitle(Messages.ui.title); //$NON-NLS-1$
		this.setJMenuBar(new JSAMenuBar(this));
		this.getContentPane().setLayout(new BorderLayout());
		topAds.setPreferredSize(new Dimension(750, 40));
		this.getContentPane().add(topAds, BorderLayout.SOUTH);

		JSplitPane spp = new JSplitPane();
		ProjectTree project = ProjectTree.getInstance();
		spp.setRightComponent(new JScrollPane(project));
		spp.setLeftComponent(content);
		spp.setResizeWeight(0.80);
		spp.setPreferredSize(new Dimension(750, 520));
		project.setPreferredSize(new Dimension(60, 520));
		this.getContentPane().add(spp, BorderLayout.CENTER);
		addWindowListener(windowCloseListener);
		this.setLocation(80, 80);
		this.pack();
	}

	public void openFile() throws IOException {
		File content = DesktopUtil.openFileDialog(JSideConfig.getInstance().getWebRoot()+"/.");
		if (content != null) {
			openFile(content);
		}
	}

	public void createFile(File file) throws IOException {
		if(file == null ){
			file = DesktopUtil.openFileDialog(JSideConfig.getInstance().getWebRoot()+"/.");
		}else if(file.isDirectory()){
			file = DesktopUtil.openFileDialog(file.getAbsolutePath()+"/.");
		}
		if (file != null) {
			if(!file.exists()){
				file.createNewFile();
			}
			openFile(file);
		}
		
	}

	public void openFile(File file) {
		content.openFile(file);
	}

	public void saveAsFile() throws IOException {
		try {
			Editor editor = getCurrentEditor();
			String filePath = editor.getFile().getAbsolutePath();
			File file = DesktopUtil.saveFileDialog(new ByteArrayInputStream(getCurrentEditor().getText()
					.getBytes(editor.getEncoding())), null, filePath);
			if (file != null) {
				editor.reset(file, null);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, new JTextArea(ex.getMessage()),
					"Error", //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
		}
	}
	public Editor getCurrentEditor(){
		return content.getCurrentEditor();
	}

	public void showText(String title, String content) {
		JScrollPane scrollPane= new JScrollPane(new JTextArea(
				content));
		scrollPane.setPreferredSize(new Dimension(300,400));
		JOptionPane.showMessageDialog(this, scrollPane, title, //$NON-NLS-1$
				JOptionPane.INFORMATION_MESSAGE);
	}
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
	}


}