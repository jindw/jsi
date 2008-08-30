package org.xidea.jsi.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSIExportorFactory;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.DataJSIRoot;
import org.xidea.jsi.impl.DefaultJSIExportorFactory;
import org.xidea.jsi.impl.DefaultJSILoadContext;
import org.xidea.jsi.impl.JSIUtil;

/**
 * 该类为方便调试开发，发布时可编译脚本，能后去掉此类。 Servlet 2.4 +
 * 
 * @author jindw
 */
public class JSIFilter implements Filter {
	protected String scriptBase;
	protected ServletContext context;
	/**
	 * 只有默认的encoding没有设置的时候，才会设置
	 */
	protected String encoding = null;
	private final static JSIExportorFactory exportorFactory = JSIUtil
			.getExportorFactory();

	public void destroy() {
	}

	public void doFilter(ServletRequest req, final ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		String path = request.getRequestURI().substring(
				request.getContextPath().length());
		if (path.startsWith(scriptBase)) {
			path = path.substring(scriptBase.length());
			if (this.processAttachedAction(request, (HttpServletResponse) resp,
					path)) {
				return;
			}
			if (isIndex(path)) {
				path = req.getParameter("path");
			}
			String resourcePath = getResourcePath(req, path);

			// 容错设计
			if (path.equals(resourcePath)) {
				resourcePath = null;
			}
			InputStream in = getResourceStream(resourcePath != null ? resourcePath
					: path);
			if (in != null) {
				// 经测试，metaType是不会自动设置的;
				// 对于静态文件的设置，我估计是提供静态文件服务的servlet内做的事情。

				// setContentType 和 setCharacterEncoding.在encoding上相互影响
				// response.getCharacterEncoding 默认是ISO-8895-1
				// request.getCharacterEncoding 默认是null
				initializeEncodingIfNotSet(request, resp);
				String metatype = context.getMimeType(path);
				if (metatype != null) {
					resp.setContentType(metatype);
				}
				ServletOutputStream out = resp.getOutputStream();
				processResourceStream(in, out, resourcePath);
				return;
			}

		}
		// 走这条分支的情况：1、无法找到资源，2、根本不在脚本目录下
		chain.doFilter(req, resp);
	}

	/**
	 * 响应附加行为
	 * 
	 * @param request
	 * @param
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public boolean processAttachedAction(HttpServletRequest request,
			HttpServletResponse response, String path) throws IOException {
		if ("jsidoc.action".equals(path) || isIndex(path)
				&& request.getParameter("path") == null) {
			initializeEncodingIfNotSet(request, response);
			String externalScript = request.getParameter("externalScript");
			if (externalScript == null) {
				printDocument(response);
			} else {
				response
						.sendRedirect("org/xidea/jsidoc/index.html?externalScript="
								+ URLEncoder.encode(externalScript, "utf-8"));

			}
			return true;
		} else if ("export.action".equals(path)) {
			initializeEncodingIfNotSet(request, response);

			if (null == DefaultJSIExportorFactory.class) {
				// 不支持导出方式
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			} else {
				String content = request.getParameter("content");
				JSIRoot root = this.creatJSIRootByXMLContent(content);
				String[] imports = root.loadText(null, "#export").split(
						"\\s*,\\s*");
				String type = root.loadText(null, "#type");
				String prefix = root.loadText(null, "#prefix");

				JSILoadContext context = new DefaultJSILoadContext();
				JSIExportor exportor;
				exportor = exportorFactory.createSimpleExplorter();
				exportor = exportorFactory.createReportExplorter();

				exportor = exportorFactory.createConfuseExplorter(prefix,
						"\r\n\r\n", false);// confuseUnimported
				exportor = exportorFactory.createConfuseExplorter(prefix,
						"\r\n\r\n", true);// confuseUnimported

				if (imports == null) {
					// 只有Data Root 才能支持这种方式
					String exports = root.loadText("", "export");
					if (exports != null) {
						imports = exports.split("[,\\s]+");
						for (String item : imports) {
							root.$import(item, context);
						}
					}
				} else {
					ArrayList<String> list = new ArrayList<String>();
					for (String param : imports) {
						String[] items = param.split("[,\\s]+");
						for (String item : items) {
							root.$import(item, context);
						}
					}
				}
				PrintWriter out = response.getWriter();
				String result = exportor.export(context);
				System.out.println(result);
				out.print(result);
			}
			return true;
		}
		return false;
	}

	private void initializeEncodingIfNotSet(ServletRequest request,
			ServletResponse response) throws UnsupportedEncodingException {
		if (request.getCharacterEncoding() == null) {
			// request 默认情况下是null
			String encoding = requireEncoding();
			request.setCharacterEncoding(encoding);
			response.setCharacterEncoding(encoding);
		}
	}

	private String requireEncoding() {
		return this.encoding == null ? "utf-8" : this.encoding;
	}

	protected JSIRoot creatJSIRootByXMLContent(String xmlContent) {
		return new DataJSIRoot(xmlContent);
	}

	private void printDocument(HttpServletResponse response) {
		try {
			PrintWriter out = response.getWriter();
			List<String> packageList = JSIUtil.findPackageList(new File(context
					.getRealPath(this.scriptBase)));

			if (packageList.isEmpty()) {
				out
						.print("<html><body> 未发现任何托管脚本包，无法显示JSIDoc。<br /> 请添加脚本包，并在包目录下正确添加相应的包定义文件 。</body><html>");
			} else {

				out
						.print("<html><frameset rows='100%'><frame src='org/xidea/jsidoc/index.html?");
				out.print(URLEncoder.encode("group.All", "utf-8"));
				out.print("=");
				boolean isFirst = true;
				for (String packageName : packageList) {
					if (isFirst) {
						isFirst = false;
					} else {
						out.print(",");
					}
					out.print(packageName);

				}
				out.print("'> </frameset></html>");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取资源路径
	 * 
	 * @param req
	 * @param path
	 * @return 原始资源路径
	 */
	public String getResourcePath(ServletRequest req, String path) {
		if (path.endsWith(JSIUtil.PRELOAD_FILE_POSTFIX)) {
			return path.replaceFirst(JSIUtil.PRELOAD_FILE_POSTFIX + "$", ".js");
		} else {
			return null;
		}
	}

