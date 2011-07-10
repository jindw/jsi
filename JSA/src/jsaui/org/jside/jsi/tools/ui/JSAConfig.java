package org.jside.jsi.tools.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jside.JSide;
import org.jside.JSideConfig;
import org.jside.jsi.tools.JavaScriptCompressorConfig;

public class JSAConfig extends JavaScriptCompressorConfig {
	private static JSAConfig instance;
	private boolean frameVisible = true;
	private List<String> projectList = new ArrayList<String>();

	public JSAConfig() {
	}

	public static JSAConfig getInstance() {
		if (instance == null) {
			instance = JSide.loadConfig(JSAConfig.class, true);
			String root = JSideConfig.getInstance().getWebRoot();
			ArrayList<String> list = new ArrayList<String>();
			if (root != null && new File(root).exists()) {
				list.add(root);
			} else {
				try {
					list.add(new File("./").getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			instance.setProjectList(list);
		}
		return instance;
	}
	public Collection<String> getFeatures() {
		return this.features;
	}

	public boolean isFrameVisible() {
		return frameVisible;
	}

	public void setFrameVisible(boolean frameVisible) {
		this.frameVisible = frameVisible;
	}

	public List<String> getProjectList() {
		return projectList;
	}

	public void setProjectList(List<String> projectList) {
		this.projectList = projectList;
	}

	public void save() {
		JSide.saveConfig(JSAConfig.class, this);

	}

}
