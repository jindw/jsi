package org.xidea.jsi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.PackageParser;
import org.xidea.jsi.UnsupportedSyntaxException;

public class ScriptPackagePaser implements PackageParser {

	public static final ScriptEngine engine = new javax.script.ScriptEngineManager()
			.getEngineByExtension("js");

	private static final String BIND_SCRIPT ;
	static{
		InputStream in = ScriptPackagePaser.class.getResourceAsStream("package-parser.js");
		try {
			InputStreamReader reader = new InputStreamReader(in,"utf-8");
			StringWriter out = new StringWriter();
			char[] cbuf = new char[1024];
			int count;
			while((count = reader.read(cbuf))>=0){
				out.write(cbuf, 0, count);
			}
			out.flush();
			BIND_SCRIPT = out.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String source;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.PackageParser#parse(java.lang.String,
	 *      org.xidea.jsi.JSIPackage)
	 */
	public void parse(String source) {
		this.source = source;
	}

	public void setup(JSIPackage packageObject) {
		javax.script.SimpleBindings binds = new javax.script.SimpleBindings();
		try {
			binds.put("$this", new PackageWapper(packageObject));
			engine.eval(BIND_SCRIPT, binds);
			engine.eval(source, binds);
		} catch (Exception e) {
			e.printStackTrace();
			throw new UnsupportedSyntaxException(e);
		}
	}

	public static final class PackageWapper {
		private JSIPackage packageObject;

		public PackageWapper(JSIPackage packageObject) {
			this.packageObject = packageObject;
		}

		public void addScript(String scriptName, Object objectNames,
				Object beforeLoadDependences, Object afterLoadDependences) {
			try {

				packageObject.addScript(scriptName, convertArray(objectNames),
						convertArray(beforeLoadDependences),
						convertArray(afterLoadDependences));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(Arrays.asList(scriptName,
						convertArray(objectNames),
						convertArray(beforeLoadDependences),
						convertArray(afterLoadDependences)));
				throw new UnsupportedSyntaxException(e);
			}
		}

		public void addDependence(String thisPath, Object targetPath,
				Object afterLoad) {
			try {
				packageObject.addDependence(thisPath, convertArray(targetPath),
						convertBoolean(afterLoad));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(Arrays.asList(thisPath,
						convertArray(targetPath), afterLoad));
				throw new UnsupportedSyntaxException(e);
			}
		}

		public void setImplementation(String implementation) {
			packageObject.setImplementation(implementation);
		}
		
		public String getSource(String scriptName){
			return packageObject.loadText(scriptName);
		}

		private Boolean convertBoolean(Object object) {
			if (object instanceof Boolean) {
				return (Boolean) object;
			} else if (object instanceof Number) {
				return ((Number) object).floatValue() != 0;
			} else if (object instanceof String) {
				return ((String) object).length() > 0;
			}
			return object != null;

		}

		private Object convertArray(Object object) {
			if (object instanceof sun.org.mozilla.javascript.internal.NativeArray) {
				sun.org.mozilla.javascript.internal.NativeArray source = ((sun.org.mozilla.javascript.internal.NativeArray) object);
				int len = (int) source.getLength();
				ArrayList<String> result = new ArrayList<String>(len);
				for (int i = 0; i < len; i++) {
					result.add((String) source.get(i, null));
				}
				return result;
			} else {
				// throw new RuntimeException("不支持的数据格式");
				return (String) object;
			}
		}
	}
}
