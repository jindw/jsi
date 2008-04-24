package org.xidea.jsi.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
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

import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.impl.DefaultJSIPackage;
import org.xidea.jsi.impl.FileJSIRoot;

public class PreloadFilter implements Filter {

	protected String scriptBase;

	protected ServletContext context;
	protected String contentType = "text/html;charset=utf-8";

	public static final String JS_FILE_POSTFIX = ".js";
	public static final String PRELOAD_FILE_POSTFIX = "__preload__.js";
	public static final String PRELOAD_PREFIX = "$JSI.preload(";
	public static final String PRELOAD_CONTENT_PREFIX = "function(){eval(this.varText);";
	public static final String PRELOAD_CONTENT_POSTFIX = "\n}";
	public static final String PRELOAD_POSTFIX = ")";

	public void destroy() {
	}

	public void doFilter(ServletRequest req, final ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		String path = request.getRequestURI().substring(
				request.getContextPath().length());
		if (path.startsWith(scriptBase)) {
			path = path.substring(scriptBase.length());
			if (this.processAttachedAction(request,(HttpServletResponse) resp, path)) {
				return;
			}
			String resourcePath = getResourcePath(req, path);
			InputStream in = getResourceStream(resourcePath);
			if (in != null) {
				//容错设计
				if(resourcePath.equals(path)){
					resourcePath = null;
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
	 * @param request 
	 * @param 
	 * @param path
	 * @return
	 */
	public boolean processAttachedAction(HttpServletRequest request, HttpServletResponse response, String path) {
		if(path.endsWith("/")){
			path = path.substring(0,path.length()-1);
		}
		if("jsidoc".equals(path)){
			
			return true;
		}
		return false;
	}

	protected List<String> getPackageList(){
		try {
			final File dir = new File(context.getResource(scriptBase).getFile());
			final List<String> result = FileJSIRoot.findPackageList(dir);
			return result;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}



	/**
	 * 获取资源路径
	 * @param req
	 * @param path
	 * @return 原始资源路径
	 */
	public String getResourcePath(ServletRequest req, String path) {
		if (path.endsWith(PRELOAD_FILE_POSTFIX)) {
			return path.replaceFirst(PRELOAD_FILE_POSTFIX + "$", ".js");
		} else if (path.length() == 0 || path.equals("index.jsp")) {
			return req.getParameter("path");
		} else {
			return null;
		}
	}

	protected InputStream getResourceStream(String path) {
		InputStream in = context.getResourceAsStream(scriptBase + path);
		return in;
	}

	protected void processResourceStream(InputStream in,
			ServletOutputStream out, String preloadPath) throws IOException {
		if (preloadPath != null) {
			out.print(this.buildPreloadPerfix(preloadPath));
			output(in, out);
			out.print(PRELOAD_CONTENT_POSTFIX);
			out.print(PRELOAD_POSTFIX);
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
		final String uri2 = uri.replace(PRELOAD_FILE_POSTFIX, ".js");
		RequestDispatcher disp = req.getRequestDispatcher(uri2);
		disp.include(req, respw);
		String preloadPerfix = buildPreloadPerfix(uri2.substring(scriptBase
				.length()));
		if (holder[1] instanceof PrintWriter) {
			PrintWriter out = (PrintWriter) holder[1];
			out.write(preloadPerfix.toString());
			out.write(bufSting.toString());
			out.write(PRELOAD_CONTENT_POSTFIX);
			out.print(PRELOAD_POSTFIX);
			out.flush();
		} else {
			ServletOutputStream out = (ServletOutputStream) holder[1];
			out.print(preloadPerfix.toString());
			bufStream.writeTo(out);
			out.print(PRELOAD_CONTENT_POSTFIX);
			out.print(PRELOAD_POSTFIX);
			out.flush();
		}
	}

	protected String buildPreloadPerfix(String path) {
		String pkg = path.substring(0, path.lastIndexOf('/')).replace('/', '.');
		String file = path.substring(pkg.length() + 1);
		StringBuffer buf = new StringBuffer();
		buf.append(PRELOAD_PREFIX);
		buf.append("'" + pkg + "',");
		buf.append("'" + file + "',");
		buf.append(PRELOAD_CONTENT_PREFIX);
		return buf.toString();
	}

	public void init(FilterConfig config) throws ServletException {
		this.context = config.getServletContext();
		String scriptBase = config.getInitParameter("scriptBase");
		String contentType = config.getInitParameter("contentType");

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
