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
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIPackage;

public class ResourceRoot extends AbstractRoot {
	private static final File[] EMPTY_FILES = {};
	private static final Log log = LogFactory.getLog(ResourceRoot.class);

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
		if(base.isDirectory()){
			sources.add(base);
		}else{
			throw new IllegalArgumentException("jsi source must be a directory");
		}
	}

	public void addLib(File lib) {
		libraries.add(lib);
	}

	public long getLastModified() {
		long t =0;
		for (File base : sources) {
			t = Math.max(t, base.lastModified());
		}
		for (File base : libraries) {
			t = Math.max(t, base.lastModified());
		}
		return t;
	}
	public JSILoadContext $import(String  path, JSILoadContext context) {
		long t = getLastModified();
		if(token<t){
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


	public List<JSIPackage> getPackageObjectList() {
		List<String> result = findPackageList(true);
		LinkedHashSet<JSIPackage> ps = new LinkedHashSet<JSIPackage>();
		for (String path : result) {
			try {
				ps.add(requirePackage(path));
			} catch (Exception e) {
			}
		}
		return new ArrayList<JSIPackage>(ps);
	}

	public List<String> findPackageList(boolean findLib) {
		List<String> result = new ArrayList<String>();
		for (File file : sources) {
			try {
				if (file.isFile()) {
					appendZipPackage(file, result);
				} else {
					result.addAll(FileRoot.findPackageList(file));
				}
			} catch (Exception e) {
			}

		}
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
	 * 打开的流使用完成后需要自己关掉
	 */
	public URL getResource(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		URL res = findResource(path);

		if(res == null){
			res = getDefaultResource(path);
		}
		return res;
	}
	protected URL getDefaultResource(String path) {
		return this.getClass().getClassLoader().getResource(path);
	}

	protected URL findResource(String path) {
		for (File base : sources) {
			URL in = findResource(base, path);
			if (in != null) {
				return in;
			}
		}
		for (File resource : libraries) {
			File[] libs = findLibFiles(resource);
			for (File item : libs) {
				URL in = findResource(item, path);
				if (in != null) {
					return in;
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
			if(lib.isFile()){
				return new File[]{lib};
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
		try{
			if(file.isDirectory()){
				file = new File(file, path);
				if (file.exists()
						&& (!"boot.js".equals(path) || file.length() > 200)) {
					return file.toURI().toURL();
				}else{
					return null;
				}
			}else{
				return findByZip(file, path);
			}
		} catch (IOException e) {
			log.debug(e);
			return null;
		}
	}
	private static URL findByZip(File file, String path) throws IOException{
		final ZipFile jarFile = new ZipFile(file);
		ZipEntry ze = jarFile.getEntry(path);
		if (ze != null) {
			return new URL("jar", "", file.toURI().toURL() + "!/"
					+ path);
		}else{
			return null;
		}
	}

	private static void appendZipPackage(File file, Collection<String> result) {
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