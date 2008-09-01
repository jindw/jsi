package org.xidea.jsi.impl;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xidea.jsi.JSIRoot;

public class DataJSIRoot extends AbstractJSIRoot implements JSIRoot {
	private Properties dataMap;
	public DataJSIRoot(String source) {
		if (source != null) {
			source = source.replaceAll("$\\s*<\\?[^>]\\?>", "");
			try {
				Properties data = new Properties();
				data.loadFromXML(new ByteArrayInputStream(source
						.getBytes("utf-8")));
				this.dataMap = data;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	public DataJSIRoot(Map<String, String> dataMap) {
		Properties data = new Properties();
		data.putAll(dataMap);
		this.dataMap = data;
	}

	public String loadText(String pkgName, String scriptName) {
		pkgName = pkgName.replace('.', '/');
		return dataMap.getProperty(pkgName + '/' + scriptName);
	}

}
