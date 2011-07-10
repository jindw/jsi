package org.jside.jsi.tools.ui.frame.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.jside.jsi.tools.ui.JSAConfig;
import org.jside.jsi.tools.ui.frame.project.ProjectConfig;

public class ProjectRootNode extends ProjectFileTreeNode {

	public ProjectRootNode() {
		super(null, null, " ");

	}

	protected List<TreeNode> buildChildren() {
		List<String> list = JSAConfig.getInstance().getProjectList();

		ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
		for (String projectDir : list) {
			ProjectConfig config = ProjectConfig.load(new File(projectDir));
			nodes.add(new ProjectNode(this, config));
		}
		return nodes;
	}

}