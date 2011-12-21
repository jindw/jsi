package org.xidea.jsi.util;

import java.util.IdentityHashMap;

import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.Scope;

public class NameRefactor {
	private AstRoot root;
	private String source;
	private IdentityHashMap<AstNode, String> oldValueMap = new IdentityHashMap<AstNode, String>();
	public NameRefactor(String path,String source){
		this.source = source;
		CompilerEnvironsImpl env = new CompilerEnvironsImpl();
		Parser parser = env.createParser();
		root = parser.parse(source,path,1);
	}
	public String rename(int index,String newName){
        Name node = findName(root,index);
        if(node != null){
        	Scope scope = node.getDefiningScope();
        	doReplace(scope,node,newName);
        }
        return toSource();
	}
	private String toSource() {
		NodeVisitor v = new NodeVisitor() {
			int begin = 0;
			final StringBuilder buf = new StringBuilder();
			public boolean visit(AstNode node) {
				int start = node.getAbsolutePosition();
				if(start>begin){
					buf.append(source.substring(begin,start));
					begin = start;
				}
				if(node instanceof Name){
					Name name = (Name)node;
					String id = name.getIdentifier();
					buf.append(id);
					String value = oldValueMap.get(name);
					if(value == null){
						value = id;
					}
					begin+=value.length();
				}
				return true;
			}
			public String toString(){
				if(begin<source.length()){
					return buf.toString()+source.substring(begin);
				}else{
					return buf.toString();
				}
				
			}
		};
		root.visit(v);
		return v.toString();
	}
	private void doReplace(final Scope scope, final Name node, final String newName) {
		scope.visit(new NodeVisitor() {
			public boolean visit(AstNode node) {
				if(node instanceof Name){
					Name name = (Name)node;
					if(scope == name.getDefiningScope()){
						if(!oldValueMap.containsKey(name)){
							oldValueMap.put(name, name.getIdentifier());
						}
						name.setIdentifier(newName);
					}
				}
				return true;
			}
		});
	}
	private Name findName(AstNode root,final int index) {
		final Name[] rtv = new Name[1];
        root.visit(new NodeVisitor() {
			public boolean visit(AstNode node) {
				if(node instanceof Name){
					if(node.getAbsolutePosition() == index){
						Name name = (Name)node;
						rtv[0]= name;
						return false;
					}
				}
				return true;
			}
		});
		return rtv[0];
	}
}
