package org.jside;


public class JSideConfig{
	private static JSideConfig instance;
	private String webRoot = null;

	public static JSideConfig getInstance() {
		if (instance == null) {
			instance = JSide.loadConfig(JSideConfig.class, true);
		}
		return instance;
	}

	public String getWebRoot() {
		return webRoot;
	}

	public void setWebRoot(String webRoot) {
		this.webRoot = webRoot;
	}

	public void save() {
		JSide.saveConfig(JSideConfig.class, this);
	}

}
