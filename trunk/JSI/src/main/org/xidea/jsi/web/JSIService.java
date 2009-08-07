package org.xidea.jsi.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.ScriptNotFoundException;
import org.xidea.jsi.impl.DataRoot;
import org.xidea.jsi.impl.DefaultExportorFactory;
import org.xidea.jsi.impl.DefaultLoadContext;
import org.xidea.jsi.impl.ResourceRoot;
import org.xidea.jsi.impl.JSIText;

public class JSIService extends ResourceRoot {
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(JSIService.class);
	protected String exportService = "http://litecompiler.appspot.com/scripts/export.action";
	protected Map<String, String> cachedMap;// = new WeakHashMap<String,
	protected SDNService sdn = new SDNService(this);

	protected static Collection<String> imgages = Arrays.asList("png", "gif",
			"jpeg", "jpg");

	public String getContentType(String path, Map<String, String[]> params,
			String defaultContentType) {
		String contentType = defaultContentType;
		int p = path.lastIndexOf('.');
		if (p > 0) {
			String ext = path.substring(p + 1).toLowerCase();
			if (imgages.contains(ext)) {
				return "image/" + ext;
			}
		}
		if (path.endsWith("/data.action")) {
			String data = params.get("data")[0];
			int dataContentEnd = data.indexOf(',');
			return "" + data.substring(dataContentEnd);
		}
		if (path.endsWith("export.action")) {
			contentType = "text/plain";
		} else if (path.endsWith(".css")) {
			contentType = "text/css";
		} else if (path.endsWith(".js")) {// for debug
			contentType = "text/plain";
		}
		if (contentType != null) {
			contentType = contentType + ";charset="
					+ this.getEncoding();
		}
		return contentType;
	}

	public void service(String path, Map<String, String[]> params,
			OutputStream out) throws IOException {
		if (path == null || path.length() == 0) {
			out.write(document().getBytes(this.getEncoding()));
		} else if ("export.action".equals(path)) {
			String result = export(params);
			out.write(result.getBytes(this.getEncoding()));
		} else if (path.startsWith("=")) {
			path = path.substring(1);
			if (path.length() == 0) {
				throw new ScriptNotFoundException("");
			}
			writeSDNRelease(path, out);
			// "text/plain";
		} else if (path.endsWith("data.action")) {
			String data = params.get("data")[0];
			int dataContentEnd = data.indexOf(',');
			this.writeBase64(data.substring(dataContentEnd + 1), out);
		} else {
			boolean isPreload = false;
			if (path.endsWith(JSIText.PRELOAD_FILE_POSTFIX)) {
				isPreload = true;
				path = path.substring(0, path.length()
						- JSIText.PRELOAD_FILE_POSTFIX.length())
						+ ".js";
			}
			this.writeResource(path, isPreload, out);
		}
		out.flush();
	}

	/**
	 * ABCDEFGHIJKLMNOPQRSTUVWXYZ//65 abcdefghijklmnopqrstuvwxyz//97
	 * 0123456789+/=
	 * 
	 * @param data
	 * @param out
	 * @throws IOException
	 */
	public void writeBase64(String data, OutputStream out) throws IOException {
		char[] cs = data.toCharArray();
		int previousByte = 0;
		for (int i = 0, k = -1; i < cs.length; i++) {
			int currentByte = cs[i];
			switch (currentByte) {
			case '+':
				currentByte = 62;
				break;
			case '/':
				currentByte = 63;
				break;
			case '=':
				return;
			default:
				if (Character.isLetterOrDigit(currentByte)) {
					if (currentByte >= 97) {// a
						currentByte -= 71;// + 26 - 97;
					} else if (currentByte >= 65) {// A
						currentByte -= 65;
					} else {// if (currentByte >= 48) {// 0
						currentByte += 4;// + 52 - 48;
					}
				} else {
					continue;
				}
			}
			switch (++k & 3) {// 00,01,10,11
			case 0:
				break;
			case 1:
				out.write((previousByte << 2) | (currentByte >>> 4));// 6+2
				break;
			case 2:// 32,16,8,4,2,1,
				out.write((previousByte & 63) << 4 | (currentByte >>> 2));// 4+4
				break;
			case 3:
				out.write((previousByte & 3) << 6 | (currentByte));// 2+6
			}
			previousByte = currentByte;
		}

	}

