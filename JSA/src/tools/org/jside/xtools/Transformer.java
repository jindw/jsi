package org.jside.xtools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jside.ui.FileDropTargetAction;

public abstract class Transformer extends JFrame {

	private static final long serialVersionUID = 1L;
	protected JFileChooser fileChooser = new JFileChooser();
	private JTextField sourceInput = new JTextField();
	protected JButton sourceButton = new JButton("选择源文件目录");
	private JTextField destInput = new JTextField();
	protected JButton destButton = new JButton("选择源文件目录");
	protected JButton beginButton = new JButton("开始转换");
	protected JTextArea output = new JTextArea();
	protected JTextField extInput = new JTextField("js,java,html,htm,xhtml");
	{
		fileChooser.setDialogTitle("选择目录");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "目录";
			}

		});
	}

	protected String toPath(File sourceFile) throws IOException {
		String sourcePath = sourceFile.getAbsoluteFile().getCanonicalPath();
		if (sourceFile.isDirectory()) {
			if (!sourcePath.endsWith(File.separator)) {
				sourcePath += File.separator;
			}
		}
		return sourcePath;
	}

	public void log(String msg) {
		this.output.append(msg + "\r\n");
	}

	protected void initializeUI() {
		this.setSize(new Dimension(780, 600));
		this.setPreferredSize(new Dimension(780, 600));

		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(4, 4, 4, 4);

		c.weightx = 6.0;
		c.gridwidth = 1;
		c.gridx = GridBagConstraints.RELATIVE;
		this.add(sourceInput, c);

		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		this.add(sourceButton, c);

		c.weightx = 6.0;
		c.gridwidth = 1;
		this.add(destInput, c);

		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		this.add(destButton, c);

		c.weightx = 1.0;
		c.gridwidth = 1;
		JPanel encodingPanel = buildSetting();

		this.add(encodingPanel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		this.add(beginButton, c);

		c.weighty = 1.0;
		this.add(new JLabel("控制台输出"), c);
		c.weighty = 100;
		output.setBorder(BorderFactory.createEtchedBorder());
		this.add(new JScrollPane(output), c);
	}

	protected JPanel buildSetting() {
		JPanel panel = new JPanel();
		JLabel extinfo = new JLabel("接受文件");
		extinfo.setToolTipText("扩展名','隔开");
		panel.add(extinfo);
		extInput.setToolTipText("扩展名','隔开");
		panel.add(extInput);
		return panel;
	}

	private DropTarget createDropTarget(final Component cmp, final boolean source,
			final boolean dest) {
		return new DropTarget(cmp, new FileDropTargetAction() {
			
			@Override
			protected void openFile(File file) throws IOException {
				try {
					if (source) {
						setFile(sourceInput,file);
					} else if (dest) {
						setFile(destInput,file);
					} else {
						if (sourceInput.getText().trim().length() > 0) {
							int rtv = alert("源代码目录已经设置，重新设置源码目录点击YES（是）");
							if (rtv == JOptionPane.YES_OPTION) {
								setFile(sourceInput,file);
							} else{

								if (destInput.getText().trim().length() > 0) {
									rtv = alert("源代码目录已经设置，重新设置源码目录点击YES（是）");
									if (rtv == JOptionPane.YES_OPTION) {
										setFile(destInput,file);
									}
								}else{
									setFile(destInput,file);
								}

							}
						}else{
							setFile(sourceInput,file);
						}
					}
				} catch (IOException e1) {
					output.append("ERROR:" + e1.getMessage());
				}
			}

			@Override
			protected boolean accept(File file) {
				return file.isDirectory();
			}
		});
	}

	protected void initializeEvent() {

		output.setDropTarget(createDropTarget(output,false,false));
		this.setDropTarget(createDropTarget(this,false,false));
		sourceButton.setDropTarget(createDropTarget(sourceButton,true,false));
		sourceInput.setDropTarget(createDropTarget(sourceInput,true,false));
		sourceInput.setDropMode(DropMode.INSERT);
		destButton.setDropTarget(createDropTarget(destButton,false,true));
		destInput.setDropTarget(createDropTarget(destInput,false,true));
		destInput.setDropMode(DropMode.INSERT);
		
		sourceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.setSelectedFile(new File(sourceInput.getText()));
				int status = fileChooser.showOpenDialog(Transformer.this);
				if (status == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					try {
						setFile(sourceInput,file);
					} catch (IOException e1) {
						alert("ERROR:" + e1.getMessage());
					}
				}
			}
		});

		destButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.setSelectedFile(new File(destInput.getText()));
				int status = fileChooser.showOpenDialog(Transformer.this);
				if (status == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					try {
						setFile(destInput,file);
					} catch (IOException e1) {
						alert("ERROR:" + e1.getMessage());
					}
				}
			}
		});
		beginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				transform();
			}
		});
	}
	private void setFile(JTextField input,File file) throws IOException {
		String oldValue = input.getText();
		String value = file.getAbsoluteFile().getCanonicalPath();
		input.setText(value);
		String sourcePath = sourceInput.getText();
		String destPath = destInput.getText();
		if(sourcePath.trim().length()>0 && destPath.trim().length()>0){
			sourcePath = toPath(sourcePath);
			destPath = toPath(destPath);
			if (destPath.startsWith(sourcePath)) {
				input.setText(oldValue);
				throw new IOException("目标地址不能在源地制下");
			}
		}

	}
	private String toPath(String sourcePath) throws IOException {
		File sourceFile = new File(sourcePath);
		sourcePath = sourceFile.getAbsoluteFile().getCanonicalPath();
		if(sourceFile.isDirectory()){
			if(!sourcePath.endsWith(File.separator)){
				sourcePath += File.separator;
			}
		}
		return sourcePath;
	}

	protected void transform() {
		File sourceFile = new File(sourceInput.getText());
		String dest = destInput.getText();
		if (sourceFile.exists() && dest.length() > 0) {
			File destFile = new File(dest);
			if (destFile.exists()) {
				int status = alert(
						"目标目录已经存在:" + destFile + "\r\b继续操作将覆盖目标目录");
				if (status != JOptionPane.OK_OPTION) {
					return;
				}
			}
			try {
				String sourcePath = toPath(sourceFile);
				String destPath = toPath(destFile);
				if (sourcePath.equals(destPath)
						|| !destPath.startsWith(sourcePath)) {
					walk(sourceFile, destFile);
				} else {
					alert("源代码目录包含目标目录,无法完成操作");
				}

			} catch (IOException e1) {
				alert(e1.toString());
			}
		} else {
			alert("请选择源文件目录和目标文件目录");
		}
	}

	private int alert(String msg) {
		return JOptionPane.showConfirmDialog(Transformer.this,
				msg);
	}

	protected void walk(final File source, final File dest) {
		if (!dest.exists()) {
			dest.mkdirs();
		}
		final HashSet<String> waterExtMap = new HashSet<String>(Arrays
				.asList(extInput.getText().split("[,]")));
		source.listFiles(new java.io.FileFilter() {
			public boolean accept(File file) {
				if (file.isDirectory()) {
					if (!file.getName().startsWith(".")) {
						walk(file, new File(dest, file.getName()));
					}
				} else {
					try {
						String name = file.getName();
						String ext = name.substring(name.lastIndexOf('.') + 1)
								.toLowerCase();
						if (waterExtMap.contains(ext)) {
							transform(file, new File(dest, name));
						} else {
							copy(file, new File(dest, name));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return false;
			}
		});
	}

	protected void copy(File source, File dest) throws IOException {
		if (!source.equals(dest)) {
			dest.createNewFile();
			FileInputStream in = new FileInputStream(source);
			FileOutputStream out = new FileOutputStream(dest);

			byte[] cbuf = new byte[1024];
			int count;
			while ((count = in.read(cbuf)) >= 0) {
				out.write(cbuf, 0, count);
			}
			out.flush();
			out.close();
		}
	}

	protected abstract void transform(final File source, File dest)
			throws IOException;

}