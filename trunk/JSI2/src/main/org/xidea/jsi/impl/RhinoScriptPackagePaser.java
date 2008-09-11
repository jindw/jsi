package org.xidea.jsi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.UnsupportedSyntaxException;

class RhinoScriptPackagePaser extends PackageParser {
	private static final String BIND_SCRIPT ;
	static{
		InputStream in1 = RhinoScriptPackagePaser.class.getResourceAsStream("package-parser.js");
		try {
			InputStreamReader reader = new InputStreamReader(in1,"utf-8");
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
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.PackageParser#parse(java.lang.String,
	 *      org.xidea.jsi.JSIPackage)
	 */
	public void parse(final JSIPackage packageObject) {
		final String source = packageObject.loadText(JSIPackage.PACKAGE_FILE_NAME);
		try {
			Object result = Context.call(new ContextAction() {
				public Object run(final Context cx) {
					Scriptable scope = ScriptRuntime.getGlobal(cx);
					scope.put("$this", scope, Context.toObject(RhinoScriptPackagePaser.this, scope));
					cx.evaluateString(scope, BIND_SCRIPT, "<package-wrapper.js>", 1, null);
					cx.evaluateString(scope,source, packageObject.getName().replace('.', '/')+"/__package__.js", 1, null);
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			throw new UnsupportedSyntaxException(e);
		}
	}

	@Override
	public Collection<String> findGlobals(String source,String pattern) {
		CompilerEnvirons env = new CompilerEnvirons();
		env.setReservedKeywordAsIdentifier(true);
		Parser parser = new Parser(
				env, new ErrorReporter() {

					public void error(String arg0, String arg1, int arg2,
							String arg3, int arg4) {
					}

					public EvaluatorException runtimeError(
							String arg0, String arg1, int arg2, String arg3,
							int arg4) {
						return null;
					}

					public void warning(String arg0, String arg1, int arg2,
							String arg3, int arg4) {

					}

				});
		ScriptOrFnNode node = parser.parse(
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


	
}
