package org.jside.webserver.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;
import org.xidea.lite.TemplateEngine;
import org.xidea.lite.tools.ResourceManager;

public class VelocityTemplateEngine implements TemplateEngine {
	private RuntimeInstance engine;


	public VelocityTemplateEngine(ResourceManager manager) {
		//Velocity 静态配置缺陷，此处只好采用自动选择当前调试服务器的配置
		//VelocityResourceLoader.setResourceManager(manager);
		engine = VelocityResourceLoader.init();
	}

	public void clear(String path) {
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
				template
						.merge(new VelocityContext((Map) context), (Writer) out);
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
}
