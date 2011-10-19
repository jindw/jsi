package org.jside.webserver.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jside.webserver.RequestContext;
import org.jside.webserver.RequestUtil;
import org.jside.webserver.servlet.ServletContextImpl;
import org.xidea.lite.LiteTemplate;
import org.xidea.lite.impl.HotLiteEngine;
import org.xidea.lite.parse.ParseConfig;
import org.xidea.lite.servlet.TemplateServlet;
import org.xidea.lite.tools.LiteCompiler;
import org.xidea.lite.tools.ResourceManagerImpl;

public class TemplateServletImpl extends TemplateServlet {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(TemplateServletImpl.class);
	private File root;
	private ResourceManagerImpl manager;
	private ServletContextImpl servletImpl = new ServletContextImpl();
	private URI dirTemplate ;
	private long dirTemplateModified = 0;

	public TemplateServletImpl(ResourceManagerImpl manager) throws ServletException {
		this.manager = manager;
		this.root = new File(manager.getRoot());
		this.templateEngine =  new HotLiteEngine((ParseConfig) manager, null);
		init(servletImpl);
		URL url = this.getClass().getResource("dir.xhtml");
		try {
			dirTemplate = url.toURI();
			if("file".equals(dirTemplate.getScheme())){
				dirTemplateModified  = new File(dirTemplate).lastModified();
			}
		} catch (URISyntaxException e) {
			log.error("目录模板位置不支持", e);
			throw new RuntimeException(e);
		}
	}

	public void initEngine(final ServletConfig config) {
	}

	void compileLite(RequestContext context)
			throws FileNotFoundException, IOException,
			UnsupportedEncodingException {
		String path = context.getParam().get("path");
		String litecode = ((HotLiteEngine) templateEngine)
				.getLitecode(path);
		String phpcode = LiteCompiler.buildPHP(path, litecode);
		String litecodepath = "/WEB-INF/litecode/" + path.replace('/', '^');
		Util
				.writeFile(new File(root, litecodepath), litecode
						.getBytes("UTF-8"));
		File php = new File(root, litecodepath + ".php");
		Util.writeFile(php, phpcode
				.getBytes(manager.getFeatureMap(path).get(
						LiteTemplate.FEATURE_ENCODING)));
		RequestUtil.printResource("{\"php\":\""+php.toURI()+"\"}", "text/javascript;charset=utf-8");
	}

	public void service(RequestContext context) throws IOException,
			ServletException {
		final String uri = context.getRequestURI();
		File f = new File(root, uri);
		String path = null;
		if (f.isDirectory()) {
			if(!uri.endsWith("/")){
				RequestUtil.sendRedirect(uri+'/');
				return;
			}
			path = dirTemplate.toString();
			if(dirTemplateModified!=0){
				File dtf = new File(dirTemplate);
				if(dirTemplateModified != dtf.lastModified()){
					dirTemplateModified = dtf.lastModified();
					templateEngine.clear(path);
				}
			}
			servletImpl.setAttribute("root", root);
			servletImpl.setAttribute("path", uri);
			servletImpl.setAttribute("fileList", f.listFiles());
		} else {
			Map<String, Object> data = Util.loadData(root, uri);
			for (String key : data.keySet()) {
				servletImpl.setAttribute(key, data.get(key));
			}
			path = servletImpl.getServletPath();
		}
		this.service(path, servletImpl, servletImpl);
	}

}
