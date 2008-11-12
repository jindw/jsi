package org.xidea.template;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.xidea.template.parser.DecoratorMapper;
import org.xidea.template.parser.ParseContext;
import org.xidea.template.parser.XMLParser;

public class TempateServlet extends GenericServlet {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory
			.getLog(TempateServlet.class);
	private static final String DEFAULT_DECORATOR_MAPPING = "/WEB-INF/decorators.xml";
	protected XMLParser parser = new XMLParser();
	protected DecoratorMapper decoratorMapper;

	protected Map<Object, Object> createDefaultModel(final HttpServletRequest req) {
		Map<Object, Object> model = new HashMap<Object, Object>();
		model.put("params", req.getParameterMap());
		model.put("requestURI", req.getRequestURI());
		return model;
	}

	@Override
	public void service(ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {
		HttpServletRequest request = (HttpServletRequest) req;
		String path = request.getServletPath();
		String decoratorPath = decoratorMapper.getDecotatorPage(path);
		Template template = getTemplate(path, decoratorPath);
		template.render(createDefaultModel(request), resp.getWriter());
	}
	protected Template getTemplate(String pagePath, String decoratorPath) {
		// TODO:do cache
		ServletContext context = this.getServletContext();
		if (pagePath != null) {
			pagePath = context.getRealPath(pagePath);
		}
		ParseContext parseContext = new ParseContext();
		if (decoratorPath != null) {
			try {
				Node node = parser.loadXML(new File(pagePath).toURI().toURL(),
						parseContext);
				parseContext.put("#page", node);
				pagePath = context.getRealPath(decoratorPath);
			} catch (Exception e) {
				log.error(e);
			}
		}
		List<Object> items;
		try {
			items = parser.parse(new File(pagePath).toURI().toURL(),
					parseContext);
			Template template = new Template(items);
			return template;
		} catch (IOException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	protected String getParam(String key, String defaultValue) {
		String value = this.getInitParameter(key);
		return value == null ? defaultValue : value;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			String decoratorPath = this.getParam("decoratorMapping",
					DEFAULT_DECORATOR_MAPPING);
			InputStream configStream = this.getServletContext()
					.getResourceAsStream(decoratorPath);
			this.decoratorMapper = new DecoratorMapper(configStream);
		} catch (Exception e) {
			log.error("装载页面装饰配置信息失败", e);
		}
	}
}
