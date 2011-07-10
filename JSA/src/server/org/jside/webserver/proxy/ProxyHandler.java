//package org.jside.webserver.proxy;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
//import java.io.Reader;
//import java.io.StringWriter;
//import java.io.UnsupportedEncodingException;
//import java.net.HttpURLConnection;
//import java.net.Socket;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Pattern;
//import java.util.zip.GZIPInputStream;
//import java.util.zip.GZIPOutputStream;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.jside.JSideWebServer;
//import org.jside.webserver.RequestUtil;
//import org.jside.webserver.RequestContext;
//import org.jside.webserver.RequestContextImpl;
//
//public class ProxyHandler {
//	private static Log log = LogFactory.getLog(ProxyHandler.class);
//	private static final String ISO_8859_1 = "ISO-8859-1";
//	private static final Pattern HOST_PATTERN = Pattern
//			.compile("^[\\w\\-]+(?:\\.[\\w\\-]+)*(?:\\:\\d+)?$");
//
//	private static final String CONTENT_LENGTH = "Content-Length";
//	protected ArrayList<PatternProxyFilter> requestHeadFilters = new ArrayList<PatternProxyFilter>();
//	protected ArrayList<PatternProxyFilter> requestContentFilters = new ArrayList<PatternProxyFilter>();
//	protected ArrayList<PatternProxyFilter> responseHeadFilters = new ArrayList<PatternProxyFilter>();
//	protected ArrayList<PatternProxyFilter> responseContentFilters = new ArrayList<PatternProxyFilter>();
//
//	protected static ProxyHandler instance;
//
//	public static ProxyHandler getInstance() {
//		if (instance == null) {
//			instance = new ProxyHandler();
//		}
//		return instance;
//	}
//
//	public void addRequestHeadFilter(String pattern, ProxyFilter headFilter) {
//		requestHeadFilters.add(new PatternProxyFilter(pattern, headFilter));
//	}
//
//	public void addRequestContentFilter(String pattern,
//			ProxyFilter contentFilter) {
//		requestContentFilters
//				.add(new PatternProxyFilter(pattern, contentFilter));
//	}
//
//	public void addResponseHeadFilter(String pattern, ProxyFilter headFilter) {
//		responseHeadFilters.add(new PatternProxyFilter(pattern, headFilter));
//	}
//
//	public void addResponseContentFilter(String pattern,
//			ProxyFilter contentFilter) {
//		responseContentFilters.add(new PatternProxyFilter(pattern,
//				contentFilter));
//	}
//
//	public void processRequest() throws IOException {
//		RequestContext context = RequestUtil.get();
//		String url = context.getRequestURI();
//		if(log.isDebugEnabled()){
//			log.debug("处理请求：" + url);
//		}
//		if (!processProxy(context)) {
//			context.setStatus(500, "不支持协议：" + url);
//		}
//	}
//
//	/**
//	 * .jside.org 域名不走代理
//	 * @param context
//	 * @return
//	 * @throws IOException
//	 */
//	protected boolean processProxy(RequestContext context) throws IOException {
//		String url = context.getRequestURI();
//		if (url.startsWith("http://")) {
//			String host = getHost(context);
//			if (host.indexOf(".jside.org:")< 0) {
//				//TODO:WHY???
//				//context.setEncoding(null);
//				dispatchServer((RequestContextImpl) context, host, null);
//			}else{
//				context.dispatch(url.substring('/',"http://".length()));
//			}
//			return true;
//		}
//		return false;
//	}
//
//	public void execute() throws IOException {
//		processProxy(RequestUtil.get());
//	}
//
//	public void dispatch(RequestContext context, String path)
//			throws IOException {
//		dispatch(context, path, null);
//	}
//
//	public void dispatch(RequestContext context, String path, String remoteHost)
//			throws IOException {
//		if (HOST_PATTERN.matcher(path).find()) {
//			dispatchServer((RequestContextImpl) context, path, remoteHost);
//		} else if (path.startsWith("/")) {
//			context.dispatch(path);
//		} else {
//			dispatchURL((RequestContextImpl) context, path);
//		}
//	}
//
//	private void dispatchURL(RequestContextImpl context, String path)
//			throws IOException {
//		URL url = new URL(path);
//		if ("http".equals(url.getProtocol())
//				|| "https".equals(url.getProtocol())) {
//			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//			String method = context.getMethod();
//			conn.setRequestMethod(method);
//			List<String> headers = context.getRequestHeaders();
//			for (String line : headers) {
//				int p = line.indexOf(':');
//				if (p > 0) {
//					conn.addRequestProperty(line.substring(0, p), line
//							.substring(p + 1));
//				}
//			}
//			String length = context.getRequestHeader(
//					"Content-Length");
//			if (length != null) {
//				int l = Integer.parseInt(length);
//				if (l > 0) {
//					printFix(context.getInputStream(),
//							toPrintWriter(conn.getOutputStream()), l);
//				}
//			}
//			InputStream in = conn.getInputStream();
//			Map<String, List<String>> hfs = conn.getHeaderFields();
//			for(Map.Entry<String, List<String>> entry : hfs.entrySet()){
//				String key = entry.getKey();
//				for(String value : entry.getValue()){
//					context.addResponseHeader(key+":"+value);
//				}
//			}
//			RequestUtil.printResource(in,conn.getContentType());
//			in.close();
//		} else {
//			RequestUtil.printResource(url,null);
//		}
//	}
//
//	private void dispatchServer(RequestContextImpl context, String host,
//			String remote) throws IOException {
//		if (remote == null) {
//			remote = host;
//		}
//		int p = remote.indexOf(':');
//		String ip, port;
//		if (p > 0) {
//			ip = remote.substring(0, p);
//			port = remote.substring(p + 1);
//		} else {
//			ip = remote;
//			port = "80";
//		}
//		Socket socket = new Socket(ip, Integer.parseInt(port));
//		BufferedReader rin = toReader(socket.getInputStream());
//		PrintWriter rout = toPrintWriter(socket.getOutputStream());
//		List<String> headers = context.getRequestHeaders();
//		List<String> result = new ArrayList<String>();
//		for (String line : headers) {
//			if (line.startsWith("Proxy-")) {
//				continue;
//			}
//			if (line.regionMatches(true, 0, "Host:", 0, 5)) {
//				if (host.endsWith(":80")) {
//					host = host.substring(0, host.length() - 3);
//				}
//				line = "Host:" + host;
//			}
//			result.add(line);
//		}
//		doSend(context, result, rout);
//		doReceive(context, rin);
//
//	}
//
//	private String getHost(RequestContext context) throws IOException {
//		String url = context.getRequestURI();
//		if(log.isDebugEnabled()){
//			log.debug("处理代理请求：" + url);
//		}
//		URL resource = new URL(url);
//		int port = resource.getPort();
//		if (port < 0) {
//			port = resource.getDefaultPort();
//		}
//		return resource.getHost() + ':' + port;
//	}
//
//	private void doSend(RequestContext context, List<String> headers,
//			PrintWriter rout) throws IOException {
//		final String url = context.getRequestURI();
//		String path = url.startsWith("/") ? url : url.substring(url.indexOf(
//				"/", url.indexOf("//") + 2));
//		String query = ((RequestContextImpl)context).getQuery();
//		if (query != null) {
//			path = path + '?' + query;
//		}
//		String requestLine = context.getMethod() + " " + path + " "
//				+ "HTTP/1.1";
//		if (!this.requestHeadFilters.isEmpty()) {
//			headers = doHeadFilter(url, this.requestHeadFilters, requestLine,
//					headers);
//			requestLine = headers.remove(0);
//		}
//
//		rout.println(requestLine);
//		String contentLength = getHeader(headers, CONTENT_LENGTH);
//		for (String line : headers) {
//			rout.println(line);
//		}
//		rout.println();
//		rout.flush();
//		if (contentLength != null) {
//			InputStream in = ((RequestContextImpl) context).getInputStream();
//			processContent(context, in, rout, false,
//					this.requestContentFilters, url, Integer
//							.parseInt(contentLength));
//
//		}
//		rout.flush();
//	}
//
//	private void doReceive(RequestContextImpl context, InputStream rin)
//			throws IOException {
//		PrintWriter out = toPrintWriter(context.getOutputStream());
//		String firstLine = rin.readLine();
//		String method = context.getMethod();
//		if (firstLine != null) {
//			List<String> headers = readHeaders(rin);
//			if (!this.responseHeadFilters.isEmpty()) {
//				headers = doHeadFilter(context.getRequestURI(),
//						this.responseHeadFilters, firstLine, headers);
//				firstLine = headers.remove(0);
//			}
//			String[] fls = firstLine.split("[\\s]");
//
//			int code = Integer.parseInt(fls[1]);
//			boolean isGZip = "gzip".equalsIgnoreCase(getHeader(headers,
//					"Content-Encoding"));
//			boolean isChunked = "chunked".equalsIgnoreCase(getHeader(headers,
//					"Transfer-Encoding"));
//
//			context.setStatus(code, fls.length > 2 ? fls[2] : null);
//
//			for (String line : headers) {
//				if (!isChunked || !line.startsWith("Transfer-Encoding")) {
//					context.addResponseHeader(line);
//				}
//			}
//			context.setResponseHeader("Connection:close");
//			String contentLength = getHeader(headers, CONTENT_LENGTH);
//			;
//			log.debug("返回数据长度：" + method + code + contentLength);
//			// 1xx, 204, and 304
//			if ("HEAD".equals(method) || "TRACE".equals(method) || code == 304
//					|| code == 204 || code == 205 || code < 200) {
//			} else {
//				if (isChunked) {
//					rin = fromChunked(rin);
//				}
//				processContent(context, rin, out, isGZip,
//						this.responseContentFilters, context.getRequestURI(),
//						contentLength == null ? -1 : Integer
//								.parseInt(contentLength));
//			}
//		} else {
//			log.warn("怎么第一行就错了？？？");
//		}
//		out.flush();
//	}
//
//	private void processContent(RequestContext context, InputStream in,
//			PrintWriter out, boolean isGzip, List<PatternProxyFilter> filters,
//			final String url, int contentLength) throws IOException {
//
//		if (match(url, filters)) {
//			StringWriter buf = new StringWriter();
//			printFix(in, new PrintWriter(buf, true), contentLength);
//			String content = buf.toString();
//			if (isGzip) {
//				content = readGZip(content);
//			}
//			for (PatternProxyFilter pf : filters) {
//				if (pf.match(url)) {
//					try {
//						byte[] data = content.getBytes(ISO_8859_1);
//						String encoding = pf
//								.findEncoding(new ByteArrayInputStream(data));
//						// System.out.println(encoding);
//						if (encoding != null) {
//							content = pf.filter(new String(data, encoding));
//							content = new String(content.getBytes(encoding),
//									ISO_8859_1);
//						} else {
//							content = pf.filter(content);
//						}
//					} catch (Exception e) {
//						log.error("过滤内容失败", e);
//					}
//				}
//			}
//
//			if (isGzip) {
//				content = writeGZip(content);
//				// System.out.println(readGZip(content));
//			}
//
//			contentLength = content.length();
//			// System.out.println(contentLength);
//			context.setResponseHeader(CONTENT_LENGTH + ":" + contentLength);
//			out.append(content);
//		} else {
//			printFix(in, out, contentLength);
//		}
//	}
//
//	private BufferedReader fromChunked(final Reader reader) throws IOException {
//
//		final ChunkedInputStream cin = new ChunkedInputStream(
//				new InputStream() {
//					@Override
//					public int read() throws IOException {
//						return reader.read();
//					}
//				});
//		return new BufferedReader(new Reader() {
//			@Override
//			public void close() throws IOException {
//				cin.close();
//			}
//
//			@Override
//			public int read(char[] cbuf, int off, int len) throws IOException {
//				int v = cin.read();
//				if (v >= 0) {
//					cbuf[off] = (char) v;
//					return 1;
//				}
//				return -1;
//			}
//		});
//	}
//
//	private String readGZip(String content) throws IOException,
//			UnsupportedEncodingException {
//		GZIPInputStream gin = new GZIPInputStream(new ByteArrayInputStream(
//				content.getBytes(ISO_8859_1)));
//		StringBuilder buf2 = new StringBuilder();
//		while (true) {
//			int b = gin.read();
//			if (b >= 0) {
//				buf2.append((char) b);
//			} else {
//				break;
//			}
//		}
//		content = buf2.toString();
//		return content;
//	}
//
//	private String writeGZip(String content) throws IOException,
//			UnsupportedEncodingException {
//		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
//		GZIPOutputStream gout = new GZIPOutputStream(out2);
//		gout.write(content.getBytes(ISO_8859_1));
//		gout.flush();
//		gout.finish();
//		gout.close();
//		content = new String(out2.toByteArray(), ISO_8859_1);
//		return content;
//	}
//
//	private List<String> doHeadFilter(String url,
//			ArrayList<PatternProxyFilter> headFilters, String requestLine,
//			List<String> headers) {
//		StringBuilder buf = new StringBuilder(requestLine);
//		for (String line : headers) {
//			buf.append("\r\n");
//			buf.append(line);
//		}
//		String content = buf.toString();
//		for (PatternProxyFilter pf : headFilters) {
//			if (pf.match(url)) {
//				content = pf.filter(content);
//			}
//		}
//		String[] list = content.split("[\\r\\n]+");
//		return new ArrayList<String>(Arrays.asList(list));
//	}
//
//	private boolean match(final String url, List<PatternProxyFilter> filters) {
//		for (PatternProxyFilter pf : filters) {
//			if (pf.match(url)) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private List<String> readHeaders(BufferedReader rin) throws IOException {
//		List<String> result = new ArrayList<String>();
//		while (true) {
//			String line = rin.readLine();
//			if (line == null) {
//				log.info("连接直接断开了");
//				break;
//			}
//			if (line.length() == 0) {
//				break;
//			}
//			result.add(line);
//		}
//		return result;
//	}
//
//	private void printFix(InputStream in, PrintWriter rout, int contentLength)
//			throws IOException {
//		if (contentLength < 0) {
//			readUnFix(in, rout);
//		} else {
//			int c;
//			char[] buf = new char[64];
//			while ((c = in.read(buf, 0, Math.min(contentLength, buf.length))) >= 0
//					&& contentLength > 0) {
//				contentLength -= c;
//				// System.out.print(new String(buf, 0, c));
//				rout.write(buf, 0, c);
//			}
//			while (contentLength > 0) {
//				rout.write(0);
//				contentLength--;
//			}
//		}
//	}
//
//	private void readUnFix(InputStream in, PrintWriter rout) throws IOException {
//		while (true) {
//			int c = in.read();
//			if (c >= 0) {
//				rout.write(c);
//			} else {
//				break;
//			}
//		}
//	}
//
//	private BufferedReader toReader(InputStream in) throws IOException {
//		return new BufferedReader(new InputStreamReader(in, ISO_8859_1));
//	}
//
//	private PrintWriter toPrintWriter(OutputStream out) throws IOException {
//		return new PrintWriter(new OutputStreamWriter(out, ISO_8859_1), true);
//	}
//
//	// Transfer-Encoding
//
//	private String getHeader(List<String> headers, String key) {
//		for (String line : headers) {
//			int length = key.length();
//			if (line.length() > length && line.charAt(length) == ':'
//					&& line.regionMatches(true, 0, key, 0, length)) {
//				return line.substring(length + 1).trim();
//			}
//		}
//		return null;
//	}
//
//	public static void main(String[] args) throws IOException {
//		JSideWebServer.getInstance().addAction("http://**",
//				ProxyHandler.getInstance());
//	}
//
//}
