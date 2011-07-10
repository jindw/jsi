package org.jside.jsi.tools.ui.frame.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class FileTreeNode implements MutableTreeNode {
	protected final File file;
	private final TreeNode parent;
	protected List<TreeNode> nodes;
	private String name;
	private String ext = "";
	@SuppressWarnings("unused")
	private Object userObject;

	public FileTreeNode(TreeNode parent, File file, String name) {
		this.parent = parent;
		this.file = file;
		this.name = name;
		if (file == null || file.isFile()) {
			int p = name.lastIndexOf('.');
			if(p>0){
				ext = name.substring(p+1).toLowerCase();
			}
		}
	}

	protected List<TreeNode> buildChildren() {
		ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
		File[] files = file.listFiles();
		for (int i = 0; files != null && i < files.length; i++) {
			nodes.add(new FileTreeNode(this, files[i], files[i].getName()));
		}
		return nodes;
	}
	protected final List<TreeNode> getChildren() {
		if(this.nodes == null){
			this.nodes = buildChildren();
		}
		return this.nodes;
	}
	public void reset(){
		this.nodes = null;
	}
	@SuppressWarnings("unchecked")
	public Enumeration children() {
		return Collections.enumeration(getChildren());
	}

	public boolean getAllowsChildren() {
		return file == null || file.isDirectory();
	}

	public TreeNode getChildAt(int childIndex) {
		return getChildren().get(childIndex);
	}

	public int getChildCount() {
		return getChildren().size();
	}

	public int getIndex(TreeNode node) {
		int i = 0;
		for (Iterator<TreeNode> it = getChildren().iterator(); it.hasNext(); i++) {
			if (node.equals(it.next())) {
				return i;
			}
		}
		return -1;
	}

	public TreeNode getParent() {
		return parent;
	}

	public boolean isLeaf() {
		return file != null && file.isFile();
	}

	public String toString() {
		return name;
	}

	public File getFile() {
		return file;
	}
	public String getExt() {
		return ext;
	}

	public void insert(MutableTreeNode child, int index) {
		this.getChildren().add(index,child);
		
	}

	public void remove(int index) {
		if(index>=0){
			this.getChildren().remove(index);
		}
	}

	public void remove(MutableTreeNode node) {
		this.remove(this.getIndex(node));
	}

	public void removeFromParent() {
		MutableTreeNode parent = (MutableTreeNode)this.getParent();
		parent.remove(parent.getIndex(this));
	}

	public void setParent(MutableTreeNode newParent) {
		removeFromParent();
		newParent.insert(this, newParent.getChildCount());
	}

	public void setUserObject(Object object) {
		this.userObject = object;
	}

}
