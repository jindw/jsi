package org.xidea.jsi.doc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.xidea.jsi.impl.FileJSIRoot;
import org.xidea.jsi.impl.JSIUtil;

public class SimpleCacheBuilder {
	private static String packageBase = "D:/workspace/JSI2/web/scripts";
	private static String destBase = "D:/workspace/JSI2/build";

	public static void main(String[] args) throws IOException {
		if(args!=null && args.length>=2){
			packageBase = args[0];
			destBase = args[1];
		}
		List<String> packages = JSIUtil.findPackageList(new File(packageBase));
		packages.add("org.xidea.jsidoc.html");
		packages.add("org.xidea.jsidoc.styles");
		packages.add("");
		
		genPackageCache(new PrintStream(new File(destBase,"jsidoc-source.js"),"utf-8"),packages);
	}

	private static void genPackageCache(PrintStream out,List<String> packages) throws IOException {
		out.println("JSIDoc.cacheScript({");
		boolean firstPackage = true;
		for (String pkg : packages) {
			File dir = new File(packageBase, pkg.replace('.', '/'));
			//System.out.println(dir);
			File[] files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".js") || name.endsWith(".xhtml");
				}
			});
			if (files!=null && files.length > 0) {
				//System.out.println(pkg);
				if(firstPackage){
					firstPackage = false;
				}else{
					out.print(",");
				}
				out.println("'" + pkg + "':{");
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
				out.println("}");
			}
		}
		out.println("})");
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
