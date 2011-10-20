package org.jside.webserver.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.resource.Resource;
import org.jside.JSideWebServer;
import org.xidea.lite.tools.ResourceManager;

public class VelocityResourceLoader extends
		org.apache.velocity.runtime.resource.loader.ResourceLoader {
	private TemplateHandler lite;
	private static TemplateHandler TEST_HANDLER = null;
	static RuntimeInstance init() {
		RuntimeInstance engine = new RuntimeInstance();
		engine.setProperty("input.encoding", "UTF-8");
		engine.setProperty("resource.loader", "lite");
		engine.setProperty("lite.resource.loader.class",
				VelocityResourceLoader.class.getName());
		engine.init();
		return engine;
	}

	public VelocityResourceLoader(TemplateHandler lite) {
		this.lite = lite;
	}

	public VelocityResourceLoader() throws IOException {
		this(TEST_HANDLER == null ? JSideWebServer.getInstance().getHandler(
				TemplateHandler.class) : TEST_HANDLER);
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
