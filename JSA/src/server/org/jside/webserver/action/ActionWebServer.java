package org.jside.webserver.action;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jside.webserver.RequestUtil;
import org.jside.webserver.MutiThreadWebServer;
import org.jside.webserver.RequestContext;

public class ActionWebServer extends MutiThreadWebServer {
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(ActionWebServer.class);
	protected List<Invocation> invocationList = new ArrayList<Invocation>();

	public ActionWebServer(URI webBase) {
		super(webBase);
	}

	public void reset() {
		invocationList.clear();
	}

	public void addAction(String path, String className) {
		try {
			addAction(path, Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void addAction(String path, Object action) {
		invocationList.add(createActionInvocation(path, action));
	}

	public Invocation createActionInvocation(String path, Object action) {
		return new InvocationImpl(path, action);
	}

	@Override
	public void processRequest(RequestContext handle) throws IOException {
		final String uri = handle.getRequestURI();
		for (Invocation ai : invocationList) {
			if (ai.match(uri)) {
				try {
					ai.execute(handle);
					if(handle.isAccept()){
						return;
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		if(webBase!= null){
			RequestUtil.printResource();
		}
	}
}
