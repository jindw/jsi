package org.jside.jsi.tools.ui.frame.project;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;


class ProjectFileTreeNode extends FileTreeNode{
	public static FilenameFilter NOT_PROTECTED_FILTER = new FilenameFilter(){
		public boolean accept(File dir, String name) {
			return !name.startsWith(".");
		}
		
	};
	public ProjectFileTreeNode(TreeNode parent, File file, String name) {
		super(parent, file, name);
	}
	

	protected List<TreeNode> buildChildren() {
		ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
		File[] files = file.listFiles(NOT_PROTECTED_FILTER);
		for (int i = 0; files != null && i < files.length; i++) {
			nodes.add(new ProjectFileTreeNode(this, files[i], files[i].getName()));
		}
		return nodes;
	}
}
