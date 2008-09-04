package org.xidea.jsi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

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
		InputStream in1 = ScriptPackagePaser.class.getResourceAsStream("package-parser.js");
		InputStream in2 = ScriptPackagePaser.class.getResourceAsStream("/org/xidea/jsidoc/export/find-globals.js");
		try {
			InputStreamReader reader = new InputStreamReader(in1,"utf-8");
			StringWriter out = new StringWriter();
			char[] cbuf = new char[1024];
			int count;
			while((count = reader.read(cbuf))>=0){
				out.write(cbuf, 0, count);
			}
			reader = new InputStreamReader(in2,"utf-8");
			while((count = reader.read(cbuf))>=0){
				out.write(cbuf, 0, count);
			}
			out.flush();
			BIND_SCRIPT = out.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static Collection<String> findGlobalsFromJava6(String source) {
		sun.org.mozilla.javascript.internal.CompilerEnvirons env = new sun.org.mozilla.javascript.internal.CompilerEnvirons();
		env.setReservedKeywordAsIdentifier(true);
		sun.org.mozilla.javascript.internal.Parser parser = new sun.org.mozilla.javascript.internal.Parser(
				env, new sun.org.mozilla.javascript.internal.ErrorReporter() {

					public void error(String arg0, String arg1, int arg2,
							String arg3, int arg4) {
					}

					public sun.org.mozilla.javascript.internal.EvaluatorException runtimeError(
							String arg0, String arg1, int arg2, String arg3,
							int arg4) {
						return null;
					}

					public void warning(String arg0, String arg1, int arg2,
							String arg3, int arg4) {

					}

				});
		sun.org.mozilla.javascript.internal.ScriptOrFnNode node = parser.parse(
				source, "<>", 0);
		HashSet<String> result = new HashSet<String>();
		result.addAll(Arrays.asList(node.getParamAndVarNames()));
		int count = node.getFunctionCount();
		while ((count--) > 0) {
			String name = node.getFunctionNode(count).getFunctionName();
			if (name != null && name.length() > 0) {
				result.add(name);
			}
		}
		return result;
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
		public Collection<String> findGlobals(String scriptName){
			String source = this.getSource(scriptName);
			return findGlobalsFromJava6(source);
		}


		public void addScript(String scriptName, Object objectNames,
				Object beforeLoadDependences, Object afterLoadDependences) {
			try {

				packageObject.addScript(scriptName, (objectNames),
						(beforeLoadDependences),
						(afterLoadDependences));
			} catch (Exception e) {
				e.printStackTrace();
				throw new UnsupportedSyntaxException(e);
			}
		}

		public void addDependence(String thisPath, Object targetPath,
				boolean afterLoad) {
			try {
				packageObject.addDependence(thisPath, (targetPath),
						afterLoad);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(Arrays.asList(thisPath,
						(targetPath), afterLoad));
				throw new UnsupportedSyntaxException(e);
			}
		}

		public void setImplementation(String implementation) {
			packageObject.setImplementation(implementation);
		}
		
		public String getSource(String scriptName){
			return packageObject.loadText(scriptName);
		}

//		private Object convertArray(Object object) {
//			if (object instanceof sun.org.mozilla.javascript.internal.NativeArray) {
//				sun.org.mozilla.javascript.internal.NativeArray source = ((sun.org.mozilla.javascript.internal.NativeArray) object);
//				int len = (int) source.getLength();
//				ArrayList<String> result = new ArrayList<String>(len);
//				for (int i = 0; i < len; i++) {
//					result.add((String) source.get(i, null));
//				}
//				return result;
//			} else {
//				// throw new RuntimeException("不支持的数据格式");
//				return (String) object;
//			}
//		}
	}
}
