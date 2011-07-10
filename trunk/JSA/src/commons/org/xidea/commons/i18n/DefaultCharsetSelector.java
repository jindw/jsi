package org.xidea.commons.i18n;

public class DefaultCharsetSelector implements CharsetSelector {
	protected String defaultCharset = "utf-8";

	public DefaultCharsetSelector() {
	}
	public DefaultCharsetSelector(String defaultCharset) {
		this.defaultCharset = defaultCharset;
	}

	public String selectCharset(String[] options) {
		for (int i = 0; i < options.length; i++) {
			if (defaultCharset.equalsIgnoreCase(options[i])) {
				return defaultCharset;
			}
		}
		return options.length > 0 ? options[0] : defaultCharset;
	}

}
