package org.xidea.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.xidea.template.Template;
import org.xidea.template.parser.DecoratorMapper;
import org.xidea.template.parser.ParseContext;
import org.xidea.template.parser.XMLParser;

public class TempateEngine {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(TempateEngine.class);
	public static final String DEFAULT_DECORATOR_MAPPING = "/WEB-INF/decorators.xml";
	protected DecoratorMapper decoratorMapper;
	protected File webRoot;
	protected XMLParser parser = new XMLParser();

	public TempateEngine() {
	}

	public TempateEngine(File webRoot) {
		this(webRoot, new File(webRoot, DEFAULT_DECORATOR_MAPPING));
	}
	public TempateEngine(File webRoot, File config) {
		try {
			InputStream configStream = new FileInputStream(config);
			this.decoratorMapper = new DecoratorMapper(configStream);
			this.webRoot = webRoot;
		} catch (FileNotFoundException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public void render(String path, Map context, Writer out) throws IOException {
		getTemplate(path).render(context, out);
	}

	protected URL getResource(String pagePath) throws MalformedURLException {
		return new File(webRoot, pagePath).toURI().toURL();
	}

	protected Template getTemplate(String pagePath) {
		if (pagePath.endsWith("/")) {
			pagePath = pagePath + "index.xhtml";
		}
		ParseContext parseContext = new ParseContext();
		String decoratorPath = decoratorMapper == null ? null : decoratorMapper
				.getDecotatorPage(pagePath);
		if (decoratorPath != null) {
			try {
				Node node = parser.loadXML(getResource(pagePath), parseContext);
				parseContext.put("#page", node);
				pagePath = decoratorPath;
			} catch (Exception e) {
				log.error(e);
			}
		}
		try {
			List<Object> items = parser.parse(getResource(pagePath),
					parseContext);
			Template template = new Template(items);
			return template;
		} catch (IOException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

}
