package org.jside.jsi.tools.web;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jside.jsi.tools.generator.PackageAction;
import org.jside.webserver.RequestContextImpl;
import org.jside.webserver.RequestUtil;
import org.jside.webserver.RequestContext;
import org.jside.webserver.action.InvocationImpl;
import org.xidea.jsi.impl.ResourceRoot;
import org.xidea.jsi.web.JSIService;
import org.xidea.lite.impl.HotLiteEngine;

public class ScriptAction extends JSIService {
	private Log log = LogFactory.getLog(ScriptAction.class);
	private HotLiteEngine lite;
	// private ActionInvocationImpl liteAction = new
	// ActionInvocationImpl("",lite);
	private PackageAction packageAction = new PackageAction(this);
	private InvocationImpl packageGen = new InvocationImpl("",
			packageAction);

	public ScriptAction() {
		try {
			URI root = new URI("classpath:///org/jside/jsi/tools/web/");
			lite = new HotLiteEngine(root,null,null);
		} catch (URISyntaxException e) {
		}
	}

	public void execute() throws Exception {
		RequestContext context = RequestUtil.get();
		final String scriptBase = findScriptBase(context);
		final String url = getPath(context, scriptBase);
		if (url == null) {
			RequestUtil.sendRedirect(scriptBase);
			return;
		}

		URI script = context.getResource(scriptBase);
		final File scriptBaseFile = new File(script);
		this.reset();
		if (scriptBaseFile != null) {
			this.addSource(scriptBaseFile);
			this.addLib(scriptBaseFile);
		}
		URI lib = context.getResource("/WEB-INF/lib/");
		if (lib != null) {
			try {
				File file = new File(lib);
				this.addLib(file);
			} catch (Exception e) {
				log.warn(e);
			}
		}
		URL res = ResourceRoot.class.getResource("ResourceRoot.class");
		if (res != null) {
			URLConnection conn = res.openConnection();
			if (conn instanceof JarURLConnection) {
				res = ((JarURLConnection) conn).getJarFileURL();
				try {
					File file = new File(res.toURI());
					this.addLib(file);
				} catch (Exception e) {
					log.warn(e);
				}
			}
		}
		if (url.indexOf('/') == -1) {
			if (url.endsWith(".xhtml")) {
				OutputStreamWriter out = new OutputStreamWriter(context
						.getOutputStream(), this.getEncoding());
				lite.render(url, context.getContextMap(), out);
				out.flush();
				return;
			} else if (url.equals("package.action")) {
				packageAction.setSources(new File[] { scriptBaseFile });
				packageGen.execute(context);
				return;
			}
		}
		Map<String, String[]> params = context.getParams();
		OutputStream out = context.getOutputStream();
		// context.setContentType("text/plain;charset=utf-8");

		// log.info(context.getRequestURI()+params);
		this.service(url, params, out, context);
	}

	@Override
	protected void addHeader(Object[] context, String key, String value) {
		((RequestContextImpl) context[0]).addResponseHeader(key + ':' + value);
	}

	@Override
	protected String getHeader(Object[] context, String key) {
		return ((RequestContext) context[0]).getRequestHeader(key);
	}

	private String getPath(RequestContext context, String scriptBase) {
		String url = context.getRequestURI();
		if (!url.startsWith(scriptBase)) {
			return null;
		}
		url = url.substring(scriptBase.length());
		if (url.length() == 0) {
			url = context.getParam().get("path");
			if (url == null) {
				url = "";
			}
		}
		return url;
	}

	private String findScriptBase(RequestContext context) {
		String scriptBase = (String) context.getServer().getApplication().get("scriptBase");
		if (scriptBase == null) {
			scriptBase = "/scripts/";
		}
		return scriptBase;
	}

}
