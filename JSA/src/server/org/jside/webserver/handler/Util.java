package org.jside.webserver.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xidea.el.impl.ExpressionFactoryImpl;
import org.xidea.lite.impl.ParseUtil;

class Util {
	@SuppressWarnings("unchecked")
	static Map<String, Object> loadData(File root, String uri)
			throws IOException {
		String jsonpath = uri.replaceFirst(".\\w+$", ".json");
		Map<String, Object> data = new HashMap<String, Object>();
		if (jsonpath.endsWith(".json")) {
			File df = new File(root, jsonpath);
			if (df.exists()) {
				String source = ParseUtil.loadTextAndClose(new FileInputStream(
						df), null);
				data = (Map<String, Object>) ExpressionFactoryImpl.getInstance()
						.create(source).evaluate(data);
			}
		}
		return data;
	}

	static void writeFile(File file, byte[] litecode) throws IOException {
		file.getParentFile().mkdirs();
		FileOutputStream out1 = new FileOutputStream(file);
		try {
			out1.write(litecode);
			out1.flush();
		} finally {
			out1.close();
		}
	}
}
