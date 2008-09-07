package org.xidea.jsi.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSIExportorFactory;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.DataJSIRoot;
import org.xidea.jsi.impl.DefaultJSILoadContext;
import org.xidea.jsi.impl.JSIUtil;

public class JSIService {
	protected String scriptBase;
	protected String absoluteScriptBase;
	/**
	 * 只有默认的encoding没有设置的时候，才会设置
	 */
	protected String encoding = null;
	protected final static JSIExportorFactory exportorFactory = JSIUtil
			.getExportorFactory();

	public String getScriptBase() {
		return scriptBase;
	}

	public void setScriptBase(String scriptBase) {
		this.scriptBase = scriptBase;
	}

	public String getAbsoluteScriptBase() {
		return absoluteScriptBase;
	}

	public void setAbsoluteScriptBase(String absoluteScriptBase) {
		this.absoluteScriptBase = absoluteScriptBase;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public JSIService() {
		super();
	}

	public boolean printResource(String path, OutputStream out)
			throws IOException {
		boolean isPreload = false;
		if (path.endsWith(JSIUtil.PRELOAD_FILE_POSTFIX)) {
			isPreload = true;
			path = path.replaceFirst(JSIUtil.PRELOAD_FILE_POSTFIX + "$", ".js");
		}
		InputStream in = this.getResourceStream(path);
		if (in != null) {
			this.printResource(path, isPreload, in, out);
			return true;
		}
		return false;

	}

	protected void printResource(String path, boolean isPreload,
			InputStream in, OutputStream out) throws IOException {
		if (isPreload) {
			out.write(JSIUtil.buildPreloadPerfix(path).getBytes());
			output(in, out);
			out.write(JSIUtil.buildPreloadPostfix().getBytes());
		} else {
			output(in, out);
		}
	}

	public String export(String content) throws IOException {
		if (content != null) {
			final DataJSIRoot root = new DataJSIRoot(content);
			String type = root.loadText(null, "#type");
			JSIExportor exportor;
			if ("report".equals(type)) {
				exportor = exportorFactory.createReportExplorter();
			} else if ("confuse".equals(type)) {
				// String prefix = root.loadText(null, "#prefix");
				exportor = exportorFactory.createConfuseExplorter();// confuseUnimported
			} else {
				exportor = exportorFactory.createSimpleExplorter();
			}
			if (exportor == null) {
				return null;
			}
			String importString = root.loadText(null, "#export");
			JSILoadContext context = new DefaultJSILoadContext();
			if (importString != null) {
				String[] imports = importString.split("\\s*,\\s*");
				// 只有Data Root 才能支持这种方式
				for (String item : imports) {
					root.$import(item, context);
				}
			}
			return exportor.export(context, new HashMap<String, String>(){
				@Override
				public String get(Object key) {
					return root.loadText(null,String.valueOf(key));
				}});
		}else{
			return exportorFactory.createConfuseExplorter() == null?null:"";
		}

		
	}

	public String document() {
		List<String> packageList = JSIUtil.findPackageList(new File(
				this.absoluteScriptBase));
		StringWriter out = new StringWriter();
		if (packageList.isEmpty()) {
			out.append("<html><body> 未发现任何托管脚本包，无法显示JSIDoc。<br /> ");
			out.append("请添加脚本包，并在包目录下正确添加相应的包定义文件 。");
			out.append("<a href='org/xidea/jsidoc/index.html?group={\"example\":[\"example\",\"example.internal\",\"org.xidea.jsidoc\"]}'>");
			out.append("察看示例</a>");
			out.append("</body><html>");
		} else {

			out.append("<html><frameset rows='100%'>");
			out.append("<frame src='org/xidea/jsidoc/index.html?");
			out.append("group={\"All\":");
			out.append("[\"");
			boolean isFirst = true;
			for (String packageName : packageList) {
				if (isFirst) {
					isFirst = false;
				} else {
					out.append("\",\"");
				}
				out.append(packageName);

			}
			out.append("\"]}'> </frameset></html>");
		}
		return out.toString();
	}

	public boolean isIndex(String path) {
		return path.length() == 0 || path.equals("index.jsp")
				|| path.equals("index.php");
	}

	public InputStream getResourceStream(String path) {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(
				path);
		if (in == null) {
			File dir = new File(absoluteScriptBase);
			File file = new File(dir, path);
			if (file.exists()) {
				try {
					return new FileInputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			File[] list = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return false;
				}
			});
			if (list != null) {
				int i = list.length;
				while (i-- > 0) {
					in = findByXML(list[i], path);
				}
			}
		}
		return in;
	}

	protected InputStream findByXML(File file, String path) {
		Properties ps = new Properties();
		try {
			ps.loadFromXML(new FileInputStream(file));
			String value = ps.getProperty(path);
			if (value != null) {
				byte[] data = value.getBytes(encoding == null ? "utf8"
						: encoding);
				return new ByteArrayInputStream(data);
			} else {
				value = ps.getProperty(path + "#base64");
				if (value != null) {
					byte[] data = new sun.misc.BASE64Decoder()
							.decodeBuffer(value);
					return new ByteArrayInputStream(data);
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static void output(InputStream in, OutputStream out)
			throws IOException {
		byte[] buf = new byte[1024];
		int len = in.read(buf);
		while (len > 0) {
			out.write(buf, 0, len);
			len = in.read(buf);
		}
	}

}