	private boolean isIndex(String path) {
		return path.length() == 0 || path.equals("index.jsp")
				|| path.equals("index.php");
	}

	protected InputStream getResourceStream(String path) {
		InputStream in = context.getResourceAsStream(scriptBase + path);
		if (in == null) {
			return this.getClass().getClassLoader().getResourceAsStream(path);
		}
		if (in == null) {
			File dir = new File(context.getRealPath(scriptBase));
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

	protected void processResourceStream(InputStream in,
			ServletOutputStream out, String preloadPath) throws IOException {
		if (preloadPath != null) {
			out.print(JSIUtil.buildPreloadPerfix(preloadPath));
			output(in, out);
			out.print(JSIUtil.buildPreloadPostfix());
		} else {
			output(in, out);
		}
		in.close();
	}

	protected void output(InputStream in, ServletOutputStream out)
			throws IOException {
		byte[] buf = new byte[1024];
		int len = in.read(buf);
		while (len > 0) {
			out.write(buf, 0, len);
			len = in.read(buf);
		}
	}

	protected void outputPreload(ServletRequest req,
			final ServletResponse resp, final String uri)
			throws ServletException, IOException {
		final StringWriter bufSting = new StringWriter();
		final ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
		final Object[] holder = new Object[2];
		HttpServletResponseWrapper respw = new HttpServletResponseWrapper(
				(HttpServletResponse) resp) {
			@Override
			public PrintWriter getWriter() throws IOException {
				holder[1] = resp.getWriter();
				if (holder[0] == null) {
					holder[0] = new PrintWriter(bufSting);
				}
				return (PrintWriter) holder[0];
			}

			@Override
			public ServletOutputStream getOutputStream() throws IOException {
				holder[1] = resp.getOutputStream();
				if (holder[0] == null) {
					holder[0] = new ServletOutputStream() {
						@Override
						public void write(int b) throws IOException {
							bufStream.write(b);
						}
					};
				}
				return (ServletOutputStream) holder[0];
			}
		};
		final String uri2 = uri.replace(JSIUtil.PRELOAD_FILE_POSTFIX, ".js");
		RequestDispatcher disp = req.getRequestDispatcher(uri2);
		disp.include(req, respw);
		String preloadPerfix = JSIUtil.buildPreloadPerfix(uri2
				.substring(scriptBase.length()));
		if (holder[1] instanceof PrintWriter) {
			PrintWriter out = (PrintWriter) holder[1];
			out.write(preloadPerfix.toString());
			out.write(bufSting.toString());
			out.print(JSIUtil.buildPreloadPostfix());
			out.flush();
		} else {
			ServletOutputStream out = (ServletOutputStream) holder[1];
			out.print(preloadPerfix.toString());
			bufStream.writeTo(out);
			out.print(JSIUtil.buildPreloadPostfix());
			out.flush();
		}
	}

	public void init(FilterConfig config) throws ServletException {
		this.context = config.getServletContext();
		String scriptBase = config.getInitParameter("scriptBase");
		String encoding = config.getInitParameter("encoding");
		if (encoding != null) {
			this.encoding = encoding;
		}
		if (scriptBase != null && (scriptBase = scriptBase.trim()).length() > 0) {
			if (!scriptBase.endsWith("/")) {
				scriptBase = scriptBase + '/';
			}
		} else {
			scriptBase = "/scripts/";
		}
		this.scriptBase = scriptBase;
	}
}
