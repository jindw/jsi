package org.jside.webserver.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.resource.Resource;
import org.xidea.lite.tools.ResourceManager;
import org.xidea.lite.tools.ResourceManagerImpl;

public class VelocityImpl extends
		org.apache.velocity.runtime.resource.loader.ResourceLoader {
	private static RuntimeInstance engine = new RuntimeInstance();
	private LiteHandler lite;

	public VelocityImpl() throws IOException {
		this(new LiteHandler());
		lite.manager = new ResourceManagerImpl(new File("D:\\workspace\\JSA\\src\\test").toURI(), null);
		//this(JSideWebServer.getInstance().getHandler(LiteHandler.class));
	}

	public VelocityImpl(LiteHandler lite) {
		this.lite = lite;
	}

	public static void main(String[] args) throws Exception{
		VelocityImpl vi = new VelocityImpl();
		Map<String, Object> context = new HashMap<String, Object>();
		OutputStreamWriter out = new OutputStreamWriter(System.out,"utf-8");
		vi.render("/test.vm", context , out);
		out.flush();
	}

	/**
	 * input.encoding=UTF-8 resource.loader = class
	 * class.resource.loader.description = Velocity Class Resource Loader
	 * class.resource.loader.class =
	 * org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
	 * class.resource.loader.cache = true runtime.log.logsystem.class =
	 * org.apache.velocity.runtime.log.NullLogSystem
	 */
	static {
		try {

			engine.setProperty("input.encoding", "UTF-8");
			engine.setProperty("resource.loader", "lite");
			engine.setProperty("lite.resource.loader.class", VelocityImpl.class.getName());
			
//			engine.setProperty("resource.loader", "file");
//			engine.setProperty("file.resource.loader.class", org.apache.velocity.runtime.resource.loader.FileResourceLoader.class.getName());
//			engine.setProperty("file.resource.loader.path","D:\\workspace\\JSA\\src\\test"); 
			

			engine.init();
		} catch (Exception e) {
		}
	}

	public void render(String path, Map<String, Object> context, Writer out)
			throws Exception {
		Template template = engine.getTemplate(path);
		template.merge(new VelocityContext(context), out);
	}

	@Override
	public long getLastModified(Resource resource) {
		ResourceManager rm = lite.getResourceManager();
		long rlm = rm.getLastModified(resource.getName());
		return rlm;
	}

	@Override
	public InputStream getResourceStream(String path)
			throws ResourceNotFoundException {
		System.out.println(path);
		ResourceManager rm = lite.getResourceManager();
		try {
			Object data = rm.getFilteredContent(path);
			if (data instanceof String) {
				data = ((String) data).getBytes("utf-8");
			}
			return new ByteArrayInputStream((byte[]) data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void init(ExtendedProperties configuration) {
	}

	@Override
	public boolean isSourceModified(Resource resource) {
		long lm = resource.getLastModified();
		ResourceManager rm = lite.getResourceManager();
		long rlm = rm.getLastModified(resource.getName());
		return rlm != lm;
	}
}
