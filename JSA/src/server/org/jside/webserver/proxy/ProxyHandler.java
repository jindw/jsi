package org.jside.webserver.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jside.JSideWebServer;
import org.jside.webserver.RequestUtil;
import org.jside.webserver.RequestContext;
import org.jside.webserver.RequestContextImpl;

public class ProxyHandler {
	private static Log log = LogFactory.getLog(ProxyHandler.class);
	private static final String ISO_8859_1 = "ISO-8859-1";
	private static final Pattern HOST_PATTERN = Pattern
			.compile("^[\\w\\-]+(?:\\.[\\w\\-]+)*(?:\\:\\d+)?$");

	private static final String CONTENT_LENGTH = "Content-Length";
	protected ArrayList<PatternProxyFilter> requestHeadFilters = new ArrayList<PatternProxyFilter>();
	protected ArrayList<PatternProxyFilter> requestContentFilters = new ArrayList<PatternProxyFilter>();
	protected ArrayList<PatternProxyFilter> responseHeadFilters = new ArrayList<PatternProxyFilter>();
	protected ArrayList<PatternProxyFilter> responseContentFilters = new ArrayList<PatternProxyFilter>();

	protected static ProxyHandler instance;

	public static ProxyHandler getInstance() {
		if (instance == null) {
			instance = new ProxyHandler();
		}
		return instance;
	}

	public void addRequestHeadFilter(String pattern, ProxyFilter headFilter) {
		requestHeadFilters.add(new PatternProxyFilter(pattern, headFilter));
	}

	public void addRequestContentFilter(String pattern,
			ProxyFilter contentFilter) {
		requestContentFilters
				.add(new PatternProxyFilter(pattern, contentFilter));
	}

	public void addResponseHeadFilter(String pattern, ProxyFilter headFilter) {
		responseHeadFilters.add(new PatternProxyFilter(pattern, headFilter));
	}

	public void addResponseContentFilter(String pattern,
			ProxyFilter contentFilter) {
		responseContentFilters.add(new PatternProxyFilter(pattern,
				contentFilter));
	}

	public void processRequest() throws IOException {
		RequestContext context = RequestUtil.get();
		String url = context.getRequestURI();
		if (log.isDebugEnabled()) {
			log.debug("处理请求：" + url);
		}
		if (!processProxy(context)) {
			context.setStatus(500, "不支持协议：" + url);
		}
	}

	/**
	 * .jside.org 域名不走代理
	 * 
	 * @param context
	 * @return
	 * @throws IOException
	 */
	protected boolean processProxy(RequestContext context) throws IOException {
		String url = context.getRequestURI();
		if (url.startsWith("http://")) {
			String host = getHost(context);
			if (host.indexOf(".jside.org:") < 0) {
				// TODO:WHY???
				// context.setEncoding(null);
				dispatchServer((RequestContextImpl) context, host, null);
			} else {
				context.dispatch(url.substring('/', "http://".length()));
			}
			return true;
		}
		return false;
	}

	public void execute() throws IOException {
		processProxy(RequestUtil.get());
	}

	public void dispatch(RequestContext context, String path)
			throws IOException {
		dispatch(context, path, null);
	}

	public void dispatch(RequestContext context, String path, String remoteHost)
			throws IOException {
		if (HOST_PATTERN.matcher(path).find()) {
			dispatchServer((RequestContextImpl) context, path, remoteHost);
		} else if (path.startsWith("/")) {
			context.dispatch(path);
		} else {
			dispatchURL((RequestContextImpl) context, path);
		}
	}

	private void dispatchURL(RequestContextImpl context, String path)
			throws IOException {
		URL url = new URL(path);
		if ("http".equals(url.getProtocol())
				|| "https".equals(url.getProtocol())) {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			String method = context.getMethod();
			conn.setRequestMethod(method);
			List<String> headers = context.getRequestHeaders();
			for (String line : headers) {
				int p = line.indexOf(':');
				if (p > 0) {
					conn.addRequestProperty(line.substring(0, p), line
							.substring(p + 1));
				}
			}
			String length = context.getRequestHeader("Content-Length");
			if (length != null) {
				int l = Integer.parseInt(length);
				if (l > 0) {
					printFix(context.getInputStream(),
							(conn.getOutputStream()), l);
				}
			}
			InputStream in = conn.getInputStream();
			Map<String, List<String>> hfs = conn.getHeaderFields();
			for (Map.Entry<String, List<String>> entry : hfs.entrySet()) {
				String key = entry.getKey();
				for (String value : entry.getValue()) {
					context.addResponseHeader(key + ":" + value);
				}
			}
			RequestUtil.printResource(in, conn.getContentType());
			in.close();
		} else {
			RequestUtil.printResource(url, null);
		}
	}

