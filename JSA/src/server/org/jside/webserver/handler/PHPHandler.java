package org.jside.webserver.handler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.jside.JSideWebServer;
import org.jside.webserver.CGIEnvironment;
import org.jside.webserver.CGIRunner;
import org.jside.webserver.RequestContext;
import org.jside.webserver.RequestUtil;

public class PHPHandler {
	private static PHPEnv phpEnv = new PHPEnv();
	private String filterScript = "/WEB-INF/default-filter.php";

	public void execute() throws IOException {
		RequestContext context = RequestUtil.get();
		String uri = context.getRequestURI();
		URI base = context.getServer().getWebBase();
		File root = new File(base);
		if (new File(root,filterScript).exists()) {
			execute(context, filterScript);
		}
		if (!context.isAccept()) {
			execute(context, uri);
		}
	}

	private void execute(RequestContext context, String uri) throws IOException {
		URI base = context.getServer().getWebBase();
		String rp = CGIEnvironment.toRealPath(base, uri);
		if (rp.endsWith(".php")) {

			CGIEnvironment env = new CGIEnvironment(context);
			Map<String, String> envp = env.toMap(System.getenv());
			String compile_service = "http://127.0.0.1:"
					+ context.getServer().getPort()
					+ LiteHandler.LITE_COMPILE_SERVICE;
			envp.put("LITE_COMPILE_SERVICE", compile_service);
			CGIRunner cr = new CGIRunner(context, env.scriptFilename, envp,
					new File(new File(base), rp).getParentFile(), null);
			cr.setCgiExecutable(phpEnv.getPHPCmd());
			cr.run();
		}
	}

	public static void main(String[] args) {
		JSideWebServer.getInstance().addAction("/**", new PHPHandler());
	}

}
