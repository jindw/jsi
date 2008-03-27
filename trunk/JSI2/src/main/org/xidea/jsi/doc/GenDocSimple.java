package org.xidea.jsi.doc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

public class GenDocSimple {
	static String packageBase = "D:/workspace/JSI2/web/scripts";
	static String destBase = "D:/workspace/JSI2/build";
	static String packages[] = { "","org.xidea.jsidoc", "org.xidea.jsidoc.export",
			"org.xidea.jsidoc.html", "org.xidea.jsidoc.styles",
			"org.xidea.sandbox.io", "org.xidea.sandbox.util",
			"org.xidea.sandbox.xml", "org.xidea.syntax",
			"org.xidea.test.loader" };

	public static void main(String[] args) throws IOException {
		genPackageCache(new PrintStream(new File(destBase,"source.js"),"utf-8"));
		genBoot(new PrintStream(new File(destBase,"boot.js"),"utf-8"));

	}

	private static void genBoot(PrintStream out) throws IOException {
		out.println(getFileContent( new File(packageBase, "boot.js")));
		out.println(getFileContent( new File(packageBase, "boot-core.js")));
		out.println(getFileContent( new File(packageBase, "boot-log.js")));
	}

	private static void genPackageCache(PrintStream out) throws IOException {
		for (int i = 0; i < packages.length; i++) {
			String pkg = packages[i];
			File dir = new File(packageBase, pkg.replace('.', '/'));
			//System.out.println(dir);
			File[] files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".js") || name.endsWith(".xhtml");
				}
			});
			if (files!=null && files.length > 0) {
				//System.out.println(pkg);
				out.println("$JSI.preload('" + pkg + "',{");
				for (int j = 0; j < files.length; j++) {
					File file = files[j];
					String fileName = file.getName();
					if(j>0){
						out.print(",");
					}
					out.print("'");
					out.print(fileName.equals("__package__.js")?"":fileName);
					out.print("':'");
					out.print(escapeString(getFileContent(file),'\''));

					out.print("'");
				}
				out.println("})");
			}
		}
		out.flush();
		out.close();
	}

	static String getFileContent(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int len;
		byte[] bufBytes = new byte[1024];
		while (-1 != (len = in.read(bufBytes, 0, bufBytes.length))) {
			buffer.write(bufBytes, 0, len);
		}
		byte[] data = buffer.toByteArray();
		in.close();
		return new String(data, "utf-8");
	}

	/**
	 * For escaping strings printed by object and array literals; not quite the
	 * same as 'escape.'
	 */
	public static String escapeString(String s, char escapeQuote) {
		if (!(escapeQuote == '"' || escapeQuote == '\'')) {
			throw new RuntimeException();
		}
		StringBuffer sb = null;

		for (int i = 0, L = s.length(); i != L; ++i) {
			int c = s.charAt(i);

			if (' ' <= c && c <= '~' && c != escapeQuote && c != '\\') {
				// an ordinary print character (like C isprint()) and not "
				// or \ .
				if (sb != null) {
					sb.append((char) c);
				}
				continue;
			}
			if (sb == null) {
				sb = new StringBuffer(L + 3);
				sb.append(s);
				sb.setLength(i);
			}

			int escape = -1;
			switch (c) {
			case '\b':
				escape = 'b';
				break;
			case '\f':
				escape = 'f';
				break;
			case '\n':
				escape = 'n';
				break;
			case '\r':
				escape = 'r';
				break;
			case '\t':
				escape = 't';
				break;
			case 0xb:
				escape = 'v';
				break; // Java lacks \v.
			case ' ':
				escape = ' ';
				break;
			case '\\':
				escape = '\\';
				break;
			}
			if (escape >= 0) {
				// an \escaped sort of character
				sb.append('\\');
				sb.append((char) escape);
			} else if (c == escapeQuote) {
				sb.append('\\');
				sb.append(escapeQuote);
			} else {
				int hexSize;
				if (c < 256) {
					// 2-digit hex
					sb.append("\\x");
					hexSize = 2;
				} else {
					// Unicode.
					sb.append("\\u");
					hexSize = 4;
				}
				// append hexadecimal form of c left-padded with 0
				for (int shift = (hexSize - 1) * 4; shift >= 0; shift -= 4) {
					int digit = 0xf & (c >> shift);
					int hc = (digit < 10) ? '0' + digit : 'a' - 10 + digit;
					sb.append((char) hc);
				}
			}
		}
		return (sb == null) ? s : sb.toString();
	}
}
