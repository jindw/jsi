package org.xidea.jsi.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIPackage;

public class ResourceRoot extends AbstractRoot {
	private static final File[] EMPTY_FILES = {};
	private static final Log log = LogFactory.getLog(ResourceRoot.class);
	protected ClassLoader loader = ResourceRoot.class.getClassLoader();

	/**
	 * 只有默认的encoding没有设置的时候，才会设置
	 */
	private String encoding = "utf-8";

	protected ArrayList<File> sources = new ArrayList<File>();
	protected ArrayList<File> libraries = new ArrayList<File>();
	protected long token = 0;

	protected void reset() {
		this.sources.clear();
		this.libraries.clear();
		super.reset();
	}

	public void addSource(File base) {
		if (!base.isFile()) {// isDir or not exist
			sources.add(base);
		} else {
			throw new IllegalArgumentException("jsi source must be a directory");
		}
	}

	public void addLib(File lib) {
		libraries.add(lib);
	}

	public long getLastModified() {
		long t = 0;
		for (File base : sources) {
			t = Math.max(t, base.lastModified());
		}
		for (File base : libraries) {
			t = Math.max(t, base.lastModified());
		}
		return t;
	}

	public JSILoadContext $import(String path, JSILoadContext context) {
		long t = getLastModified();
		if (token < t) {
			super.reset();
			token = t;
		}
		return super.$import(path, context);
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return this.encoding;
	}

	@Override
	public String loadText(String pkgName, final String scriptName) {
		String path = scriptName;
		if (pkgName != null && pkgName.length() > 0) {
			path = toPath(pkgName, scriptName);
		}
		return getResourceAsString(path);
	}

	private String toPath(String pkgName, final String scriptName) {
		return pkgName.replace('.', '/') + '/' + scriptName;
	}

