package org.jside.jsi.tools.ui.frame.project;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.tree.TreePath;

import org.jside.JSideConfig;
import org.jside.jsi.tools.ui.Messages;
import org.jside.jsi.tools.ui.frame.BrowserDialog;
import org.jside.jsi.tools.ui.frame.JSAFrame;
import org.jside.ui.DesktopUtil;

public class FileTreeMenuAction implements MouseListener {
	private ProjectTree fileTree;
	private final FileTreePopupMenu fileMenu;
	private final DirTreePopupMenu dirMenu;
	private final SourcePopupMenu sourceMenu;
	private final PackagePopupMenu packageMenu;
	private ProjectFileTreeNode node;

	public FileTreeMenuAction(ProjectTree fileTree) {
		this.fileTree = fileTree;
		fileMenu = new FileTreePopupMenu(fileTree);
		dirMenu = new DirTreePopupMenu(fileTree);
		packageMenu = new PackagePopupMenu(fileTree);
		sourceMenu = new SourcePopupMenu(fileTree);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() > 1 && node != null && node.isLeaf()) {
			JSAFrame.getInstance().openFile(node.getFile());
		}
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		TreePath path = fileTree.getPathForLocation(e.getX(), e.getY());
		if (path == null) {
			this.node = null;
		} else {
			fileTree.setSelectionPath(path);
			this.node = (ProjectFileTreeNode) path.getLastPathComponent();
			doPopup(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		doPopup(e);
	}

	public void doPopup(MouseEvent e) {
		if (e.isPopupTrigger() && node != null) {
			if (node.isLeaf()) {
				long size = node.getFile().length() / 1024;
				if (size > 512) {
					JOptionPane.showMessageDialog(fileTree, "文件太大(" + size
							+ ")，无法打开");
				} else {
					fileMenu.show(node, e);
				}
			} else if (node instanceof PackageNode) {
				packageMenu.show((PackageNode) node, e);
			} else if (node instanceof SourceNode) {
				sourceMenu.show((SourceNode) node, e);
			}  else {
				dirMenu.show(node, e);
			}

		}
	}

}
class PackagePopupMenu extends DirTreePopupMenu{

	private static final long serialVersionUID = 1L;

	public PackagePopupMenu(ProjectTree tree) {
		super(tree);
	}
	protected void initialize() {

		ActionListener createPackageAction = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				PackageWizard.showDialog(((PackageNode)currentNode).toString(),(SourceNode)currentNode.getParent(),e);
			}
		};
		
		JMenuItem createPackage = new JMenuItem(Messages.ui.createPackage);
		createPackage.addActionListener(createPackageAction);
		this.add(createPackage);
		this.addSeparator();
		
		super.initialize();
	}


	
}
class SourcePopupMenu extends DirTreePopupMenu {
	private static final long serialVersionUID = 1L;


	public SourcePopupMenu(final ProjectTree tree) {
		super(tree);
	}

	protected void initialize() {
		ActionListener exportAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BrowserDialog.showHTML(JSAFrame.getInstance(),
						"/scripts/export.action");
			}
		};

		ActionListener createPackageAction = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				PackageWizard.showDialog("",(SourceNode)currentNode,e); 
			}
		};
		
		JMenuItem createPackage = new JMenuItem(Messages.ui.createPackage);
		createPackage.addActionListener(createPackageAction );
		this.add(createPackage);
		
		JMenuItem export = new JMenuItem(Messages.ui.export);
		export.addActionListener(exportAction);
		this.add(export);

		this.addSeparator();
		super.initialize();
	}


}

