package org.xidea.jsi.impl;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xidea.jsi.JSIRoot;

public class DataJSIRoot extends AbstractJSIRoot implements JSIRoot {
	private Map<String, String> dataMap;

	public DataJSIRoot(String source) {
		source = source.replaceAll("$\\s*<\\?[^>]\\?>", "");
		try {
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(
							new ByteArrayInputStream(source.getBytes("utf-8")));
			String imports = doc.getDocumentElement().getAttribute("export");
			NodeList nodes = doc.getElementsByTagName("script");
			HashMap<String, String> dataMap = new HashMap<String, String>();
			for (int i = nodes.getLength() - 1; i >= 0; i--) {
				Element node = (Element) nodes.item(i);
				String path = node.getAttribute("path");
				String content = node.getTextContent();
				dataMap.put(path, content);
			}
			dataMap.put("/export", imports);
			this.dataMap = dataMap;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

//	public List<String> getImports() {
//		return Arrays.asList(dataMap.get("/export").split("[,\\s]"));
//	}

	public DataJSIRoot(Map<String, String> dataMap) {
		this.dataMap = dataMap;
	}

	public String loadText(String pkgName, String scriptName) {
		pkgName = pkgName.replace('.', '/');
		return dataMap.get(pkgName + '/' + scriptName);
	}

}
