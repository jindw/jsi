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

	public void execute() throws IOException {
		RequestContext context = RequestUtil.get();
		String uri = context.getRequestURI();
		URI base = context.getServer().getWebBase();
		String rp = CGIEnvironment.toRealPath(base, uri);
		if (rp.endsWith(".php")) {
			Map<String, String> envp = new CGIEnvironment(context).toMap(null);
			CGIRunner cr = new CGIRunner(context, "php-cgi", envp,
					new File(new File(base), rp).getParentFile(), null);
			cr.run();
		}
	}

	public static void main(String[] args) {
		JSideWebServer.getInstance().addAction("/**", new PHPHandler());
	}

}
