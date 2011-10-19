package org.jside.webserver.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.resource.Resource;
import org.jside.JSideWebServer;
import org.xidea.lite.tools.ResourceManager;
import org.xidea.lite.tools.ResourceManagerImpl;

public class VelocityResourceLoader extends
		org.apache.velocity.runtime.resource.loader.ResourceLoader {
	static LiteHandler defaultHandler = null;
	private LiteHandler lite;

	static RuntimeInstance init() {
		RuntimeInstance engine = new RuntimeInstance();
		engine.setProperty("input.encoding", "UTF-8");
		engine.setProperty("resource.loader", "lite");
		engine.setProperty("lite.resource.loader.class", VelocityResourceLoader.class
				.getName());
		engine.init();
		return engine;
	}
	public VelocityResourceLoader(LiteHandler lite) {
		this.lite = lite;
	}

	public VelocityResourceLoader() throws IOException {
		this(defaultHandler == null ? JSideWebServer.getInstance().getHandler(
				LiteHandler.class) : defaultHandler);
	}

	public static void main(String[] args) throws Exception {
		VelocityResourceLoader.defaultHandler = new LiteHandler();
		defaultHandler.manager = new ResourceManagerImpl(new File(
				"D:\\workspace\\JSA\\src\\test").toURI(), null);

		RuntimeInstance engine = init();
		Map<String, Object> context = new HashMap<String, Object>();
		OutputStreamWriter out = new OutputStreamWriter(System.out, "utf-8");
		
		Template template = engine.getTemplate("/test.vm");
		template.merge(new VelocityContext(context), out);
		
		out.flush();
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
		ResourceManager rm = lite.getResourceManager();
		try {
			File root = new File(rm.getRoot());
			if (new File(root, path).exists()) {
				Object data = rm.getFilteredContent(path);
				if (data instanceof String) {
					data = ((String) data).getBytes("utf-8");
				}
				return new ByteArrayInputStream((byte[]) data);
			} else {
				throw new ResourceNotFoundException(path + " not found(root:"
						+ root + ")");
			}
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
