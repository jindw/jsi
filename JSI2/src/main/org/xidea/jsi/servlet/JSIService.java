package org.xidea.jsi.servlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;


import org.xidea.jsi.JSIExportorFactory;
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
	public JSIService() {
		super();
	}
	protected void printDocument(PrintWriter out) {
		List<String> packageList = JSIUtil.findPackageList(new File(
				this.absoluteScriptBase));

		if (packageList.isEmpty()) {
			out
					.print("<html><body> 未发现任何托管脚本包，无法显示JSIDoc。<br /> 请添加脚本包，并在包目录下正确添加相应的包定义文件 。</body><html>");
		} else {

			out.print("<html><frameset rows='100%'>");
			out.print("<frame src='org/xidea/jsidoc/index.html?");
			out.print("group={\"All\":");
			out.print("[\"");
			boolean isFirst = true;
			for (String packageName : packageList) {
				if (isFirst) {
					isFirst = false;
				} else {
					out.print("\",\"");
				}
				out.print(packageName);

			}
			out.print("\"]'> </frameset></html>");
		}
	}

	protected boolean isIndex(String path) {
		return path.length() == 0 || path.equals("index.jsp")
				|| path.equals("index.php");
	}

	protected InputStream getResourceStream(String path) {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(path);
		if (in == null) {
			File dir = new File(absoluteScriptBase);
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

	protected void output(InputStream in, OutputStream out)
			throws IOException {
		byte[] buf = new byte[1024];
		int len = in.read(buf);
		while (len > 0) {
			out.write(buf, 0, len);
			len = in.read(buf);
		}
	}

}