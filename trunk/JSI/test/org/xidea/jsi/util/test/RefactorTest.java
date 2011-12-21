package org.xidea.jsi.util.test;

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
import org.xidea.jsi.util.CompilerEnvironsImpl;
import org.xidea.jsi.util.NameRefactor;

public class RefactorTest {
	@Test
	public void testRename(){
		String path = ".js";
		String source = "var xxx = function xxx(){var xxx = 1;alert(xxx)}";
		String name = "xxx";
		int index = source.lastIndexOf(name);
		String result = new NameRefactor(path, source).rename(index, name+"$__");
		System.out.println(result);
		System.out.println(source);
	}
	@Test
	public void testRefractor(){
		String source = "var aaa=1;function xxx(a){" +
				"function n1(a){var n;alert(a+b)}\n" +
				"l1:for(var i=0;i<10;i++){alert(i);break l1;}" +
				"};var yyy = xxx();function ddd(a){alert(a)}";
		NodeVisitor vistor1 = new NodeVisitor() {
			public boolean visit(AstNode node) {
				if(node instanceof Label){
					Label name = (Label)node;
					String id = name.getName();
					name.setName(id+"$");
				}
				if(node instanceof Name){
					Name name = (Name)node;
					String id = name.getIdentifier();

					Scope scope = name.getDefiningScope();
					System.out.print(System.identityHashCode(scope)+","+id);
					System.out.println(Token.typeToName(node.getParent().getType()));

					name.setIdentifier(id+"$");
					return true;
				}
				return true;
			}
		};
		String result1 =  replace( source,vistor1);
		System.out.println(result1);
	}

	private String replace(String source,NodeVisitor vistor) {
		String path = "test.js";
		CompilerEnvironsImpl env = new CompilerEnvironsImpl();
		Parser parser = env.createParser();
		AstRoot root = parser.parse(source,path,1);
        IRFactory irf = new IRFactory(env, env);
        root.visit(vistor);
        return root.toSource();
        
//        ScriptNode tree = irf.transformTree(root);
//        UintMap properties = new UintMap(1);
//        properties.put(Decompiler.INITIAL_INDENT_PROP, 1);
//        String ds = Decompiler.decompile(tree.getEncodedSource(), 0, properties);
//        return ds;
	}

}
