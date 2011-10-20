package org.jside.webserver.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;

import org.jside.JSideWebServer;
import org.jside.webserver.RequestContext;
import org.jside.webserver.RequestUtil;
import org.xidea.lite.TemplateEngine;
import org.xidea.lite.impl.HotLiteEngine;
import org.xidea.lite.parse.ParseConfig;
import org.xidea.lite.tools.ResourceManager;
import org.xidea.lite.tools.ResourceManagerImpl;


public class TemplateHandler {
	static final String LITE_COMPILE_SERVICE = "/WEB-INF/service/lite-compile";
	private File root;
	private ResourceManager resourceManager;
	private TemplateServletImpl liteServlet;
	private long lastModifiedTime = 0;
	private URLClassLoader webClassLoader;
	private List<File> libs = Collections.emptyList();
	private HashMap<String, TemplateServletImpl> templateServletMap;

	public static void main(String[] args) {
		JSideWebServer.getInstance().addAction("/**", new TemplateHandler());
	}

	public void execute() throws IOException, ServletException {
		RequestContext context = init();
		final String uri = context.getRequestURI();
		if (uri.equals(LITE_COMPILE_SERVICE)) {
			TemplateUtil.compileLite(liteServlet, context);
		} else if (uri.endsWith(".xhtml")
				|| uri.equals("/WEB-INF/service/lite-service")) {
			liteServlet.service(context);
		} else if (uri.endsWith(".vm")) {
			String engineName = "org.jside.webserver.handler.VelocityTemplateEngine";
			renderTemplate(context, engineName);
		} else if (uri.endsWith(".ftl")) {
			String engineName = "org.jside.webserver.handler.FreemarkerTemplateEngine";
			renderTemplate(context, engineName);
		} else {
			File file = new File(root, uri);
			if (file.isDirectory()) {
				TemplateUtil.printDir(root, uri);
			} else {
				Object result = resourceManager.getFilteredContent(uri);
				RequestUtil.printResource(result, null);
			}
		}
	}

	private void renderTemplate(RequestContext context, String engineName)
			throws ServletException, IOException {
		TemplateServletImpl servlet;
		try{
			servlet = getTemplateServlet(engineName);
		}catch (ClassNotFoundException e) {
			TemplateUtil.printNotSupport(root, context.getRequestURI(),engineName,e);
			return;
		}catch(NoClassDefFoundError e){
			TemplateUtil.printNotSupport(root, context.getRequestURI(),engineName,e);
			return;
		}
		servlet.service(context);
		
	}

	private TemplateServletImpl getTemplateServlet(String engineName)
			throws ClassNotFoundException {
		TemplateServletImpl impl = templateServletMap.get(engineName);
		if (impl == null) {
			TemplateEngine templateEngine;
			try {
				Class<?> engineClass = Class.forName(engineName,true,this.webClassLoader);
				templateEngine = (TemplateEngine) engineClass.getConstructor(ResourceManager.class).newInstance(resourceManager);
				impl = new TemplateServletImpl(resourceManager, templateEngine);
				templateServletMap.put(engineName, impl);
			}catch(ClassNotFoundException e){
				throw e;
			} catch (Exception e) {
				throw new ClassNotFoundException(engineName,e);
				//throw new RuntimeException(e);
			}

			
		}
		//throw new ClassNotFoundException();
		return impl;
	}

	synchronized RequestContext init() throws IOException, ServletException {
		RequestContext context = RequestUtil.get();
		URI base = context.getServer().getWebBase();
		File root = new File(base);
		if (isModified(root, context)) {
			this.root = root;
			this.templateServletMap = new HashMap<String, TemplateServletImpl>();
			resourceManager = new ResourceManagerImpl(base, base
					.resolve("WEB-INF/lite.xml"));
			liteServlet = new TemplateServletImpl(resourceManager, new HotLiteEngine(
					(ParseConfig) resourceManager, null));
			lastModifiedTime = System.currentTimeMillis();
			File lib = new File(root, "WEB-INF/lib");
			this.libs = new ArrayList<File>();
			ArrayList<URL> urls = new ArrayList<URL>();
			if (lib.exists()) {
				libs.add(lib);
				for (File f : lib.listFiles()) {
					if (f.getName().endsWith(".jar")) {
						libs.add(f);
						urls.add(f.toURI().toURL());
					}
				}
			}
			initClassLoader(urls);
		}
		return context;
	}

	private void initClassLoader(ArrayList<URL> urls) {
		this.webClassLoader = new URLClassLoader(urls.toArray(new URL[urls
				.size()]), this.getClass().getClassLoader()) {
			@Override
			protected Class<?> findClass(final String name)
					throws ClassNotFoundException {
				String path = "/internal/"+ name.replace('.', '/') + ".class";
				URL url = this.getClass().getResource(path);

				try {
					if (url != null) {
						InputStream in = url.openStream();
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						byte[] buf = new byte[1024];
						int c;
						while ((c = in.read(buf)) >= 0) {
							out.write(buf, 0, c);
						}
						byte[] data = out.toByteArray();
						return super
								.defineClass(name, data, 0, data.length);
					}
				} catch (Exception e) {
					throw new ClassNotFoundException(e.toString(),e);
				}
				return super.findClass(name);
			}

		};
		System.setSecurityManager(null);
	}

	@SuppressWarnings("unchecked")
	private boolean isModified(File root, RequestContext context) {
		if (root.equals(this.root)) {
			List<File> files = ((ResourceManagerImpl) resourceManager)
					.getScriptFileList();
			long lastModifiedTime = 0;
			File lib = new File(root, "WEB-INF/lib");
			List<File> libs = this.libs;
			if (libs.size() > 0 ^ lib.exists()) {// 0 ^0 =>0 1^1 =>0
				return true;
			}
			List[] lists = new List[] { libs, files };
			for (List<File> files2 : lists) {
				for (File f : files2) {
					if (f.exists()) {
						lastModifiedTime = Math.max(f.lastModified(),
								lastModifiedTime);
					} else {
						return true;
					}
				}
			}
			return lastModifiedTime > this.lastModifiedTime;
		}
		return true;
	}

	public ResourceManager getResourceManager() {
		return this.resourceManager;
	}

	public void setResourceManager(ResourceManager manager) {
		this.resourceManager = manager;
	}

}
