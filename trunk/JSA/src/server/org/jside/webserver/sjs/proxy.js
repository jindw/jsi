function proxyDispatch(host,remote){
	var ws = Packages.org.jside.webserver.proxy.ProxyHandler.getInstance();
	var context = Packages.org.jside.webserver.RequestContext.get();
	var url = context.requestURI;
	ws.dispatch(context,host,remote||null);
}