package org.jside.webserver.proxy;

import java.io.InputStream;

public interface ProxyFilter {
	public String findEncoding(InputStream content);
	public String filter(String content);
}
