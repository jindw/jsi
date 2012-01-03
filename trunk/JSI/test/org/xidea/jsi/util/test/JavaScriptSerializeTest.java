package org.xidea.jsi.util.test;

import java.io.IOException;

import org.junit.Test;
import org.mozilla.javascript.ast.AstRoot;
import org.xidea.jsi.impl.v2.JSIText;
import org.xidea.jsi.util.CompilerEnvironsImpl;
import org.xidea.jsi.util.JavaScriptSerialize;

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