	private void dispatchServer(RequestContextImpl context, String host,
			String remote) throws IOException {
		if (remote == null) {
			remote = host;
		}
		int p = remote.indexOf(':');
		String ip, port;
		if (p > 0) {
			ip = remote.substring(0, p);
			port = remote.substring(p + 1);
		} else {
			ip = remote;
			port = "80";
		}
		Socket socket = new Socket(ip, Integer.parseInt(port));
		InputStream rin = socket.getInputStream();
		OutputStream rout = (socket.getOutputStream());
		List<String> headers = context.getRequestHeaders();
		List<String> result = new ArrayList<String>();
		for (String line : headers) {
			if (line.startsWith("Proxy-")) {
				continue;
			}
			if (line.regionMatches(true, 0, "Host:", 0, 5)) {
				if (host.endsWith(":80")) {
					host = host.substring(0, host.length() - 3);
				}
				line = "Host:" + host;
			}
			result.add(line);
		}
		doSend(context, result, rout);
		doReceive(context, rin);

	}

	private String getHost(RequestContext context) throws IOException {
		String url = context.getRequestURI();
		if (log.isDebugEnabled()) {
			log.debug("处理代理请求：" + url);
		}
		URL resource = new URL(url);
		int port = resource.getPort();
		if (port < 0) {
			port = resource.getDefaultPort();
		}
		return resource.getHost() + ':' + port;
	}

	private void doSend(RequestContext context, List<String> headers,
			OutputStream rout) throws IOException {
		final String url = context.getRequestURI();
		String path = url.startsWith("/") ? url : url.substring(url.indexOf(
				"/", url.indexOf("//") + 2));
		String query = ((RequestContextImpl) context).getQuery();
		if (query != null) {
			path = path + '?' + query;
		}
		String requestLine = context.getMethod() + " " + path + " "
				+ "HTTP/1.1";
		List<PatternProxyFilter> filters = findFilter(url,
				this.requestHeadFilters);
		if (filters != null) {
			headers = doHeadFilter(url, filters, requestLine, headers);
			requestLine = headers.remove(0);
		}

		rout.write(requestLine.getBytes(ISO_8859_1));
		int contentLength = getContentLength(headers);
		for (String line : headers) {
			rout.write(line.getBytes(ISO_8859_1));
		}
		rout.write('\r');
		rout.write('\n');
		rout.flush();
		if (contentLength >= 0) {
			InputStream in = context.getInputStream();
			processContent(context, in, rout,null, this.requestContentFilters, url,
					contentLength);
		}
		rout.flush();
	}

	private int getContentLength(List<String> headers) {
		String contentLength = getHeader(headers, CONTENT_LENGTH);
		int length = -1;
		if (contentLength != null && contentLength.length() > 0) {
			length = Integer.parseInt(contentLength);
		}
		return length;
	}

	private void doReceive(RequestContextImpl context, InputStream rin)
			throws IOException {
		OutputStream out = context.getOutputStream();
		String firstLine = readLine(rin);// rin.readLine();
		if (firstLine != null) {
			String url = context.getRequestURI();
			List<String> headers = readHeaders(rin);
			List<PatternProxyFilter> filters = findFilter(url,
					this.responseHeadFilters);
			if (filters != null) {
				headers = doHeadFilter(context.getRequestURI(), filters,
						firstLine, headers);
				firstLine = headers.remove(0);
			}
			String[] fls = firstLine.split("[\\s]");

			int code = Integer.parseInt(fls[1]);

			context.setStatus(code, fls.length > 2 ? fls[2] : null);

			filters = findFilter(url, this.responseContentFilters);
			int contentLength = getContentLength(headers);
			if (filters == null) {
				for (String line : headers) {
					context.addResponseHeader(line);
				}
				context.setResponseHeader("Connection:close");
				printFix(rin, out, contentLength);

			} else {
				filterReceivedContent(context, filters, rin, out, headers,
						code, contentLength);
			}

		} else {
			log.warn("怎么第一行就错了？？？");
		}
		out.flush();
	}

