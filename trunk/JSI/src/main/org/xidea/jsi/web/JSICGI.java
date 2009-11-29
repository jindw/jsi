package org.xidea.jsi.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 该类为方便调试开发，发布时可编译脚本，能后去掉此类。 Servlet 2.4 +
 * 
 * @author jindw
 */
public class JSICGI extends JSIService {
	private static final Pattern QUERY_PATTERN = Pattern
			.compile("([^=&]+)(?:=([^&]+))?");

	private Map<String, String[]> configMap;
	private String path;
	private Map<String, String[]> params = Collections.emptyMap();
	private Map<String, String> envMap;

	public JSICGI(Map<String, String> env) {
		this.envMap = env;
	}

	public Map<String, String[]> parseParams(String query) {
		Matcher matcher = QUERY_PATTERN.matcher(query);
		Map<String, String[]> params = new HashMap<String, String[]>();
		while (matcher.find()) {
			String name = matcher.group(1);
			String value = matcher.group(2);

			String[] values = params.get(name);
			if (values == null) {
				params.put(name, new String[] { decode(value) });
			} else {
				String[] values2 = new String[values.length + 1];
				System.arraycopy(values, 0, values2, 0, values.length);
				values2[values.length] = decode(value);
				params.put(name, values2);
			}
		}
		return params;
	}

	private String decode(String value) {
		try {
			return value == null ? null : URLDecoder.decode(value, this
					.getEncoding());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public void execute() throws IOException {
		try {
			initialize();
		} catch (Exception e) {
			System.out.println("Content-Type:text/plain;charset="
					+ this.getEncoding());
			System.out.println();
			e.printStackTrace(System.out);
			return;
		}
		String contentType = this.getContentType(path, params, "text/html");
		System.out.println("Content-Type:"+contentType);
		System.out.println();
		try {
			this.service(path, params, System.out);
		} catch (Exception e) {
			System.out.println("path:" + path);
			System.out.println("params:" + params);
			e.printStackTrace(System.out);
		}
		//out.close();
	}

	protected void initialize() throws IOException {
		String pathInfo = getenv("PATH_INFO");
		// String documentRoot = getenv("DOCUMENT_ROOT");
		String config = pathInfo.substring(1, pathInfo.indexOf('/', 2));
		this.path = pathInfo.substring(config.length() + 2);
		config = decode(config);
		this.configMap = parseParams(config);

		String method = getenv("REQUEST_METHOD");
		String query;
		if ("POST".equals(method)) {
			query = new BufferedReader(new InputStreamReader(System.in))
					.readLine();
		} else {
			query = getenv("QUERY_STRING");
		}
		params = parseParams(query);

		String scriptBaseFile = getConfig("scriptBase", "scripts/");
		String externalLibraryDirectory = getConfig("externalLibrary",
				"WEB-INF/lib/");
		this.clear();
		this.addBase(new File(scriptBaseFile));
		this.addLib(new File(externalLibraryDirectory));
	}

	private String getConfig(String key, String def) {
		String[] values = configMap.get(key);
		if (values == null) {
			return def;
		} else {
			return values[0];
		}
	}

	private String getenv(String key) {
		return envMap.get(key);
	}

	public static void main(String... args) throws IOException {
		JSICGI cgi = new JSICGI(System.getenv());
		// cgi.set
		cgi.execute();
	}

}
