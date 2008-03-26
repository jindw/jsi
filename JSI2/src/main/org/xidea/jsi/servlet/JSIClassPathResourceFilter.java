package org.xidea.jsi.servlet;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.xidea.jsi.servlet.PreloadFilter;

public class JSIClassPathResourceFilter extends PreloadFilter {

	private ServletContext context;
	private ClassLoader scriptLibs = this.getClass().getClassLoader();

	public void destroy() {
	}

	public void doFilter(ServletRequest req, final ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		String path = request.getRequestURI().substring(
				request.getContextPath().length());
		if (path.startsWith(scriptBase)) {
			path = path.substring(scriptBase.length());
			boolean preload = false;
			if (path.endsWith(PRELOAD_FILE_POSTFIX)) {
				preload = true;
				path = path.replaceFirst(PRELOAD_FILE_POSTFIX + "$", ".js");
			}
			if (path.length() == 0 || path.equals("index.jsp")) {
				preload = true;
				path = req.getParameter("path");
			}
			ServletOutputStream out = resp.getOutputStream();

			InputStream in = context.getResourceAsStream(scriptBase + path);

			if (in == null) {
				in = scriptLibs.getResourceAsStream('/' + path);
			}
			if (in == null) {
				chain.doFilter(req, resp);
				return;
			}
			if (preload) {
				out.print(this.buildPreloadPerfix(path));
				output(in, out);
				out.print(PRELOAD_CONTENT__POSTFIX);
			} else {
				output(in, out);
			}
			in.close();
			return;
		}
		chain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.context = config.getServletContext();
		super.init(config);
		try {
			File dir = new File(context.getResource(scriptBase).getFile());
			final List<URL> result = new ArrayList<URL>();
			FileFilter filter = new FileFilter() {
				public boolean accept(File file) {
					String name = file.getName().toLowerCase();
					if (name.endsWith(".jar") || name.endsWith(".zip")) {
						try {
							result.add(file.toURI().toURL());
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					}
					return false;
				}

			};
			if (dir.exists() && dir.isDirectory()) {
				dir.listFiles(filter);
				dir = new File(dir, "lib");
				if (dir.exists() && dir.isDirectory()) {
					dir.listFiles(filter);
				}
			}
			URL[] urls = result.toArray(new URL[result.size()]);
			scriptLibs = new URLClassLoader(urls, this.getClass()
					.getClassLoader());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}