	public String getResourceAsString(String path) {
		StringWriter out = new StringWriter();
		try {
			if (this.output(path, out, false)) {
				return out.toString();
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 输出指定资源，如果该资源存在，返回真
	 * 
	 * @param path
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public boolean output(String path, Writer out, boolean isPreload)
			throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		if (this.output(path, buf, isPreload)) {
			out.write(new String(buf.toByteArray(), this.encoding));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 输出指定资源，如果该资源存在，返回真
	 * 
	 * @param path
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public boolean output(String path, OutputStream out, boolean isPreload)
			throws IOException {
		URL url = this.getResource(path);
		InputStream in = url == null ? null : url.openStream();
		if (in == null) {
			return false;
		} else {
			try {
				if (isPreload) {
					out.write(JSIText.buildPreloadPerfix(path).getBytes(
							this.encoding));
				}
				byte[] buf = new byte[1024];
				int len = in.read(buf);
				while (len > 0) {
					out.write(buf, 0, len);
					len = in.read(buf);
				}
				if (isPreload) {
					out.write(JSIText.buildPreloadPostfix("//").getBytes(
							this.encoding));
				}
				return true;
			} finally {
				in.close();
			}
		}
	}

	public List<JSIPackage> getPackageObjectList() {
		List<String> result = findPackageList(true);
		LinkedHashSet<JSIPackage> ps = new LinkedHashSet<JSIPackage>();
		for (String path : result) {
			try {
				ps.add(requirePackage(path));
			} catch (Exception e) {
				log.warn(e);
			}
		}
		return new ArrayList<JSIPackage>(ps);
	}

	public List<String> getPackageFileList(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		List<URL> res = getResources(path);
		try {
			List<URL> res2 = Collections.list(loader.getResources(path));
			res.addAll(res2);
		} catch (IOException e1) {
			log.warn(e1);
		}

		ArrayList<String> result = new ArrayList<String>();
		for (URL item : res) {
			try {
				this.append(item, result);
			} catch (Exception e) {
				log.debug(e);
			}
		}

		return result;
	}

	private void append(URL item, final List<String> result)
			throws URISyntaxException, IOException {
		if (item.getProtocol().equals("file")) {
			new File(item.toURI()).listFiles(new FileFilter() {
				public boolean accept(File file) {
					String name = file.getName();
					if (file.isFile() && name.endsWith(".js")
							&& !result.contains(name)) {
						result.add(name);
					}
					return false;
				}
			});
		} else if (item.getProtocol().equals("jar")) {
			JarURLConnection jarCon = (JarURLConnection) item.openConnection();
			JarFile jarFile = jarCon.getJarFile();
			Enumeration<JarEntry> en = jarFile.entries();
			String name = jarCon.getJarEntry().getName();
			while (en.hasMoreElements()) {
				JarEntry jarEntry = (JarEntry) en.nextElement();
				String name2 = jarEntry.getName();
				if (name2.startsWith(name)) {
					name2 = name2.substring(name.length());
					if (name2.indexOf('/') < 0 && name2.endsWith(".js")
							&& !result.contains(name2)) {
						result.add(name2);
					}
				}
			}
			jarFile.close();
		}
	}

	public List<String> findPackageList(boolean findLib) {
		List<String> result = new ArrayList<String>();
		for (File file : sources) {
			try {
				if (file.exists()) {
					if (file.isFile()) {
						appendZipPackage(file, result);
					} else {
						result.addAll(FileRoot.findPackageList(file));
					}
				}
			} catch (Exception e) {
				log.warn(e);
			}

		}
		if (findLib) {
			for (File resource : libraries) {
				if (resource.exists()) {
					File[] libs = findLibFiles(resource);
					for (File lib : libs) {
						if (lib.exists()) {
							appendZipPackage(lib, result);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * 打开的流使用完成后需要自己关掉
	 */
	public URL getResource(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		URL res = findResource(path, null);

		if (res == null) {
			res = loader.getResource(path);
		}
		return res;
	}

	public List<URL> getResources(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		ArrayList<URL> result = new ArrayList<URL>();
		findResource(path, result);
		try {
			result.addAll(Collections.list(loader.getResources(path)));
		} catch (IOException e) {
			log.warn(e);
		}

		return result;
	}
//
//	// URL item = findResource(path);
//	protected URL getDefaultResource(String path) {
//		return loader.getResource(path);
//	}

	protected URL findResource(String path, Collection<URL> result) {
		for (File base : sources) {
			URL in = findResource(base, path);
			if (in != null) {
				if (result == null) {
					return in;
				} else {
					result.add(in);
				}
			}
		}
		for (File resource : libraries) {
			File[] libs = findLibFiles(resource);
			for (File item : libs) {
				URL in = findResource(item, path);
				if (in != null) {
					if (result == null) {
						return in;
					} else {
						result.add(in);
					}
				}
			}
		}
		return null;
	}

	/**
	 * 放心吧，我们不返回空：）
	 * 
	 * @param lib
	 * @return
	 */
	private static File[] findLibFiles(File lib) {
		if (lib != null) {
			if (lib.isFile()) {
				return new File[] { lib };
			}
			File[] result = lib.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					name = name.toLowerCase();
					return name.endsWith(".jar") || name.endsWith(".zip");
				}
			});
			if (result != null) {
				return result;
			}
		}
		return EMPTY_FILES;
	}

	private static URL findResource(File file, String path) {
		try {
			if (file.isDirectory()) {
				file = new File(file, path);
				if (file.exists()
						&& (!"boot.js".equals(path) || file.length() > 200)) {
					return file.toURI().toURL();
				} else {
					return null;
				}
			} else {
				return findByZip(file, path);
			}
		} catch (IOException e) {
			log.debug(e);
			return null;
		}
	}

	private static URL findByZip(File file, String path) throws IOException {
		final ZipFile jarFile = new ZipFile(file);
		ZipEntry ze = jarFile.getEntry(path);
		try{
			if (ze != null) {
				return new URL("jar", "", file.toURI().toURL() + "!/" + path);
			} else {
				return null;
			}
		}finally{
			jarFile.close();
		}
	}

	private static void appendZipPackage(File file, Collection<String> result) {
		try {
			final ZipFile jarFile = new ZipFile(file);
			try{
				Enumeration<? extends ZipEntry> ze = jarFile.entries();
				while (ze.hasMoreElements()) {
					ZipEntry zipEntry = ze.nextElement();
					String name = zipEntry.getName();
					if (name.endsWith(JSIPackage.PACKAGE_FILE_NAME)) {
						name = name.substring(0, name.lastIndexOf('/'));
						if (name.startsWith("/")) {
							name = name.substring(1);
						}
						result.add(name.replace('/', '.'));
					}
				}
			} finally {
				if(jarFile != null){
					jarFile.close();
				}
			}
		} catch (IOException e) {
			log.warn(e);
		}
	}
}