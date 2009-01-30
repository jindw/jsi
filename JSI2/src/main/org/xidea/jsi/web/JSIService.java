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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.impl.DataJSIRoot;
import org.xidea.jsi.impl.DefaultJSIExportorFactory;
import org.xidea.jsi.impl.DefaultJSILoadContext;
import org.xidea.jsi.impl.FileJSIRoot;
import org.xidea.jsi.impl.JSIText;

public class JSIService {
	protected String scriptBase;
	protected File scriptBaseDirectory;
	protected File externalLibraryDirectory;
	/**
	 * 只有默认的encoding没有设置的时候，才会设置
	 */
	protected String encoding = null;
	public void setScriptBase(String scriptBase) {
		this.scriptBase = scriptBase;
	}
	public void setScriptBaseDirectory(File scriptBaseFile) {
		this.scriptBaseDirectory = scriptBaseFile;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public void setExternalLibraryDirectory(File externalLibraryFilr) {
		this.externalLibraryDirectory = externalLibraryFilr;
	}

	public boolean print(String path, Map<String, String[]> param, OutputStream out)
			throws IOException {
		String encoding = this.encoding == null?"utf-8":this.encoding;
		if(path == null || path.length() == 0){
			out.write(buildDocumentHTML().getBytes(encoding));
			return true;
		}else if("export.action".equals(path)){
			out.write(buildExportHTML(param).getBytes(encoding));
		}
		boolean isPreload = false;
		if (path.endsWith(JSIText.PRELOAD_FILE_POSTFIX)) {
			isPreload = true;
			path = path.substring(0,path.length()-JSIText.PRELOAD_FILE_POSTFIX .length())+".js";
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
			out.write(JSIText.buildPreloadPerfix(path).getBytes());
			output(in, out);
			out.write(JSIText.buildPreloadPostfix("//").getBytes());
		} else {
			output(in, out);
		}
	}
	protected String buildDocumentHTML() {
		List<String> packageList = FileJSIRoot
				.findPackageList(this.scriptBaseDirectory);
		StringWriter out = new StringWriter();
		if (packageList.isEmpty()) {
			out.append("<html><body> 未发现任何托管脚本包，无法显示JSIDoc。<br /> ");
			out.append("请添加脚本包，并在包目录下正确添加相应的包定义文件 。");
			out
					.append("<a href='org/xidea/jsidoc/index.html?group={\"example\":[\"example\",\"example.internal\",\"example.dependence\",\"org.xidea.jsidoc.util\"]}'>");
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

	protected String buildExportHTML(Map<String, String[]> param) throws IOException {
		String[] contents = param.get("content");
		if (contents != null) {
			final DataJSIRoot root = new DataJSIRoot(contents[0]);
			JSIExportor exportor = DefaultJSIExportorFactory.getInstance()
					.createExplorter(param);
			if (exportor == null) {
				return null;
			}
			JSILoadContext context = new DefaultJSILoadContext();
			String[] exports = param.get("exports");
			if (exports != null) {
				// 只有Data Root 才能支持这种方式
				for (String item : exports) {
					root.$import(item, context);
				}
			}
			return exportor.export(context);
		} else {
			Map<String, String[]> testParams = new HashMap<String, String[]>();
			testParams.put("level", new String[] { String
					.valueOf(DefaultJSIExportorFactory.TYPE_EXPORT_CONFUSE) });
			return DefaultJSIExportorFactory.getInstance().createExplorter(
					testParams) == null ? null : "";
		}

	}

	protected boolean isIndex(String path) {
		return path.length() == 0 || path.equals("index.jsp")
				|| path.equals("index.php");
	}

	public InputStream getResourceStream(String path) {
		File file = new File(this.scriptBaseDirectory, path);
		if (file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		File[] list = this.scriptBaseDirectory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xml");
			}
		});
		if (list != null) {
			int i = list.length;
			while (i-- > 0) {
				InputStream in = findByXML(list[i], path);
				if (in != null) {
					return in;
				}
			}
		}
		if (this.externalLibraryDirectory != null) {
			list = this.externalLibraryDirectory
					.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							name = name.toLowerCase();
							return name.endsWith(".jar")
									|| name.endsWith(".zip");
						}
					});
			if (list != null) {
				int i = list.length;
				while (i-- > 0) {
					InputStream in = findByJAR(list[i], path);
					if (in != null) {
						return in;
					}
				}
			}
		}
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}

	protected InputStream findByJAR(File file, String path) {
		try {
			JarFile jarFile = new JarFile(file);
			ZipEntry ze = jarFile.getEntry(path.substring(1));
			if (ze != null) {
				return jarFile.getInputStream(ze);
			}
		} catch (IOException e) {
		}
		return null;
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

	private static void output(InputStream in, OutputStream out)
			throws IOException {
		byte[] buf = new byte[1024];
		int len = in.read(buf);
		while (len > 0) {
			out.write(buf, 0, len);
			len = in.read(buf);
		}
	}

}