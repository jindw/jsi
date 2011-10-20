package org.jside.webserver.handler;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jside.webserver.RequestContext;
import org.jside.webserver.RequestUtil;
import org.jside.webserver.servlet.ServletContextImpl;
import org.xidea.lite.TemplateEngine;
import org.xidea.lite.servlet.TemplateServlet;
import org.xidea.lite.tools.ResourceManager;

class TemplateServletImpl extends TemplateServlet {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(TemplateServletImpl.class);
	private File root;
	private ResourceManager manager;
	private ServletContextImpl servletImpl = new ServletContextImpl();

	public TemplateServletImpl(ResourceManager manager,TemplateEngine templateEngine)
			throws ServletException {
		this.manager = manager;
		this.root = new File(manager.getRoot());
		this.templateEngine = templateEngine;
		init(servletImpl);
	}

	public void initEngine(final ServletConfig config) {
	}
	public ResourceManager getResourceManager(){
		return manager;
	}

	public void service(RequestContext context) throws IOException,
			ServletException {
		String path = servletImpl.getServletPath();
		Map<String, Object> data = TemplateUtil.loadData(root, path);
		for (String key : data.keySet()) {
			servletImpl.setAttribute(key, data.get(key));
		}
		try{
			this.service(path, servletImpl, servletImpl);
		}catch (Exception e) {
			RequestUtil.printResource(e, "text/html;charset=utf-8");
		}
	}

	public TemplateEngine getTemplateEngine() {
		return templateEngine;
	}

}
