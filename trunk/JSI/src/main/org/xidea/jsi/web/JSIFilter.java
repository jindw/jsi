package org.xidea.jsi.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 该类为方便调试开发，发布时可编译脚本，能后去掉此类。 Servlet 2.4 +
 * 
 * @author jindw
 */
public class JSIFilter extends JSIService implements Filter, Servlet {
	@SuppressWarnings("unused")
	private Log log = LogFactory.getLog(JSIFilter.class);
	protected ServletContext context;
	protected ServletConfig config;
	protected String scriptBase = "/scripts/";

	public void service(ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {
		if (!service((HttpServletRequest)req, (HttpServletResponse)resp)) {
			// 走这条分支的情况：1、无法找到资源，2、根本不在脚本目录下
			HttpServletResponse response = (HttpServletResponse) resp;
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "找不到指定的资源");
		}
	}

	public void doFilter(ServletRequest req, final ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		if (!service((HttpServletRequest)req, (HttpServletResponse)resp)) {
			// 走这条分支的情况：1、无法找到资源，2、根本不在脚本目录下
			chain.doFilter(req, resp);
		}
	}

	/**
	 * 处理指定资源，如果该资源存在，返回真
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public boolean service(HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		@SuppressWarnings("unchecked")
		Map<String,String[]> params = req.getParameterMap();
		String path = getScriptPath(req);
		String service = null;
		if (path == null || path.length() == 0) {
			String[] services = params.get("service");
			if(services.length>0){
				service = services[0];
			}else{
				service = "";
			}
		}else if (path.startsWith("export/")) {
			service = path;
		}
		if(service != null ){
			ByteArrayOutputStream out2 = new ByteArrayOutputStream();
			try{
				//header(': ');
				resp.setHeader("Content-Disposition", "attachment; filename='data.zip'");
				processAction(service, params,req.getHeader("Cookie"), out2);
			}catch (FileNotFoundException e) {
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			out2.writeTo(resp.getOutputStream());
		} else {
			initializeEncodingIfNotSet(req, resp, null);
			String metatype = context.getMimeType(path);
			if (metatype != null) {
				resp.setContentType(metatype);
			}
			return this.writeResource(path, resp.getOutputStream());
		}
		return true;
	}

	protected String getScriptPath(HttpServletRequest request) {
		String path = request.getRequestURI().substring(
				request.getContextPath().length());
		if (path.startsWith(this.scriptBase)) {
			return path.substring(this.scriptBase.length());
		} else {
			return null;
		}
	}
	/*
	 * 经测试，metaType是不会自动设置的; 对于静态文件的设置，我估计是提供静态文件服务的servlet内做的事情。 setContentType
	 * 和 setCharacterEncoding.在encoding上相互影响 response.getCharacterEncoding
	 * 默认是ISO-8895-1 request.getCharacterEncoding 默认是null
	 */
	protected void initializeEncodingIfNotSet(ServletRequest request,
			ServletResponse response,String encoding) throws UnsupportedEncodingException {
		if (encoding != null || request.getCharacterEncoding() == null) {
			// request 默认情况下是null
			if(encoding == null){
				encoding = this.getEncoding();
			}
			request.setCharacterEncoding(encoding);
			response.setCharacterEncoding(encoding);
		}
	}

	/**
	 * 打开的流使用完成后需要自己关掉
	 */
	@Override
	public URL getResource(String path) {
		URL in = null;
		try {
			in = context.getResource(scriptBase + path);
		} catch (IOException e) {
		}
		if (in == null) {
			in = super.getResource(path);
		}
		return in;
	}

	public void init(FilterConfig config) throws ServletException {
		this.context = config.getServletContext();
		String scriptBase = config.getInitParameter("scriptBase");
		String encoding = config.getInitParameter("encoding");
		init(scriptBase, encoding);
	}

	public void init(ServletConfig config) throws ServletException {
		this.config = config;
		this.context = config.getServletContext();
		String scriptBase = config.getInitParameter("scriptBase");
		String encoding = config.getInitParameter("encoding");
		init(scriptBase, encoding);
	}

	protected void init(String scriptBase, String encoding) {
		if (encoding != null) {
			this.setEncoding(encoding);
		}
		if (scriptBase != null && (scriptBase = scriptBase.trim()).length() > 0) {
			if (!scriptBase.endsWith("/")) {
				scriptBase += '/';
			}
			this.scriptBase = scriptBase;
		}
		this.reset();
		String file = context.getRealPath(this.scriptBase);
		this.addBase(new File(file));
		this.addLib(new File(context.getRealPath(this.scriptBase)));
	}

	public ServletConfig getServletConfig() {
		return this.config;
	}

	public String getServletInfo() {
		return config.getServletName();
	}

	public void destroy() {
	}

}