	private void filterReceivedContent(RequestContextImpl context,
			List<PatternProxyFilter> filters, InputStream rin,
			OutputStream out, List<String> headers, int statusCode,
			int contentLength) throws IOException {

		String method = context.getMethod();
		boolean isGZip = "gzip".equalsIgnoreCase(getHeader(headers,
				"Content-Encoding"));
		boolean isChunked = "chunked".equalsIgnoreCase(getHeader(headers,
				"Transfer-Encoding"));

		for (String line : headers) {//不用gzip，不用chunk
			if ((!isChunked || !line.startsWith("Transfer-Encoding")) && (!isGZip || !line.startsWith("Content-Encoding"))) {
				context.addResponseHeader(line);
			}
		}
		log.debug("返回数据长度：" + method + statusCode + contentLength);
		// 1xx, 204, and 304
		if ("HEAD".equals(method) || "TRACE".equals(method)
				|| statusCode == 304 || statusCode == 204 || statusCode == 205
				|| statusCode < 200) {
		} else {
			if (isChunked) {
				rin = new ChunkedInputStream(rin);
			}
			if (isGZip) {
				rin = new GZIPInputStream(rin);
			}

			//Content-Type	text/html; charset=UTF-8
			String encoding = getHeader(headers, "Content-Type");
			if(encoding != null){
				int p = encoding.indexOf("=");
				if(p>0){
					encoding = encoding.substring(p+1);
				}else{
					encoding = null;
				}
				
			}
			processContent(context, rin, out, encoding, filters, context.getRequestURI(),
					contentLength);
		}
	}

	private void processContent(RequestContext context, InputStream in,
			OutputStream out,String encoding, List<PatternProxyFilter> filters,
			final String url, int contentLength) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		printFix(in, buf, contentLength);
		byte[] data = buf.toByteArray();
		if(encoding == null){
			encoding = ISO_8859_1;
		}
		String content = new String(data, encoding);

		for (PatternProxyFilter pf : filters) {
			try {
				content = pf.filter(content);
			} catch (Exception e) {
				log.error("过滤内容失败", e);
			}
		}

		data = content.getBytes(encoding);
		contentLength = data.length;
		context.setResponseHeader(CONTENT_LENGTH + ":" + contentLength);
		out.write(data);

	}

//	private void write(InputStream in, OutputStream out) throws IOException {
//		byte[] buf = new byte[1024];
//		int c;
//		while ((c = in.read(buf)) >= 0) {
//			out.write(buf, 0, c);
//		}
//	}

	private List<PatternProxyFilter> findFilter(String url,
			ArrayList<PatternProxyFilter> headFilters) {
		ArrayList<PatternProxyFilter> result = null;
		if (headFilters != null) {
			for (PatternProxyFilter pf : headFilters) {
				if (pf.match(url)) {
					if (result == null) {
						result = new ArrayList<PatternProxyFilter>();
					}
					result.add(pf);
				}
			}
		}
		return result;
	}

	private List<String> doHeadFilter(String url,
			List<PatternProxyFilter> headFilters, String requestLine,
			List<String> headers) {
		StringBuilder buf = new StringBuilder(requestLine);
		for (String line : headers) {
			buf.append("\r\n");
			buf.append(line);
		}
		String content = buf.toString();
		for (PatternProxyFilter pf : headFilters) {
			content = pf.filter(content);
		}
		String[] list = content.split("[\\r\\n]+");
		return new ArrayList<String>(Arrays.asList(list));
	}

	private String readLine(InputStream in) {
		StringBuilder buf = new StringBuilder();
		int c;
		try {
			while ((c = in.read()) >= 0) {
				if (c == '\n') {
					return buf.toString();
				} else if (c != '\r') {// r//n
					buf.append((char) c);
				}
			}
		} catch (IOException e) {
			log.error(e);
		}
		throw new RuntimeException("请求异常");
	}

	private List<String> readHeaders(InputStream rin) throws IOException {
		List<String> result = new ArrayList<String>();
		while (true) {
			String line = readLine(rin);// .readLine();
			if (line == null) {
				log.info("连接直接断开了");
				break;
			}
			if (line.length() == 0) {
				break;
			}
			result.add(line);
		}
		return result;
	}

	private void printFix(InputStream in, OutputStream rout, int contentLength)
			throws IOException {
		if (contentLength < 0) {
			readUnFix(in, rout);
		} else {
			int c;
			byte[] buf = new byte[64];
			while ((c = in.read(buf, 0, Math.min(contentLength, buf.length))) >= 0
					&& contentLength > 0) {
				contentLength -= c;
				// System.out.print(new String(buf, 0, c));
				rout.write(buf, 0, c);
			}
			while (contentLength > 0) {
				rout.write(0);
				contentLength--;
			}
		}
	}

	private void readUnFix(InputStream in, OutputStream rout)
			throws IOException {
		while (true) {
			int c = in.read();
			if (c >= 0) {
				rout.write(c);
			} else {
				break;
			}
		}
	}

	// Transfer-Encoding

	private String getHeader(List<String> headers, String key) {
		for (String line : headers) {
			int length = key.length();
			if (line.length() > length && line.charAt(length) == ':'
					&& line.regionMatches(true, 0, key, 0, length)) {
				return line.substring(length + 1).trim();
			}
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		JSideWebServer.getInstance().addAction("http://**",
				ProxyHandler.getInstance());
	}

}
