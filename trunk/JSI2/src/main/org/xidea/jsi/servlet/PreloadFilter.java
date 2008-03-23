package org.xidea.jsi.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class PreloadFilter implements Filter {

	protected String scriptBase ;

	//protected String contextPath;

	//protected int contextLength;
	
	public static final String PRELOAD_FILE_POSTFIX = "__preload__.js";
	public static final String PRELOAD_CONTENT__POSTFIX = "\n})";

	public void destroy() {
	}

	public void doFilter(ServletRequest req, final ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		final String uri = request.getRequestURI().substring(request.getContextPath().length());
		if (uri.startsWith(scriptBase)) {
			if (uri.endsWith(PRELOAD_FILE_POSTFIX)) {
				outputPreload(req, resp, uri);
				return;
			}
		}
		chain.doFilter(req, resp);

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

	protected void outputPreload(ServletRequest req, final ServletResponse resp,
			final String uri) throws ServletException, IOException {
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
			out.write(PRELOAD_CONTENT__POSTFIX);
			out.flush();
		} else {
			ServletOutputStream out = (ServletOutputStream) holder[1];
			out.print(preloadPerfix.toString());
			bufStream.writeTo(out);
			out.print(PRELOAD_CONTENT__POSTFIX);
			out.flush();
		}
	}

	protected String buildPreloadPerfix(String path) {
		String pkg = path.substring(0, path.lastIndexOf('/')).replace('/', '.');
		String file = path.substring(pkg.length() + 1);
		StringBuffer buf = new StringBuffer();
		buf.append("$JSI.cacheScript(");
		buf.append("'" + pkg + "',");
		buf.append("'" + file + "',");
		buf.append("function(){");
		if (!"__package__.js".equals(file)) {
			buf.append("eval(this.varText);");
		}
		return buf.toString();
	}

	public void init(FilterConfig config) throws ServletException {
		String scriptBase = config.getInitParameter("scriptBase");

		if (scriptBase != null && (scriptBase = scriptBase.trim()).length() > 0) {
			if (!scriptBase.endsWith("/")) {
				scriptBase = scriptBase +'/';
			}
		}else{
			scriptBase = "/scripts/";
		}
		//this.contextPath = config.getServletContext().getContextPath();
		//this.contextLength = this.contextPath.length();
		this.scriptBase =  scriptBase;
	}

}
