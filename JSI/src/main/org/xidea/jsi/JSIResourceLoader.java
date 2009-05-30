package org.xidea.jsi;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.impl.AbstractRoot;
import org.xidea.jsi.impl.FileRoot;


public class JSIResourceLoader extends AbstractRoot {
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
			if (this.output(path, out, null, null)) {
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
		if (pkgName != null && pkgName.length() > 0) {
			scriptName = pkgName.replace('.', '/') + '/' + scriptName;
		}
		return getResourceAsString(scriptName);
	}

	public String getResourceAsString(String path) {
		StringWriter out = new StringWriter();
		try {
			if (this.output(path, out, null, null)) {
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
	 * 
	 * @param path
	 * @param out
	 * @return
	 * @throws IOException
	 */
	protected boolean output(String path, Writer out, String prefix,
			String postfix) throws IOException {
		char[] buf = new char[1024];
		InputStream in = this.getResourceStream(path);

		if (in == null) {
			return false;
		} else {
			try {
				InputStreamReader reader = new InputStreamReader(in,
						this.encoding);
				int len = reader.read(buf);
				if (prefix != null) {
					out.write(prefix);
				}
				while (len > 0) {
					out.write(buf, 0, len);
					len = reader.read(buf);
				}
				if (postfix != null) {
					out.write(postfix);
				}
				return true;
			} finally {
				in.close();
			}
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
	protected boolean output(String path, OutputStream out, byte[] prefix,
			byte[] postfix) throws IOException {
		InputStream in = this.getResourceStream(path);
		if (in == null) {
			return false;
		} else {
			try {
				byte[] buf = new byte[1024];
				int len = in.read(buf);
				if (prefix != null) {
					out.write(prefix);
				}
				while (len > 0) {
					out.write(buf, 0, len);
					len = in.read(buf);
				}
				if (postfix != null) {
					out.write(postfix);
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
		File[] libs = findLibFiles();
		if (libs != null) {
			for (File item : libs) {
				InputStream in = findByZip(item, path);
				if (in != null) {
					return in;
				}
			}
		}
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}
	
	public List<JSIPackage> getPackageObjectList(){
		final List<String> result = FileRoot.findPackageList(this.scriptBaseDirectory);
		File[] libs = findLibFiles();
		if(libs != null){
			for(File lib : libs){
				appendZipPackage(lib, result);
			}
		}
		LinkedHashSet<JSIPackage> ps = new LinkedHashSet<JSIPackage>();
		for (String path:result) {
			try{
				ps.add(requirePackage(path, true));
			}catch (Exception e) {
			}
		}
		return new ArrayList<JSIPackage>(ps);
	}

	

	private File[] findLibFiles() {
		if (this.externalLibraryDirectory != null) {
			return this.externalLibraryDirectory
					.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							name = name.toLowerCase();
							return name.endsWith(".jar")
									|| name.endsWith(".zip");
						}
					});
		}
		return null;
	}

	private void appendZipPackage(File file, Collection<String> result) {
		try {
			final ZipFile jarFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> ze = jarFile.entries();
			while (ze.hasMoreElements()) {
				ZipEntry zipEntry = ze.nextElement();
				String name = zipEntry.getName();
				if(name.endsWith(JSIPackage.PACKAGE_FILE_NAME)){
					name = name.substring(0,name.lastIndexOf('/'));
					if(name.startsWith("/")){
						name = name.substring(1);
					}
					result.add(name.replace('/', '.'));
				}
			}
			
		} catch (IOException e) {
		}
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
}