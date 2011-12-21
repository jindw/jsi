package org.xidea.jsi.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ast.AstRoot;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.PackageSyntaxException;

public class RhinoScriptPackagePaser extends PackageParser {
	private static final Log log = LogFactory.getLog(RhinoScriptPackagePaser.class);

	private JSIPackage packageObject;
	public RhinoScriptPackagePaser(JSIPackage packageObject){
		this.packageObject = packageObject;
		parse(packageObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.PackageParser#parse(java.lang.String,
	 *      org.xidea.jsi.JSIPackage)
	 */
	private void parse(final JSIPackage packageObject) {
		final String source = packageObject.loadText(JSIPackage.PACKAGE_FILE_NAME);
		try {
			@SuppressWarnings("unused")
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
			log.warn(e);;
			throw new PackageSyntaxException(packageObject.getName(),e);
		}
	}

	@Override
	public Collection<String> findGlobals(String scriptName,String pattern) {
		String source = this.packageObject.loadText(scriptName);
		if(source == null){
			//当某个脚本没有一起大包时，这里抛出的异常可能会导致包解析失败
			return new ArrayList<String>();
		}
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
		AstRoot node = parser.parse(
				source, "<>", 0);
		HashSet<String> result = new HashSet<String>();
		result.addAll(Arrays.asList(node.getParamAndVarNames()));
		int count = node.getFunctionCount();
		while ((count--) > 0) {
			String name = node.getFunctionNode(count).getName();
			if (name != null && name.length() > 0) {
				result.add(name);
			}
		}
		return result;
	}


	
}
