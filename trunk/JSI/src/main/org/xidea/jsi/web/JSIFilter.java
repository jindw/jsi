package org.xidea.jsi.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

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

import org.xidea.jsi.impl.JSIText;

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
				return;
			}
			if (isIndex(path)) {
				path = req.getParameter("path");
			}
			boolean isPreload = false;
			if (path.endsWith(JSIText.PRELOAD_FILE_POSTFIX)) {
				isPreload = true;
				path = path.substring(0,path.length()-JSIText.PRELOAD_FILE_POSTFIX .length())+".js";

			}
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
			writeResource(path, isPreload, out);

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
	private boolean processAttachedAction(HttpServletRequest request,
			HttpServletResponse response, String path) throws IOException {
		if (isIndex(path) && request.getParameter("path") == null) {
			initializeEncodingIfNotSet(request, response);
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
			//type =1,type=2,type=3
			initializeEncodingIfNotSet(request, response);
			@SuppressWarnings("unchecked")
			Map param = request.getParameterMap();
			@SuppressWarnings("unchecked")
			String result = export(param);
			if(result == null){
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}else{
				response.addHeader("Content-Type", "text/plain;charset="+this.encoding);
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
			request.setCharacterEncoding(encoding);
			response.setCharacterEncoding(encoding);
		}
	}

	/**
	 * 打开的流使用完成后需要自己关掉
	 */
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
		this.scriptBaseDirectory = new File(context.getRealPath(this.scriptBase));
		this.scriptBaseDirectory = new File(context.getRealPath(this.scriptBase));
	}
}
