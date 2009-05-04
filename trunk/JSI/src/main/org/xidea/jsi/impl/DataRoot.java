package org.xidea.jsi.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIRoot;

public class DataRoot extends AbstractRoot implements JSIRoot {
	private static final Log log = LogFactory.getLog(DataRoot.class);
	private static final String PROP_DOCTYPE = "<!DOCTYPE properties SYSTEM 'http://java.sun.com/dtd/properties.dtd'>";
	protected Properties dataMap;

	public DataRoot(String source) {
		if (source != null) {
			source = source.replaceAll("$\\s*<\\?[^>]\\?>", "").trim();
			try {
				if(!source.startsWith("<!DOCTYPE properties")){
					log.error("JavaProperties 必须带DOCTYPE 信息");
					source = PROP_DOCTYPE+source;
				}
				Properties data = new Properties();
				data.loadFromXML(new ByteArrayInputStream(source
						.getBytes("utf-8")));
				this.dataMap = data;
			} catch (Exception e) {
				log.warn(e);;
				throw new RuntimeException(e);
			}
		}
	}

	public DataRoot(Map<String, String> dataMap) {
		Properties data = new Properties();
		data.putAll(dataMap);
		this.dataMap = data;
	}

	@SuppressWarnings("unchecked")
	public List<String> listPath() {
		return new ArrayList(dataMap.keySet());
	}

	public void setContent(String path, String context) {
		dataMap.put(path, context);
	}

	public String loadText(String pkgName, String scriptName) {
		if (pkgName != null && pkgName.length() > 0) {
			scriptName = pkgName.replace('.', '/') + '/' + scriptName;
		}
		return dataMap.getProperty(scriptName);
	}

}
