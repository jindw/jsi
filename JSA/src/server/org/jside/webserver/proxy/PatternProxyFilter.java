package org.jside.webserver.proxy;

import java.io.InputStream;

import org.jside.webserver.action.URLMatcher;

class PatternProxyFilter implements ProxyFilter {
	static URLMatcher toPathPattern(String path, boolean isProxy) {

		if (isProxy && path.startsWith("/")) {
			path = "http://*/" + path;
		}
		return URLMatcher.createMatcher(path);
	}

	private ProxyFilter base;
	private URLMatcher pattern;
	PatternProxyFilter(String pattern,ProxyFilter base){
		this.base = base;
		this.pattern = toPathPattern(pattern,true);
	}
	public boolean match(String uri) {
		return pattern.match(uri);
	}
	public String filter(String content) {
		return base.filter(content);
	}
	public String findEncoding(InputStream content) {
		return base.findEncoding(content);
	}


}
