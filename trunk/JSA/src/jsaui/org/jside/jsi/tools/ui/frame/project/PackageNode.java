package org.jside.jsi.tools.ui.frame.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

public class PackageNode extends ProjectFileTreeNode implements TreeNode {
	public static final String EMPTY_PACKAGE_NAME = " ";
	public PackageNode(TreeNode parentNode, File file, String name) {
		super(parentNode, file, name.length() == 0 ? EMPTY_PACKAGE_NAME : name);
	}

	protected List<TreeNode> buildChildren() {
		TreeNode parent = EMPTY_PACKAGE_NAME.equals(this.toString()) ? this.getParent() : this;
		ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
		File[] files = this.getFile().listFiles(NOT_PROTECTED_FILTER);
		for (int i = 0; files != null && i < files.length; i++) {
			File file = files[i];
			if (file.isFile()) {
				nodes.add(new ProjectFileTreeNode(parent, file, files[i]
						.getName()));
			}
		}
		return nodes;
	}
}