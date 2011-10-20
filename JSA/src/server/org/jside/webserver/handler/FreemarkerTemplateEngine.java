package org.jside.webserver.handler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;

import org.xidea.lite.TemplateEngine;
import org.xidea.lite.tools.ResourceManager;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerTemplateEngine implements TemplateEngine ,TemplateLoader{
	private Configuration engine;
	private ResourceManager resourceManager;


	public FreemarkerTemplateEngine(ResourceManager manager) {
		this. engine = new Configuration();
		engine.setTemplateLoader(this);
		this.resourceManager = manager;
	}

	public void clear(String path) {
		engine.clearTemplateCache();
	}

	public org.xidea.lite.Template getTemplate(final String path)
			throws IOException {
		final Template template = engine.getTemplate(path, "utf-8");
		return new org.xidea.lite.Template() {

			public void render(Map<String, Object> context, Object[] children,
					Appendable out) {
				throw new UnsupportedOperationException();
			}

			@SuppressWarnings("unchecked")
			public void render(Object context, Appendable out)
					throws IOException {
				try {
					template
							.process((Map) context, (Writer) out);
				} catch (TemplateException e) {
					throw new IOException(e);
				}
			}

			public String getEncoding() {
				return "utf-8";
			}

			public String getContentType() {
				return "text/html;charset=utf-8";
			}

			public void addVar(String name, Object value) {
			}
		};
	}

	public void render(String path, Object context, Writer out)
			throws IOException {
		try {
			this.getTemplate(path).render(context, out);
		} catch (Exception e) {
			e.printStackTrace(new PrintWriter(out,true));
		}
	}

	public void closeTemplateSource(Object path) throws IOException {
	}

	public Object findTemplateSource(String path) throws IOException {
		path = path.replaceFirst("^/?", "/");
		if(new File(new File(resourceManager.getRoot()),path).exists()){
			return path;
		}else{
			return null;
		}
	}

	public long getLastModified(Object ref) {
		return resourceManager.getLastModified((String)ref);
	}

	public Reader getReader(Object ref, String encoding) throws IOException {
		return new StringReader(resourceManager.getFilteredText((String)ref));
	}
}