	public void writeSDNRelease(String path, OutputStream out)
			throws IOException {
		String result = null;
		//每次都清理一下吧，CPU足够强悍，反正这只是一个调试程序
		this.packageMap.clear();
		if (cachedMap != null) {
			result = cachedMap.get(path);
		}
		if (result == null) {
			result = sdn.doReleaseExport(path);
			if (cachedMap != null) {
				cachedMap.put(path, result);
			}
		}
		out.write(result.getBytes(this.getEncoding()));
	}

	public void writeSDNDebug(String path, OutputStream out) throws IOException {
		out.write(sdn.doDebugExport(path).getBytes(this.getEncoding()));
	}

	protected boolean writeResource(String path, boolean isPreload, Writer out)
			throws IOException {
		return this.output(path, out, isPreload);
	}

	protected boolean writeResource(String path, boolean isPreload,
			OutputStream out) throws IOException {
		return this.output(path, out, isPreload);
	}

	protected String document() {
		List<String> packageList = this.getPackageList(false);
		StringWriter out = new StringWriter();
		if (packageList.isEmpty()) {
			out.append("<html><head>");
			out
					.append("<meta http-equiv='Content-Type' content='text/html;utf-8'/>");
			out.append("</head>");
			out.append("<body> 未发现任何托管脚本包，无法显示JSIDoc。<br /> ");
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

	protected String export(Map<String, String[]> param) throws IOException {
		String[] contents = param.get("content");
		if (contents != null) {
			final DataRoot root = new DataRoot(contents[0]);
			JSIExportor exportor = DefaultExportorFactory.getInstance()
					.createExplorter(param);
			if (exportor == null) {
				if (!param.containsKey(exportService)) {
					HttpURLConnection url = (HttpURLConnection) new URL(
							exportService).openConnection();
					url.setRequestMethod("POST");
					url.setDoOutput(true);
					url.setRequestProperty("Content-Length", "");
					StringBuilder buf = new StringBuilder();
					buf
							.append(URLEncoder.encode(exportService, "UTF-8")
									+ "=1");
					for (String key : param.keySet()) {
						String[] values = param.get(key);
						for (String value : values) {
							buf.append('&');
							buf.append(URLEncoder.encode(key, "UTF-8"));
							buf.append('=');
							buf.append(URLEncoder.encode(value, "UTF-8"));
						}
					}
					url.getOutputStream().write(
							buf.toString().getBytes("UTF-8"));
					return org.xidea.jsi.impl.JSIText.loadText(url
							.getInputStream(), "UTF-8");
				} else {
					return null;
				}
			}
			JSILoadContext context = new DefaultLoadContext();
			String[] exports = param.get("exports");
			if (exports != null) {
				// 只有Data Root 才能支持这种方式
				for (String item : exports) {
					// PHP 不支持同名参数
					for (String subitem : item.split("[^\\w\\$\\:\\.\\-\\*]+")) {
						root.$import(subitem, context);
					}
				}
			}
			return exportor.export(context);
		} else {
			Map<String, String[]> testParams = new HashMap<String, String[]>();
			testParams.put("level", new String[] { String
					.valueOf(DefaultExportorFactory.TYPE_EXPORT_CONFUSE) });
			return DefaultExportorFactory.getInstance().createExplorter(
					testParams) == null ? null : "";
		}

	}

	protected boolean isIndex(String path) {
		return path.length() == 0 || path.equals("index.jsp")
				|| path.equals("index.php");
	}

}