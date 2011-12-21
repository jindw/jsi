package org.xidea.jsi.util.scope;

import java.util.ArrayList;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.ObjToIntMap;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.ScriptNode;

public class CompressorImpl {
	public static void main(String[] args) {
//		try {
//	        System.out.println("###"+tree.toSource());
//	        tree = root;
//	        org.mozilla.javascript.Decompiler de = new Decompiler();
//
//            UintMap properties = new UintMap(1);
//            properties.put(Decompiler.INITIAL_INDENT_PROP, 1);
//	        String ds = de.decompile(tree.getEncodedSource(), 0, properties);
//	        System.out.println(ds);
//			String s = tree.toSource();
//			System.out.println(Token.typeToName(tree.getFirstChild().getType()));
//			System.out.println("111" + toStringTree(root,tree));
//			System.out.println(s);
//			System.out.println(root.getComments());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println(env.getErrors());
	}
    public static String toStringTree(ScriptNode treeTop,Node node) {
            StringBuffer sb = new StringBuffer();
            toStringTreeHelper(treeTop, node, null, 0, sb);
            return sb.toString();
    }


    private static void generatePrintIds(Node n, ObjToIntMap map)
    {
        if (Token.printTrees) {
            map.put(n, map.size());
            for (Node cursor = n.getFirstChild(); cursor != null;
                 cursor = cursor.getNext())
            {
                generatePrintIds(cursor, map);
            }
        }
    }
	private static void toStringTreeHelper(ScriptNode treeTop, Node n,
			ObjToIntMap printIds, int level, StringBuffer sb) {
		if (printIds == null) {
			printIds = new ObjToIntMap();
			generatePrintIds(treeTop, printIds);
		}
		for (int i = 0; i != level; ++i) {
			sb.append("    ");
		}
		//n.toString(printIds, sb);
		sb.append(Token.typeToName(n.getType()));
		sb.append('\n');
		for (Node cursor = n.getFirstChild(); cursor != null; cursor = cursor
				.getNext()) {
			if (cursor.getType() == Token.FUNCTION) {
				int fnIndex = cursor.getExistingIntProp(Node.FUNCTION_PROP);
				FunctionNode fn = treeTop.getFunctionNode(fnIndex);
				toStringTreeHelper(fn, fn, null, level + 1, sb);
			} else {
				toStringTreeHelper(treeTop, cursor, printIds, level + 1, sb);
			}
		}
	}
}
