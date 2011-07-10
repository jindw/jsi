package org.jside.jsi.tools.export;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xidea.jsi.JSIPackage;

class ExportText {
	private static final Pattern DOCUMENT_HEADER_PATTERN = Pattern
			.compile("\\s*(?:/\\*[\\s\\S]*?\\*/\\s*|//.*$\\s*)*");
	static final String PACKAGE_PATH_POSTFIX = "/"
			+ JSIPackage.PACKAGE_FILE_NAME;

	public static String loadText(InputStream in, String encoding) {
		try {
			Reader reader = new InputStreamReader(in, encoding);
			StringBuilder buf = new StringBuilder();
			char[] cbuf = new char[1024];
			for (int len = reader.read(cbuf); len > 0; len = reader.read(cbuf)) {
				buf.append(cbuf, 0, len);
			}
			return buf.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	static String findDocumentHeader(String content) {
		Matcher mateher = DOCUMENT_HEADER_PATTERN.matcher(content);
		if (mateher.find()) {
			return content.substring(0, mateher.end());
		} else {
			return "";
		}
	}

	static void write(String source, OutputStream out, String encoding)
			throws IOException {
		out.write(source.getBytes(encoding));
		out.flush();
		out.close();
	}

	static String toPackageName(String path) {
		int p = path.lastIndexOf('/');
		if(p<0){
			p = path.lastIndexOf(':');
			if(p<0){
				p = path.lastIndexOf('.');
			}
		}
		return path.substring(0, p).replace('/','.');
	}

	static String toPackagePath(String path) {
		return toPackageName(path).replace('.', '/') + PACKAGE_PATH_POSTFIX;
	}

}
