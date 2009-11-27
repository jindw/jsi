package org.xidea.jsi.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.script.ScriptEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.PackageSyntaxException;

import sun.org.mozilla.javascript.internal.CompilerEnvirons;
import sun.org.mozilla.javascript.internal.ErrorReporter;
import sun.org.mozilla.javascript.internal.EvaluatorException;
import sun.org.mozilla.javascript.internal.Parser;
import sun.org.mozilla.javascript.internal.ScriptOrFnNode;

public class Java6ScriptPackagePaser extends PackageParser {
	private static final Log log = LogFactory.getLog(Java6ScriptPackagePaser.class);
	
	public static final ScriptEngine engine = new javax.script.ScriptEngineManager()
			.getEngineByExtension("js");

	private JSIPackage packageObject;
	
	public Java6ScriptPackagePaser(JSIPackage packageObject){
		this.packageObject = packageObject;
		parse(packageObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xidea.jsi.PackageParser#parse(java.lang.String,
	 *      org.xidea.jsi.JSIPackage)
	 */
	private void parse(JSIPackage packageObject) {
		this.packageObject = packageObject;
		String source = packageObject.loadText(JSIPackage.PACKAGE_FILE_NAME);
		javax.script.SimpleBindings binds = new javax.script.SimpleBindings();
		try {
			binds.put("$this", this);
			engine.eval(BIND_SCRIPT, binds);
			engine.eval(source, binds);
		} catch (Exception e) {
			log.error(e);;
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
