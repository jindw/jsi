package org.xidea.jsi.util.scope;


import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.ScriptNode;


public class FunctionScopeReplacer extends AbstractReplacer{
	protected ScriptNode scriptNode;


	protected FunctionScopeReplacer(AbstractReplacer parentReplacer) {
		super(parentReplacer);
	}

	FunctionScopeReplacer(ScriptNode fn, AbstractReplacer parentReplacer,
			Node parentNode, Node functionNode) {
		super(parentReplacer);
//		this.parentNode = parentNode;
//		this.functionNode = functionNode;
		localVarMap.put("arguments", "arguments");
		this.scriptNode = fn;
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

	protected void preInitialize(ScriptNode root) {
		String[] values = root.getParamAndVarNames();

		// TODO:????
		String expressionFunctionName = null;
		if (root instanceof FunctionNode) {
			FunctionNode fn = (FunctionNode) root;
			if (fn.getFunctionType() > 1) {// exp
				String name = fn.getName();
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
			String name = fn.getName();
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


}