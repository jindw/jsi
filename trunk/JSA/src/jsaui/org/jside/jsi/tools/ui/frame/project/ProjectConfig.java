package org.jside.jsi.tools.ui.frame.project;

import java.io.File;
import java.io.IOException;

import org.jside.JSide;

public class ProjectConfig {
	private static String CONFIG_PATH = "WEB-INF/.jsi-project.xml";
	private String name;
	private File root;
	private String webRoot = "/";
	private String scriptBase = "scripts/";

	public static ProjectConfig load(File dir) {
		ProjectConfig conf = JSide.loadConfig(new File(dir,
				CONFIG_PATH), ProjectConfig.class);
		conf.root = dir;
		if (conf.name == null) {
			conf.name = dir.getName().toUpperCase();
		}
		return conf;
	}

	public void save() {
		try {
			File config = new File(root, CONFIG_PATH);
			if (config.exists() || !config.isFile()) {
				if (config.getParentFile().isDirectory()) {
					config.getParentFile().mkdirs();
				}
				config.createNewFile();
			}
			JSide.saveConfig(config, this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public ProjectConfig() {
	}

	public String getWebRoot() {
		return webRoot;
	}

	public void setWebRoot(String webRoot) {
		this.webRoot = webRoot;
	}

	public String getScriptBase() {
		return scriptBase;
	}

	public void setScriptBase(String scriptBase) {
		this.scriptBase = scriptBase;
	}

	public File getWebRootFile() {
		File file = this.root;
		if (!"/".equals(webRoot)) {
			file = new File(file, webRoot);
		}
		return file;
	}

	public File getScriptBaseFile() {
		File file = this.getWebRootFile();
		if (!"/".equals(scriptBase)) {
			file = new File(file, scriptBase);
		}
		return file;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getRoot() {
		return root;
	}

	public void setRoot(File root) {
		this.root = root;
	}

}
