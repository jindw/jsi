package cn.jside.jsi.tools.rhino.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jside.jsi.tools.util.ScriptNode;
import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;


public class FunctionScopeReplacer extends AbstractReplacer implements
		ScriptNode {
	protected ScriptOrFnNode scriptNode;

	private Node parentNode;

	private Node functionNode;

	protected FunctionScopeReplacer(AbstractReplacer parentReplacer) {
		super(parentReplacer);
	}

	FunctionScopeReplacer(FunctionNode fn, AbstractReplacer parentReplacer,
			Node parentNode, Node functionNode) {
		super(parentReplacer);
		this.parentNode = parentNode;
		localVarMap.put("arguments", "arguments");
		this.scriptNode = fn;
		this.functionNode = functionNode;
		this.preInitialize(fn);

	}

	// 这两个成员非常诡异.注意
	private int subFunctrionIndex = 0;

	/**
	 * 唯一的一次调用
	 * 
	 * @see AbstractReplacer#collectSubNodes(Node)
	 */
	@Override
	protected FunctionNode nextFunction() {
		return scriptNode.getFunctionNode(subFunctrionIndex++);
	}

	protected void preInitialize(ScriptOrFnNode root) {
		String[] values = root.getParamAndVarNames();

		// TODO:????
		String expressionFunctionName = null;
		if (root instanceof FunctionNode) {
			FunctionNode fn = (FunctionNode) root;
			if (fn.getFunctionType() > 1) {// exp
				String name = fn.getFunctionName();
				if (name.length() > 0) {
					expressionFunctionName = name;
				}
			}
		}

		if (expressionFunctionName == null) {
			for (int i = 0; i < values.length; i++) {
				localVarMap.put(values[i], null);
			}
		} else {// 有标识的函数表达式。ECMA但作函数内变量， 但是IE当作外部申明变量，按照IE的方式处理，不会有问题
			for (int i = 0; i < values.length; i++) {
				if (!expressionFunctionName.equals(values[i])) {
					localVarMap.put(values[i], null);
				}
			}
		}
		// 应为 IE bug，catch var x = function y(){}变量也当是本地变量
		this.collectCatchNodes(root);

		int count = root.getFunctionCount();
		for (int i = 0; i < count; i++) {
			FunctionNode fn = root.getFunctionNode(i);
			String name = fn.getFunctionName();
			if (name.length() > 0) {// 全部添加，包括有标识的函数表达式（非ECMA规范，兼容IE）。
				// DebugTool.println("函数：" + name);
				localVarMap.put(name, null);
			}
		}
		this.collectSubNodes(root);
	}

	private void collectCatchNodes(Node node) {
		node = node.getFirstChild();
		while (node != null) {
			// DebugTool.println(Rhino节点字符.getName(node.getType()));
			switch (node.getType()) {
			case Token.CATCH_SCOPE:
				// TODO:
				// DebugTool.println("%%%%CATCH_SCOPE");
				this.localVarMap.put(node.getFirstChild().getString(), null);
			}
			this.collectCatchNodes(node);
			node = node.getNext();
		}
	}

	private void appendSubFunction(ArrayList<ScriptNode> result,
			List<AbstractReplacer> subList) {
		for (Iterator<AbstractReplacer> it = subList.iterator(); it.hasNext();) {
			AbstractReplacer node = it.next();
			if (node instanceof ScriptNode) {
				result.add((ScriptNode) node);
			} else {
				appendSubFunction(result, node.subReplacerList);
			}
		}
		;
	}

	private ArrayList<ScriptNode> result = null;

	public List<ScriptNode> getChildren() {
		if (result == null) {
			String[] vars = this.scriptNode.getParamAndVarNames();
			ArrayList<ScriptNode> result = new ArrayList<ScriptNode>();
			appendSubFunction(result, this.subReplacerList);

			int line = getStartLine();
			for (int i = 0; i < vars.length; i++) {
				result.add(new VarNode(this, vars[i], line));
			}
			this.result = result;
		}
		return result;
	}

	public int getEndLine() {
		return this.scriptNode.getEndLineno();
	}

	private String name = null;

	public String getName() {
		if (this.name == null) {
			String name = ((FunctionNode) this.scriptNode).getFunctionName();
			if (name.length() == 0) {
				name = findName();
			}
			if (name == null || name.length() == 0) {
				name = "<匿名函数>";
			}
			this.name = name;
		}
		return this.name;
	}

	private String findName() {
		if (this.parentNode.getType() == Token.OBJECTLIT) {
			int i = -1;
			Node first = this.parentNode.getFirstChild();
			do {
				first = first.getNext();
				i++;
			} while (first != null && first != this.functionNode);
			Object[] names = (Object[]) this.parentNode
					.getProp(Node.OBJECT_IDS_PROP);
			String name = (String) names[i];
			Node ppNode = findParentNode(findParentFunctionNode(),this.parentNode);
			Node priviousNode = ppNode.getChildBefore(this.parentNode);
			name = findName(ppNode,priviousNode) + "."+name;
			return name;
		}
		Node previous = parentNode.getChildBefore(this.functionNode);
		if (previous != null) {
			return findName(parentNode,previous);
		}
		return "<匿名函数>";
	}
	private String findName(Node parentNode,Node priviousNode){
		switch(priviousNode.getType()){
		case Token.RETURN:
			return "return";
		case Token.THIS:
			return "this";
		case Token.NAME:
			return priviousNode.getString();
		case Token.STRING:
			return findName(parentNode,parentNode.getChildBefore(priviousNode)) + "."+priviousNode.getString();
		case Token.GETPROP:
			Node firstChild = priviousNode.getFirstChild();
			return findName(priviousNode,firstChild) + "." + firstChild.getNext().getString();
		}
		return null;
	}

	private ScriptOrFnNode findParentFunctionNode() {
		AbstractReplacer parent = this.parentReplacer;
		while (parent != null) {
			if (parent instanceof FunctionScopeReplacer) {
				return ((FunctionScopeReplacer) parent).scriptNode;
			} else {
				parent = parent.parentReplacer;
			}
		}
		return null;// this.scriptNode;
	}

	private Node findParentNode(Node parentNode,Node ls) {
		
		if(parentNode.getLastChild() == ls){
			return parentNode;
		}
		Node node = parentNode.getFirstChild();
		while(node!=null){
			Node findNode = findParentNode(node,ls);
			if(findNode !=null){
				return findNode;
			}
			node = node.getNext();
		}
		return null;// this.scriptNode;
	}

	public ScriptNode getParent() {
		AbstractReplacer node = this.parentReplacer;
		while (node != null) {
			if (node instanceof ScriptNode) {
				return (ScriptNode) node;
			} else {
				node = this.parentReplacer;
			}
		}
		return null;

	}

	public int getStartLine() {
		return this.scriptNode.getBaseLineno();
	}

	public int getType() {
		return TYPE_FUNCTION;
	}
}

class VarNode implements ScriptNode {
	private ScriptNode parent;
	private int line;
	private String name;

	public VarNode(ScriptNode parent, String name, int line) {
		this.parent = parent;
		this.name = name;
		this.line = line;
	}

	public List<ScriptNode> getChildren() {
		return Collections.EMPTY_LIST;
	}

	public int getEndLine() {
		return line;
	}

	public String getName() {
		return name;
	}

	public ScriptNode getParent() {
		return parent;
	}

	public int getStartLine() {
		return line;
	}

	public int getType() {
		return TYPE_VARIABLE;
	}

}