package org.jside.jsi.tools.ui.frame.project;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jside.JSideConfig;
import org.jside.jsi.tools.ui.frame.JSAFrame;
import org.jside.ui.DockUI;
import org.jside.ui.DesktopUtil;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.PackageSyntaxException;
import org.xidea.jsi.impl.DefaultPackage;
import org.xidea.jsi.web.JSIService;

@SuppressWarnings("serial")
public class PackageWizard extends JDialog {
	static final Pattern PACKAGE_NAME = Pattern
			.compile("^[a-zA-Z][\\w_$]*(?:\\.[a-zA-Z][\\w_$]*)*$");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField nameField;
	private JTextArea sourceField;

	KeyAdapter keyAdapter = new KeyAdapter() {
		public void keyReleased(KeyEvent e) {
			int kc = e.getKeyCode();
			if (kc == KeyEvent.VK_ESCAPE) {
				cancelAction.actionPerformed(null);
			} else if (kc == KeyEvent.VK_ENTER) {
				okAction.actionPerformed(null);
			}
		}
	};

	private Action cancelAction = new AbstractAction("取消") {
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
			dispose();
		}

	};

	private Action okAction = new AbstractAction("确定") {
		public void actionPerformed(ActionEvent e) {
			final String name = nameField.getText();
			final String source = sourceField.getText();

			if (PACKAGE_NAME.matcher(name).find()) {
				File packageDir = new File(scriptBase, name.replace('.', '/'));
				if (!new File(packageDir, JSIPackage.PACKAGE_FILE_NAME)
						.exists()) {
					Set<String> keys = new HashSet<String>();
					try {
						DefaultPackage pkg = loadPackage(name, source);
						pkg.initialize();
						keys.addAll(pkg.getScriptObjectMap().keySet());
					} catch (PackageSyntaxException e2) {
						DesktopUtil.alert("包加载失败：" + e2
								+ "\n包语法错误。请返回修改!!");
						return;
					} catch (Exception e2) {
						if (DesktopUtil.confirm("包加载失败：" + e2
								+ "\n是否返回修改？\n点击否，忽略错误!!")) {
							return;
						}
					}
					packageDir.mkdirs();
					JSAFrame frame = JSAFrame.getInstance();
					
					for (String file : keys) {
						try {
							File f = new File(packageDir, file);
							frame.createFile(f);
						} catch (IOException e1) {
							DesktopUtil.alert("创建文件！！" + name + " 失败");
						}
					}
					try {
						File f = new File(packageDir,
								JSIPackage.PACKAGE_FILE_NAME);
						f.createNewFile();
						FileOutputStream out = new FileOutputStream(f);
						out.write(source.getBytes("utf-8"));
						out.flush();
						out.close();
						frame.openFile(f);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					setVisible(false);
					dispose();
					ProjectTree.getInstance().refresh(sourceNode);
				} else {
					DesktopUtil.alert("包名已存在！！" + name + "\n换个名字吧");
				}
			} else {
				DesktopUtil.alert("非法包名！！" + name + "\n 包名格式是：以'.'连接有效的js 变量名形成。"
						+ "\n例如：org.xidea.jsi");
			}
		}

	};
	private File webRoot;
	private File scriptBase;

	private SourceNode sourceNode;

	public DefaultPackage loadPackage(final String key, final String source) {
		JSIService jsiService = new JSIService() {

			@Override
			public String loadText(String pkgName, String scriptName) {
				if (pkgName.equals(key)) {
					return source;
				}
				return super.loadText(pkgName, scriptName);
			}

		};
		if (scriptBase.exists()) {
			jsiService.addSource(scriptBase);
			jsiService.addLib(scriptBase);
		}
		File lib = new File(webRoot, "WEB-INF/lib/");
		if (lib.exists()) {
			jsiService.addLib(lib);
		}
		return (DefaultPackage) jsiService.findPackageByPath(key + ':');
	}

	public PackageWizard(DockUI parent, SourceNode sourceNode, String packageName) {
		super(parent, "创建JSI脚本包");
		this.sourceNode = sourceNode;
		this.webRoot = new File(JSideConfig.getInstance().getWebRoot());
		this.scriptBase = new File(webRoot, "scripts");
		nameField = new JTextField(packageName);
		sourceField = new JTextArea(
				"this.addScript('test.js',['testVar','testFn']);");
		Container root = this.getContentPane();
		root.setLayout(new BorderLayout());
		root.setPreferredSize(new Dimension(430, 320));
		JLabel sourceLabel = new JLabel("JSI 包描述内容:");
		sourceLabel.setPreferredSize(new Dimension(120, 35));
		root.add(sourceLabel, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(sourceField);
		scrollPane.setPreferredSize(new Dimension(400, 300));
		root.add(scrollPane, BorderLayout.CENTER);

		JPanel infobar = new JPanel();
		infobar.setLayout(new FlowLayout());
		infobar.add(new JLabel("包名:"));
		infobar.add(nameField);
		nameField.setPreferredSize(new Dimension(260, 30));

		infobar.add(new JButton(okAction));
		infobar.add(new JButton(cancelAction));
		root.add(infobar, BorderLayout.SOUTH);
		nameField.addKeyListener(keyAdapter);
		this.addKeyListener(keyAdapter);

	}

	public static void showDialog(String packageName, SourceNode sourceNode, ActionEvent e) {
		PackageWizard wizard = new PackageWizard(DockUI.getInstance(),sourceNode,
				packageName);
		Dimension dm = Toolkit.getDefaultToolkit().getScreenSize();
		wizard.pack();
		wizard.setLocation((dm.width - wizard.getWidth()) / 2,
				(dm.height - wizard.getHeight()) / 2);
		wizard.setVisible(true);
	}

}
