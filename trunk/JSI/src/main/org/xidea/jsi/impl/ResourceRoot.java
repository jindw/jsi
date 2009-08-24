package org.xidea.jsi.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
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

	protected ArrayList<URL> scriptBases = new ArrayList<URL>();
	protected ArrayList<File> libraries = new ArrayList<File>();

	public void addScriptBase(URL base) {
		scriptBases.add(base);
	}

	protected void clear() {
		this.scriptBases.clear();
		this.libraries.clear();
	}

	public void addLib(File base) {
		if (base.isDirectory()) {
			libraries.add(base);
		} else {
			try {
				scriptBases.add(base.toURI().toURL());
			} catch (MalformedURLException e) {
				log.warn(e);
			}
		}
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
		URL url = this.getResource(path);
		InputStream in = url == null ? null : url.openStream();
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
		for (URL resource : scriptBases) {
			try {
				resource = new URL(resource, path);
				File file = toFile(resource);
				if (file != null) {
					if (file.exists()
							&& (!"boot.js".equals(path) || file.length() > 200)) {
						return resource;
					}
				} else {
					// HTTP
					if(resource.getProtocol().equals("jar")){
						String fp = resource.getFile();
						int p = fp.indexOf('!');
						File jar = new File(URLDecoder.decode(fp.substring(0,p),"UTF-8"));
						resource = findByZip(jar, fp.substring(p+1));
						if(resource!=null){
							return resource;
						}
					}
				}
			} catch (IOException e) {
				log.debug(e);
			}
		}

		for (File resource : libraries) {
			File[] libs = findLibFiles(resource);
			for (File item : libs) {
				URL in = findByZip(item, path);
				if (in != null) {
					return in;
				}
			}
		}

		return this.getClass().getClassLoader().getResource(path);
	}

	private File toFile(URL resource) throws UnsupportedEncodingException {
		if (resource.getProtocol().equals("file")) {
			return new File(URLDecoder.decode(resource.getFile(), "UTF-8"));
		}
		return null;
	}

	public List<JSIPackage> getPackageObjectList() {
		List<String> result = getPackageList(true);
		LinkedHashSet<JSIPackage> ps = new LinkedHashSet<JSIPackage>();
		for (String path : result) {
			try {
				ps.add(requirePackage(path));
			} catch (Exception e) {
			}
		}
		return new ArrayList<JSIPackage>(ps);
	}

	protected List<String> getPackageList(boolean findLib) {
		List<String> result = new ArrayList<String>();
		for (URL resource : scriptBases) {
			try {
				File file = toFile(resource);
				if (file != null) {
					result.addAll(FileRoot.findPackageList(file));
				}
			} catch (Exception e) {
			}

		}
		;
		if (findLib) {
			for (File resource : libraries) {
				File[] libs = findLibFiles(resource);
				for (File lib : libs) {
					appendZipPackage(lib, result);
				}
			}
		}
		return result;
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