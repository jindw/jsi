package org.xidea.jsi.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.impl.JSIText;

class JSIResourceLoader {
	private static final Log log = LogFactory.getLog(JSIResourceLoader.class);
	/**
	 * 只有默认的encoding没有设置的时候，才会设置
	 */
	protected String encoding = "utf-8";

	protected String scriptBase;
	protected File scriptBaseDirectory;
	protected File externalLibraryDirectory;

	public void setScriptBase(String scriptBase) {
		this.scriptBase = scriptBase;
	}

	public void setScriptBaseDirectory(File scriptBaseFile) {
		this.scriptBaseDirectory = scriptBaseFile;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setExternalLibraryDirectory(File externalLibraryFilr) {
		this.externalLibraryDirectory = externalLibraryFilr;
	}

	protected boolean output(String path, Writer out) throws IOException {
		char[] buf = new char[1024];
		InputStream in = this.getResourceStream(path);
		if (in == null) {
			return false;
		} else {
			InputStreamReader reader = new InputStreamReader(in, this.encoding);
			int len = reader.read(buf);
			while (len > 0) {
				out.write(buf, 0, len);
				len = reader.read(buf);
			}
			return true;
		}
	}

	protected boolean output(String path, OutputStream out) throws IOException {
		InputStream in = this.getResourceStream(path);
		if (in == null) {
			return false;
		} else {
			byte[] buf = new byte[1024];
			int len = in.read(buf);
			while (len > 0) {
				out.write(buf, 0, len);
				len = in.read(buf);
			}
			return true;
		}
	}

	/**
	 * 打开的流使用完成后需要自己关掉
	 */
	protected InputStream getResourceStream(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		File file = new File(this.scriptBaseDirectory, path);
		if (file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				log.debug(e);
				;
			}
		}
		File[] list = this.scriptBaseDirectory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xml");
			}
		});
		if (list != null) {
			int i = list.length;
			while (i-- > 0) {
				InputStream in = findByXML(list[i], path);
				if (in != null) {
					return in;
				}
			}
		}
		if (this.externalLibraryDirectory != null) {
			list = this.externalLibraryDirectory
					.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							name = name.toLowerCase();
							return name.endsWith(".jar")
									|| name.endsWith(".zip");
						}
					});
			if (list != null) {
				int i = list.length;
				while (i-- > 0) {
					InputStream in = findByJAR(list[i], path);
					if (in != null) {
						return in;
					}
				}
			}
		}
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}

	private InputStream findByJAR(File file, String path) {
		try {
			ZipFile jarFile = new ZipFile(file);
			ZipEntry ze = jarFile.getEntry(path);
			if (ze != null) {
				return jarFile.getInputStream(ze);
			}
		} catch (IOException e) {
		} finally {
		}
		return null;
	}

	private InputStream findByXML(File file, String path) {
		Properties ps = new Properties();
		try {
			ps.loadFromXML(new FileInputStream(file));
			String value = ps.getProperty(path);
			if (value != null) {
				byte[] data = value.getBytes(encoding);
				return new ByteArrayInputStream(data);
			} else {
				value = ps.getProperty(path + "#base64");
				if (value != null) {
					byte[] data = new sun.misc.BASE64Decoder()
							.decodeBuffer(value);
					return new ByteArrayInputStream(data);
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
}