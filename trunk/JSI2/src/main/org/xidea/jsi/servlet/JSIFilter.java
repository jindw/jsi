package org.xidea.jsi.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
import org.xidea.jsi.impl.AbstractJSIRoot;
import org.xidea.jsi.impl.DataJSIRoot;
import org.xidea.jsi.impl.DefaultJSIExportorFactory;
import org.xidea.jsi.impl.DefaultJSILoadContext;
import org.xidea.jsi.impl.FileJSIRoot;
import org.xidea.jsi.impl.JSIUtil;

/**
 * 该类为方便调试开发，发布时可编译脚本，能后去掉此类。 Servlet 2.4 +
 * 
 * @author jindw
 */
public class JSIFilter implements Filter {
	public static final String GLOBAL_JSI_ROOT_KEY = JSIFilter.class.getName()
			.concat(".GLOBAL_JSIROOT_KEY");

	private static final String UTF8_INCODING = "utf-8";
	/**
	 * 直接合并
	 * 
	 * @deprecated
	 */
	private static final int JOIN_DIRECT = 0;
	/**
	 * 合并内部冲突
	 * 
	 * @deprecated
	 */
	private static final int JOIN_WITHOUT_INNER_CONFLICTION = 1;
	/**
	 * 合并全部冲突
	 * 
	 * @deprecated
	 */
	private static final int JOIN_WITHOUT_ALL_CONFLICTION = 2;

	protected String scriptBase;
	protected ServletContext context;
	/**
	 * 只有默认的encoding没有设置的时候，才会设置
	 */
	protected String encoding = null;
	protected String exportorFactoryClass = JSIUtil.JSI_EXPORTOR_FACTORY_CLASS;
	private JSIRoot jsiRoot;
	private static JSIExportorFactory exportorFactory;

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
								+ URLEncoder.encode(externalScript,
										UTF8_INCODING));

			}
			return true;
		} else if ("export.action".equals(path)) {
			initializeEncodingIfNotSet(request, response);
			int level = 0;
			{
				String levelParam = request.getParameter("level");
				if (levelParam != null) {
					try {
						level = Integer.parseInt(levelParam);
					} catch (Exception e) {
					}
				}
			}
			JSIExportorFactory factory = getExportorFactory();
			if (level != 0
					&& factory.getClass() == DefaultJSIExportorFactory.class) {
				// 不支持导出方式
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			} else {
				JSILoadContext context = new DefaultJSILoadContext();
				String content = request.getParameter("content");
				String[] imports = request.getParameterValues("imports");
				JSIRoot root;
				JSIExportor exportor;
				if (content != null) {
					root = this.creatJSIRootByXMLContent(content);
				} else {
					root = this.jsiRoot;
				}
				if (level == 0) {
					exportor = factory.createSimpleExplorter();
				} else {
					String prefix = request.getParameter("prefix");
					exportor = factory.createExplorter(prefix, 0, "\r\n\r\n",
							level == 1);
				}
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
				out.print(exportor.export(context));
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
		return this.encoding == null ? UTF8_INCODING : this.encoding;
	}

	protected JSIRoot creatJSIRootByXMLContent(String xmlContent) {
		return new DataJSIRoot(xmlContent);
	}

	protected JSIExportorFactory getExportorFactory() {
		if (exportorFactory == null) {
			try {
				exportorFactory = (JSIExportorFactory) Class.forName(
						exportorFactoryClass).newInstance();
			} catch (Exception e) {
				exportorFactory = JSIUtil.getExportorFactory();
			}
		}
		return exportorFactory;
	}

	private void printDocument(HttpServletResponse response) {
		try {
			PrintWriter out = response.getWriter();
			List<String> packageList = getPackageList();

			if (packageList.isEmpty()) {
				out
						.print("<html><body> 未发现任何托管脚本包，无法显示JSIDoc。<br /> 请添加脚本包，并在包目录下正确添加相应的包定义文件 。</body><html>");
			} else {

				out
						.print("<html><frameset rows='100%'><frame src='org/xidea/jsidoc/index.html?");
				out.print(URLEncoder.encode("group.全部托管类库", UTF8_INCODING));
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

	protected List<String> getPackageList() {
		final File dir = new File(context.getRealPath(scriptBase));
		final List<String> result = FileJSIRoot.findPackageList(dir);
		return result;
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
		} else if (isIndex(path)) {
			return req.getParameter("path");
		} else {
			return null;
		}
	}

	private boolean isIndex(String path) {
		return path.length() == 0 || path.equals("index.jsp")
				|| path.equals("index.php");
	}

	protected InputStream getResourceStream(String path) {
		return context.getResourceAsStream(scriptBase + path);
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
		String exportorFactoryClass = config
				.getInitParameter("exportorFactoryClass");
		if (encoding != null) {
			this.encoding = encoding;
		}
		if (exportorFactoryClass != null) {
			this.exportorFactoryClass = exportorFactoryClass;
		}

		if (scriptBase != null && (scriptBase = scriptBase.trim()).length() > 0) {
			if (!scriptBase.endsWith("/")) {
				scriptBase = scriptBase + '/';
			}
		} else {
			scriptBase = "/scripts/";
		}
		// this.contextPath = config.getServletContext().getContextPath();
		// this.contextLength = this.contextPath.length();
		this.scriptBase = scriptBase;
		this.jsiRoot = new JSIRootImpl();
		config.getServletContext().setAttribute(GLOBAL_JSI_ROOT_KEY,
				this.jsiRoot);
	}

	private class JSIRootImpl extends AbstractJSIRoot {
		@Override
		public String loadText(String pkgName, String scriptName) {
			try {
				InputStream in = getResourceStream('/'
						+ pkgName.replace('.', '/') + '/' + scriptName);
				if (in != null) {
					Reader reader = new InputStreamReader(in,
							encoding == null ? UTF8_INCODING : encoding);

					StringBuilder buf = new StringBuilder();
					char[] cbuf = new char[1024];
					for (int len = reader.read(cbuf); len > 0; len = reader
							.read(cbuf)) {
						buf.append(cbuf, 0, len);
					}

					return buf.toString();
				} else {
					return null;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
