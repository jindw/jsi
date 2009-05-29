package org.xidea.jsi.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.impl.AbstractRoot;

public class JSIResourceLoader extends AbstractRoot{
	private static final Log log = LogFactory.getLog(JSIResourceLoader.class);
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

	public byte[] getResourceAsBinary(String path) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			if (this.output(path, out)) {
				return out.toByteArray();
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public String loadText(String pkgName, String scriptName) {
		if(pkgName!=null && pkgName.length()>0){
			scriptName = pkgName.replace('.', '/')+'/'+scriptName;
		}
		return getResourceAsString(scriptName);
	}
	public String getResourceAsString(String path) {
		StringWriter out = new StringWriter();
		try {
			if (this.output(path, out)) {
				return out.toString();
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	/**
	 * 输出指定资源，如果该资源存在，返回真
	 * @param path
	 * @param out
	 * @return
	 * @throws IOException
	 */
	protected boolean output(String path, Writer out) throws IOException {
		char[] buf = new char[1024];
		InputStream in = this.getResourceStream(path);

		if (in == null) {
			return false;
		} else {
			try {
				InputStreamReader reader = new InputStreamReader(in,
						this.encoding);
				int len = reader.read(buf);
				while (len > 0) {
					out.write(buf, 0, len);
					len = reader.read(buf);
				}
				return true;
			} finally {
				in.close();
			}
		}
	}

	/**
	 * 输出指定资源，如果该资源存在，返回真
	 * @param path
	 * @param out
	 * @return
	 * @throws IOException
	 */
	protected boolean output(String path, OutputStream out) throws IOException {
		InputStream in = this.getResourceStream(path);
		if (in == null) {
			return false;
		} else {
			try {
				byte[] buf = new byte[1024];
				int len = in.read(buf);
				while (len > 0) {
					out.write(buf, 0, len);
					len = in.read(buf);
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
			}
		}
		File[] list = this.scriptBaseDirectory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xml");
			}
		});
		if (list != null) {
			for (File item : list) {
				InputStream in = findByXML(item, path);
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
				for (File item : list) {
					InputStream in = findByZip(item, path);
					if (in != null) {
						return in;
					}
				}
			}
		}
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}

	private InputStream findByZip(File file, String path) {
		try {
			final ZipFile jarFile = new ZipFile(file);
			ZipEntry ze = jarFile.getEntry(path);
			if (ze != null) {
				return new FilterInputStream(jarFile.getInputStream(ze)) {
					public void close() throws IOException {
						super.close();
						jarFile.close();
					}
				};
			}
		} catch (IOException e) {
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