package org.jside.webserver.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.jside.webserver.RequestContext;
import org.jside.webserver.RequestUtil;
import org.xidea.el.impl.ExpressionFactoryImpl;
import org.xidea.lite.impl.HotLiteEngine;
import org.xidea.lite.impl.ParseUtil;
import org.xidea.lite.tools.LiteCompiler;
import org.xidea.lite.tools.ResourceManager;

class TemplateUtil {

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
				data = (Map<String, Object>) ExpressionFactoryImpl
						.getInstance().create(source).evaluate(data);
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

	static void compileLite(TemplateServletImpl templateServlet,
			RequestContext context) throws FileNotFoundException, IOException,
			UnsupportedEncodingException {
		String path = context.getParam().get("path");
		HotLiteEngine templateEngine = (HotLiteEngine) templateServlet
				.getTemplateEngine();
		ResourceManager resourceManager = templateServlet.getResourceManager();
		File root = new File(resourceManager.getRoot());
		String litecode = templateEngine.getLitecode(path);
		String phpcode = LiteCompiler.buildPHP(path, litecode);
		String litecodepath = "/WEB-INF/litecode/" + path.replace('/', '^');
		TemplateUtil.writeFile(new File(root, litecodepath), litecode
				.getBytes("UTF-8"));
		File php = new File(root, litecodepath + ".php");
		TemplateUtil.writeFile(php, phpcode.getBytes(templateEngine
				.getTemplate(path).getEncoding()));
		RequestUtil.printResource("{\"php\":\"" + php.toURI() + "\"}",
				"text/javascript;charset=utf-8");
	}

	static InternalTemplate dirTemplate = new InternalTemplate("dir.xhtml");
	static InternalTemplate missedEngineTemplate = new InternalTemplate(
			"missed-engine.xhtml");

	static void printDir(File root, final String path) throws IOException {
		File f = new File(root, path);
		if (!path.endsWith("/")) {
			RequestUtil.sendRedirect(path + '/');
		} else {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("root", root);
			map.put("path", path);
			map.put("fileList", f.listFiles());
			dirTemplate.render(map);
		}
	}

	public static void printNotSupport(File root, final String path,
			String engineName, Throwable e) throws IOException {
		HashMap<String, Object> map = new HashMap<String, Object>();
		StringWriter out = new StringWriter();
		if (e != null) {
			e.printStackTrace(new PrintWriter(out, true));
		}
		map.put("engineName", engineName);
		map.put("error", out);
		map.put("root", root);
		map.put("path", path);
		missedEngineTemplate.render(map);
	}
}
