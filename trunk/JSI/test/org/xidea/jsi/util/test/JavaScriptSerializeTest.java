package org.xidea.jsi.util.test;

import java.io.IOException;

import org.junit.Test;
import org.mozilla.javascript.Decompiler;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.UintMap;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Label;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.ScriptNode;
import org.xidea.jsi.impl.JSIText;
import org.xidea.jsi.util.CompilerEnvironsImpl;
import org.xidea.jsi.util.JavaScriptSerialize;
import org.xidea.jsi.util.NameRefactor;

public class JavaScriptSerializeTest {
	@Test
	public void testSerialize() throws IOException{
		String source = JSIText.loadText(JavaScriptSerializeTest.class.getResource("/require.js"),"UTF-8");
		//System.out.println(source);
		CompilerEnvironsImpl env = new CompilerEnvironsImpl();
		AstRoot root = env.createParser().parse(source, "require.js", 1);
		JavaScriptSerialize jss = new JavaScriptSerialize(root);
		System.out.println(jss);
	}

}
