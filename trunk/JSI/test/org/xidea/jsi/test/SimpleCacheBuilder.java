package org.xidea.jsi.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.xidea.el.json.JSONEncoder;

public class SimpleCacheBuilder {
	private static String packageBase = "D:/workspace/JSI2/web/scripts";
	private static String destFile = "D:/workspace/JSI2/build/jsidoc-source.js";

	public static void main(String[] args) throws IOException {
		if(args!=null && args.length>=2){
			packageBase = args[0];
			destFile = args[1];
		}
		List<String> packages = new ArrayList<String>();
		packages.add("");
		packages.add("example");
		packages.add("example.alias");
		packages.add("example.dependence");
		packages.add("example.internal");
		
		genPackageCache(new PrintStream(destFile,"utf-8"),packages);
	}

	private static void genPackageCache(PrintStream out,List<String> packages) throws IOException {
		for (String pkg : packages) {
			File dir = new File(packageBase, pkg.replace('.', '/'));
			//System.out.println(dir);
			File[] files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".js");
				}
			});
			if (files!=null && files.length > 0) {
				//System.out.println(pkg);

				out.println("$JSI.preload(");
				out.println("'" + pkg + "',{");
				for (int j = 0; j < files.length; j++) {
					File file = files[j];
					String fileName = file.getName();
					if(j>0){
						out.print(",");
					}
					out.print("'");
					out.print(fileName.equals("__package__.js")?"":fileName);
					out.print("':");
					out.print(JSONEncoder.encode(getFileContent(file)));
				}
				out.println("});\n");
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
}
