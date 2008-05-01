package org.xidea.jsi.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.DataJSIRoot;
import org.xidea.jsi.impl.DefaultJSIExportorFactory;
import org.xidea.jsi.impl.DefaultJSILoadContext;
import org.xidea.jsi.impl.DefaultJSIPackage;
import org.xidea.jsi.impl.FileJSIRoot;
import org.xidea.jsi.impl.JSIUtil;

/**
 * 该类为方便调试开发，发布时可编译脚本，能后去掉此类。
 * 
 * @author jindw
 */
public class JSIFilter implements Filter {

	/**
	 * 合并成JSIDoc
	 * 
	 * @deprecated
	 */
	private static final int JOIN_AS_JSIDOC = -2;
	/**
	 * 合并成XML
	 * 
	 * @deprecated
	 */
	private static final int JOIN_AS_XML = -1;
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
	protected String encoding = null;
	protected String contentType = null;// "text/html;charset=utf-8";
	protected String exportorFactoryClass = JSIUtil.JSI_EXPORTOR_FACTORY_CLASS;
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
				if (!path.toLowerCase().endsWith(".js")) {
					resp.setContentType(context.getMimeType(path));
				}
				ServletOutputStream out = resp.getOutputStream();
				processResourceStream(in, out, resourcePath);
				return;
			}

		}
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
			// if(request.getCharacterEncoding() == null){
			request.setCharacterEncoding("utf-8");
			// }
			JSIExportorFactory factory = getExportorFactory();
			if (factory == null) {
				// 不支持
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			} else {
				String level = request.getParameter("level");
				String prefix = request.getParameter("prefix");
				String content = request.getParameter("content");
				JSILoadContext context = new DefaultJSILoadContext();
				if (content != null) {
					JSIRoot root = this.creatJSIRoot(content);
					// TODO:只有Data Root 才能支持这种方式
					String exports = root.loadText("", "export");
					String[] imports = exports.split("[,\\s]+");
					for (String item : imports) {
						root.$import(item, context);
					}
					boolean preservedUnimported = "1".equals(level);

					JSIExportor exportor = factory.createExplorter(prefix, 0,
							"\r\n\r\n", preservedUnimported);
					PrintWriter out = response.getWriter();
					out.print(exportor.export(context));
				}
			}
			return true;
		}
		return false;
	}

	protected JSIRoot creatJSIRoot(String xmlContent) {
		return new DataJSIRoot(xmlContent);
	}

	protected JSIExportorFactory getExportorFactory() {
		if (exportorFactory == null) {
			try {
				JSIUtil.getExportorFactory();
				exportorFactory = (JSIExportorFactory) Class.forName(
						exportorFactoryClass).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return exportorFactory;
	}

	private void printDocument(HttpServletResponse response) {
		try {
			PrintWriter out = response.getWriter();
			List<String> packageList = getPackageList();

			out
					.print("<html><frameset rows='100%'><frame src='org/xidea/jsidoc/index.html?");
			out.print(URLEncoder.encode("group.全部托管类库", "utf-8"));
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
		InputStream in = context.getResourceAsStream(scriptBase + path);
		return in;
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
		String contentType = config.getInitParameter("contentType");
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
		if (contentType != null) {
			this.contentType = contentType;
		}
		// this.contextPath = config.getServletContext().getContextPath();
		// this.contextLength = this.contextPath.length();
		this.scriptBase = scriptBase;
	}

}
