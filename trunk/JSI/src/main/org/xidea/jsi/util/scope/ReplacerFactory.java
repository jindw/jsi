package org.xidea.jsi.util.scope;

import java.util.ArrayList;
import java.util.Collection;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ScriptNode;
import org.xidea.jsi.util.CompilerEnvironsImpl;
import org.xidea.jsi.util.JavaScriptError;
import org.xidea.jsi.util.ReplaceAdvisor;

public class ReplacerFactory {
	public Replacer createReplacer(String path,String source,ReplaceAdvisor advisor){
		CompilerEnvironsImpl env = new CompilerEnvironsImpl();
		Parser parser = env.createParser();
		AstRoot root = parser.parse(source,path,1);
        IRFactory irf = new IRFactory(env, env);
        ScriptNode tree = irf.transformTree(root);
		return new ScriptReplacer( tree,  advisor);
	}

}