class DirTreePopupMenu extends JPopupMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected ProjectTree tree;
	protected ProjectFileTreeNode currentNode;

	private ActionListener expandAllAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int count = 0;
			while (true) {
				List<TreePath> result = getSubNodes(tree, tree
						.getSelectionPath());
				int size = result.size();
				if (size > count) {
					for (TreePath p : result) {
						tree.expandPath(p);
					}
					count = size;
				} else {
					break;
				}
			}
			;
		}
	};
	private ActionListener explorerAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			DesktopUtil.explorer(currentNode.getFile());
		}
	};
	private ActionListener collapseAllAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			List<TreePath> result = getSubNodes(tree, tree.getSelectionPath());
			int i = result.size();
			while (i-- > 0) {
				tree.collapsePath(result.get(i));
			}
		}
	};
	private ActionListener createAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				JSAFrame.getInstance().createFile(currentNode.getFile());
				tree.refresh(currentNode);
			} catch (IOException e1) {
				DesktopUtil.alert(e1);
			}
		}
	};
	private ActionListener deleteAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			File file = currentNode.getFile();
			final StringBuilder buf= new StringBuilder(file.toString());
			final ArrayList<File> fileList = new ArrayList<File>();
			fileList.add(file);
			file.listFiles(new FileFilter(){
				public boolean accept(File file) {
					fileList.add(file);
					buf.append("\n");
					buf.append(file);
					if(file.isDirectory()){
						file.listFiles(this);
					}
					return false;
				}
				
			});
			JPanel info = new JPanel();
			info.setLayout(new BorderLayout());
			info.add(new JLabel("操作将删除如下文件:"),BorderLayout.NORTH);
			info.add(new JScrollPane(new JTextArea(buf.toString())),BorderLayout.CENTER);
			info.add(new JLabel("\n\n请检查后确认，操作后不可恢复！！！"),BorderLayout.SOUTH);
			info.setPreferredSize(new Dimension(300,300));
			if (DesktopUtil.confirm(info)) {
				Collections.reverse(fileList);
				for(File file2 : fileList){
					if(!file2.delete()){
						DesktopUtil.alert("文件删除失败:"+file2);;
					}
				}
				tree.refresh((FileTreeNode) currentNode.getParent());
			}
		}
	};
	private ActionListener refreshAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			tree.refresh(currentNode);
		}
	};

	public DirTreePopupMenu(final ProjectTree tree) {
		this.tree = tree;
		initialize();

	}

	protected void initialize() {
		JMenuItem refresh = new JMenuItem(Messages.ui.refreshFile);
		refresh.addActionListener(refreshAction);
		this.add(refresh);

		JMenuItem explorer = new JMenuItem(Messages.ui.explorerFile);
		explorer.addActionListener(explorerAction);
		this.add(explorer);

		this.addSeparator();

		JMenuItem expandAll = new JMenuItem(Messages.ui.expandAll);
		expandAll.addActionListener(expandAllAction);
		this.add(expandAll);

		JMenuItem collapseAll = new JMenuItem(Messages.ui.collapseAll);
		collapseAll.addActionListener(collapseAllAction);
		this.add(collapseAll);

		this.addSeparator();

		JMenuItem create = new JMenuItem(Messages.ui.createFile);
		create.addActionListener(createAction);
		this.add(create);
		
		JMenuItem delete = new JMenuItem(Messages.ui.deleteFile);
		delete.addActionListener(deleteAction);
		this.add(delete);
	}

	public List<TreePath> getSubNodes(ProjectTree tree, TreePath path) {
		int begin = tree.getRowForPath(path);
		int count = tree.getRowCount();
		int end = begin + 1;
		ArrayList<TreePath> result = new ArrayList<TreePath>();
		result.add(path);
		while (end < count) {
			TreePath node = tree.getPathForRow(end++);
			if (!path.isDescendant(node)) {
				break;
			}
			result.add(node);
		}
		return result;
	}

	public void show(ProjectFileTreeNode node, MouseEvent e) {
		this.currentNode = node;
		this.show(e.getComponent(), e.getX(), e.getY());
	}
}

class FileTreePopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	protected ProjectFileTreeNode currentNode;
	private ProjectTree tree;

	ActionListener openAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			File file = currentNode.getFile();
			JSAFrame.getInstance().openFile(file);
		}
	};
	ActionListener browseAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				String file = currentNode.getFile().getCanonicalPath();
				String root = new File(JSideConfig.getInstance().getWebRoot()).getCanonicalPath();
				if(file.startsWith(root)){
					file = file.substring(root.length());
					if(!file.startsWith("/")){
						file = "/"+file;
					}
					DesktopUtil.browse(file);
				}
			} catch (IOException e1) {
				DesktopUtil.alert(e1);
			}
			
		}
	};
	private ActionListener deleteAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			File file = currentNode.getFile();
			if (DesktopUtil.confirm("删除文件:" + file + "？")) {
				if (file.delete())
					tree.refresh((FileTreeNode) currentNode.getParent());
			} else {
				DesktopUtil.alert("文件删除失败");

			}
		}
	};

	public FileTreePopupMenu(ProjectTree tree) {
		this.tree = tree;
		initialize();
	}

	protected void initialize() {
		JMenuItem open = new JMenuItem(Messages.ui.open);
		open.addActionListener(openAction);
		this.add(open);

		JMenuItem web = new JMenuItem(Messages.ui.openPage);
		web.addActionListener(browseAction);
		this.add(web);

		this.addSeparator();

		JMenuItem delete = new JMenuItem(Messages.ui.deleteFile);
		delete.addActionListener(deleteAction);
		this.add(delete);
	}

	public void show(ProjectFileTreeNode node, MouseEvent e) {
		currentNode = node;
		this.show(e.getComponent(), e.getX(), e.getY());
	}
}