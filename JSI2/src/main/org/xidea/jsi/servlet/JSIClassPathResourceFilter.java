package org.xidea.jsi.servlet;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * 该类为方便调试开发，发布时可编译脚本，能后去掉此类。
 * 
 * @author jindw
 */
public class JSIClassPathResourceFilter extends PreloadFilter {

	protected ClassLoader scriptLibs = this.getClass().getClassLoader();

	public void destroy() {
	}

	protected InputStream getResourceStream(String path) {
		InputStream in = context.getResourceAsStream(scriptBase + path);
		if (in == null) {
			in = scriptLibs.getResourceAsStream(path);
		}
		return in;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		super.init(config);
		File dir = new File(context.getRealPath(scriptBase));
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
		scriptLibs = new URLClassLoader(urls, this.getClass().getClassLoader());
	}

}