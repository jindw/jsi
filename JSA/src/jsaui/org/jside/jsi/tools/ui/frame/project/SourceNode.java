package org.jside.jsi.tools.ui.frame.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import javax.swing.tree.TreeNode;

public class SourceNode extends ProjectFileTreeNode implements TreeNode {

	public SourceNode(ProjectNode projectNode, File file) {
		super(projectNode, file, file.getName());
	}

	@SuppressWarnings("unchecked")
	protected List<TreeNode> buildChildren() {
		TreeMap<String, PackageNode> map = new TreeMap<String, PackageNode>();
		this.buildChildren(this.getFile(), map, null);
		PackageNode rootNode = map.remove("");
		List<TreeNode> result = new ArrayList<TreeNode>(map.values()); 
		if(rootNode!=null){
			result.addAll(Collections.list(rootNode.children()));
		}
		return result;
	}

	private void buildChildren(File file, TreeMap<String, PackageNode> map,
			String prefix) {
		if (prefix == null) {
			prefix = "";
		} else if (prefix.length() == 0) {
			prefix = file.getName();
		} else {
			prefix = prefix + '.' + file.getName();
		}
		File[] files = file.listFiles(NOT_PROTECTED_FILTER);
		for (int i = 0; files != null && i < files.length; i++) {
			File subfile = files[i];
			if (subfile.isDirectory()) {
				buildChildren(subfile, map, prefix);
			} else {
				PackageNode pkgNode = map.get(prefix);
				if (pkgNode == null) {
					pkgNode = new PackageNode(this, file, prefix);
					map.put(prefix, pkgNode);
				}
				// pkgNode.addFile(subfile);
			}
		}
	}
}