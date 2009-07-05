package org.xidea.jsi.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIPackage;

public class ResourceRoot extends AbstractRoot {
	private static final File[] EMPTY_FILES = {};
	private static final Log log = LogFactory.getLog(ResourceRoot.class);

	/**
	 * 只有默认的encoding没有设置的时候，才会设置
	 */
	private String encoding = "utf-8";

	private File scriptBaseDirectory;
	private File externalLibraryDirectory;

	public void setScriptBaseDirectory(File scriptBaseFile) {
		this.scriptBaseDirectory = scriptBaseFile;
	}

	public File getScriptBaseDirectory() {
		return scriptBaseDirectory;
	}

	public File getExternalLibraryDirectory() {
		return externalLibraryDirectory;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public void setExternalLibraryDirectory(File externalLibraryFilr) {
		this.externalLibraryDirectory = externalLibraryFilr;
	}

	@Override
	public String loadText(String pkgName, final String scriptName) {
		String path = scriptName;
		if (pkgName != null && pkgName.length() > 0) {
			path = pkgName.replace('.', '/') + '/' + scriptName;
		}
		return getResourceAsString(path);
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
		InputStream in = this.getResource(path).openStream();
		if (in == null) {
			return false;
		} else {
			try {
				if (isPreload) {
					out.write(JSIText.buildPreloadPerfix(path).getBytes(
							this.getEncoding()));
				}
				byte[] buf = new byte[1024];
				int len = in.read(buf);
				while (len > 0) {
					out.write(buf, 0, len);
					len = in.read(buf);
				}
				if (isPreload) {
					out.write(JSIText.buildPreloadPostfix("//").getBytes(
							this.getEncoding()));
				}
				return true;
			} finally {
				in.close();
			}
		}
	}

	/**
	 * 打开的流使用完成后需要自己关掉
	 */
	public URL getResource(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		File file = new File(this.scriptBaseDirectory, path);
		if (file.exists() && (!"boot.js".equals(path) || file.length() > 200)) {
			try {
				return file.toURI().toURL();
			} catch (IOException e) {
				log.debug(e);
			}
		}
		File[] libs = findLibFiles(this.scriptBaseDirectory);
		for (File item : libs) {
			URL in = findByZip(item, path);
			if (in != null) {
				return in;
			}
		}
		libs = findLibFiles(this.externalLibraryDirectory);
		for (File item : libs) {
			URL in = findByZip(item, path);
			if (in != null) {
				return in;
			}
		}
		return this.getClass().getClassLoader().getResource(path);
	}

	public List<JSIPackage> getPackageObjectList() {
		final List<String> result = FileRoot
				.findPackageList(this.scriptBaseDirectory);
		File[] libs = findLibFiles(this.scriptBaseDirectory);
		for (File lib : libs) {
			appendZipPackage(lib, result);
		}
		libs = findLibFiles(this.externalLibraryDirectory);
		for (File lib : libs) {
			appendZipPackage(lib, result);
		}

		LinkedHashSet<JSIPackage> ps = new LinkedHashSet<JSIPackage>();
		for (String path : result) {
			try {
				ps.add(requirePackage(path));
			} catch (Exception e) {
			}
		}
		return new ArrayList<JSIPackage>(ps);
	}

	/**
	 * 放心吧，我们不返回空：）
	 * 
	 * @param lib
	 * @return
	 */
	protected File[] findLibFiles(File lib) {
		if (lib != null) {
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

	protected URL findByZip(File file, String path) {
		URL resource = null;
		try {
			final ZipFile jarFile = new ZipFile(file);
			ZipEntry ze = jarFile.getEntry(path);
			if (ze != null) {
				resource = new URL("jar", "", file.toURI().toURL() + "!/"
						+ path);
			}
		} catch (IOException e) {
			log.debug(e);
		}
		return resource;
	}

	private void appendZipPackage(File file, Collection<String> result) {
		try {
			final ZipFile jarFile = new ZipFile(file);
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

		} catch (IOException e) {
		}
	}
}