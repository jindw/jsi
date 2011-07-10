package org.jside.jsi.tools.ui.frame.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.jside.jsi.tools.ui.frame.project.ProjectConfig;


public class ProjectNode extends ProjectFileTreeNode implements TreeNode {
	private ProjectConfig config;

	public ProjectNode(TreeNode parent,ProjectConfig config) {
		super(parent, null, config.getName());
		this.config = config;
	}

	protected List<TreeNode> buildChildren() {
		List<TreeNode>  nodes = new ArrayList<TreeNode>();
		File scriptFile = config.getScriptBaseFile();
		nodes.add(new SourceNode(this,scriptFile));
		File[] files = config.getWebRootFile().listFiles(NOT_PROTECTED_FILTER);
		for(File file:files){
			if(!file.equals(scriptFile)){
				nodes.add(new ProjectFileTreeNode(this,file,file.getName()));
			}
		}
		return nodes;
	}

	public ProjectConfig getConfig() {
		return config;
	}
	

}