package org.jside.jsi.tools.ui.frame.project;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.jside.jsi.tools.ui.Messages;

/**
 * Display a file system in a JTree view
 * 
 * @version $Id: ProjectTree.java,v 1.1 2008/08/30 05:57:42 jindw Exp $
 * @author Ian Darwin
 */
public class ProjectTree extends JTree {
	private static final long serialVersionUID = 1L;
	private static ProjectTree instance;
	private static List<String> MLS = Arrays.asList("xml","xhtml","html","htm");

	public static ProjectTree getInstance() {
		if (instance == null) {
			instance = new ProjectTree();
		}
		return instance;
	}

	private ProjectTree() {
		super(new ProjectRootNode());
		this.expandRow(1);
		this.expandRow(2);
		this.initialize();
	}
	
	public void refresh(FileTreeNode node){
		DefaultTreeModel model = (DefaultTreeModel)this.getModel();
		node.reset();
		model.reload(node);
	}

	private void initialize() {
		// this.setComponentPopupMenu(new FileTreePopupMenu());
		this.setRootVisible(false);
		this.setShowsRootHandles(true);
		this.addMouseListener(new FileTreeMenuAction(this));
		this.setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {

				super.getTreeCellRendererComponent(tree, value, sel, expanded,
						leaf, row, hasFocus);
				if (value instanceof ProjectNode) {
					setIcon(Messages.ui.projectCloseIcon);
				} else if (value instanceof SourceNode) {
					setIcon(Messages.ui.sourceIcon);
				} else if (value instanceof PackageNode) {
					setIcon(Messages.ui.packageIcon);
				} else {
					ProjectFileTreeNode node = (ProjectFileTreeNode) value;
					if(node.file != null && node.file.isDirectory()){
						setIcon(Messages.ui.dirIcon);
					}else if (node.getExt().equals("js")) {
						setIcon(Messages.ui.jsFileIcon);
					}else if (MLS.contains(node.getExt())) {
						setIcon(Messages.ui.xmlIcon);
					}else{
						setIcon(Messages.ui.fileIcon);
					}
				}
				return this;
			}
		});
	}
}


