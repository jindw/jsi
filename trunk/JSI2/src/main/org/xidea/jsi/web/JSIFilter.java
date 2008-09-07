package org.xidea.jsi.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xidea.jsi.JSIExportor;
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
public class JSIFilter extends JSIService implements Filter {
	protected ServletContext context;
	public void destroy() {
	}

	public void doFilter(ServletRequest req, final ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getRequestURI().substring(
				request.getContextPath().length());
		if (path.startsWith(scriptBase)) {
			path = path.substring(scriptBase.length());
			if (this.processAttachedAction(request, response, path)) {
				initializeEncodingIfNotSet(request, response);
				return;
			}
			if (isIndex(path)) {
				path = req.getParameter("path");
			}
			boolean isPreload = false;
			if (path.endsWith(JSIUtil.PRELOAD_FILE_POSTFIX)) {
				isPreload = true;
				path = path.replaceFirst(JSIUtil.PRELOAD_FILE_POSTFIX + "$",
						".js");
			}
			InputStream in = getResourceStream(path);
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
				printResource(path, isPreload, in, out);
				in.close();
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
		if (isIndex(path) && request.getParameter("path") == null) {
			String externalScript = request.getParameter("externalScript");
			if (externalScript == null) {
				response.getWriter().print(document());
			} else {
				response
						.sendRedirect("org/xidea/jsidoc/index.html?externalScript="
								+ URLEncoder.encode(externalScript, "utf-8"));

			}
			return true;
		} else if ("export.action".equals(path)) {
			String content = request.getParameter("content");
			String result = export(content);
			if(result == null){
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}else{
				response.addHeader("Content-Type", "text/paint;charset="+(this.encoding == null ? "utf-8" : this.encoding));
				response.getWriter().print(result);
			}
			return true;
		}
		return false;
	}
	private void initializeEncodingIfNotSet(ServletRequest request,
			ServletResponse response) throws UnsupportedEncodingException {
		if (request.getCharacterEncoding() == null) {
			// request 默认情况下是null
			String encoding = this.encoding == null ? "utf-8" : this.encoding;
			request.setCharacterEncoding(encoding);
			response.setCharacterEncoding(encoding);
		}
	}

	public InputStream getResourceStream(String path) {
		InputStream in = context.getResourceAsStream(scriptBase + path);
		if (in == null) {
			in = super.getResourceStream(path);
		}
		return in;
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
		this.absoluteScriptBase = context.getRealPath(this.scriptBase);
	}
}
