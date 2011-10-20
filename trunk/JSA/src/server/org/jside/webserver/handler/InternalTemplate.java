package org.jside.webserver.handler;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import org.jside.webserver.RequestUtil;
import org.xidea.lite.Template;
import org.xidea.lite.impl.HotLiteEngine;

public class InternalTemplate {
	static HotLiteEngine engine = new HotLiteEngine(URI.create("classpath:///"), null, null);
	private URI resource;
	static URI base = URI.create("classpath:///"+InternalTemplate.class.getPackage().getName().replace('.', '/')+'/');
	
	public InternalTemplate(URI resource){
		this.resource = resource;
		
	}
	public InternalTemplate(String path) {
		this(base.resolve(path));
	}
	public void render(Map<String, Object> context) throws IOException{
		Template template = engine.getTemplate(resource.toString());
		StringWriter out = new StringWriter();;
		template.render(context, out );
		RequestUtil.printResource(out,"text/html;charset=utf-8");//template.getContentType());
	}
}
