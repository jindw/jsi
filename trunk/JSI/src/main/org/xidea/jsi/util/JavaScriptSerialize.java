package org.xidea.jsi.util;

import java.util.List;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.ArrayComprehension;
import org.mozilla.javascript.ast.ArrayComprehensionLoop;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.CatchClause;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.ContinueStatement;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForInLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Jump;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Label;
import org.mozilla.javascript.ast.LabeledStatement;
import org.mozilla.javascript.ast.LetNode;
import org.mozilla.javascript.ast.Loop;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ParenthesizedExpression;
import org.mozilla.javascript.ast.RegExpLiteral;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.ThrowStatement;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.UnaryExpression;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;
import org.mozilla.javascript.ast.XmlDotQuery;
import org.mozilla.javascript.ast.XmlElemRef;
import org.mozilla.javascript.ast.XmlExpression;
import org.mozilla.javascript.ast.XmlFragment;
import org.mozilla.javascript.ast.XmlLiteral;
import org.mozilla.javascript.ast.XmlRef;
import org.mozilla.javascript.ast.Yield;

public class JavaScriptSerialize {
	// private AstNode root;
	private StringBuilder sb = new StringBuilder();
	int depth = 0;

	public JavaScriptSerialize(AstNode root) {
		// this.root = root;
	}

	protected void write(AstNode node) {
		makeIndent();
		if (node instanceof Name || node instanceof NumberLiteral
				|| node instanceof RegExpLiteral
				|| node instanceof KeywordLiteral) {
			sb.append(node.toSource(depth));
		} else if (node instanceof ArrayLiteral) {
			writeArray((ArrayLiteral) node);
		} else if (node instanceof ObjectLiteral) {
			writeObject((ObjectLiteral) node);
		} else if (node instanceof InfixExpression) {
			writeInfix((InfixExpression) node);
		} else if (node instanceof ElementGet) {
			writeGet((ElementGet) node);
		} else if (node instanceof UnaryExpression) {
			writeUnary((UnaryExpression) node);
		} else if (node instanceof ParenthesizedExpression) {
			sb.append("(");
			ParenthesizedExpression pe = (ParenthesizedExpression) node;
			sb.append(toSource(pe.getExpression(), depth + 1));
			sb.append(")");

		} else if (node instanceof ExpressionStatement) {
			write(((ExpressionStatement) node).getExpression());
			sb.append(";");
			println();
		} else if (node instanceof Jump) {
			writeJump(node);
		} else if (node instanceof LabeledStatement) {
			writeLabeledStatement((LabeledStatement) node);
		} else if (node instanceof ReturnStatement) {
			writeReturn((ReturnStatement) node);
		} else if (node instanceof VariableDeclaration) {
			writeVar((VariableDeclaration) node);
		} else if (node instanceof VariableInitializer) {
			writeVarAssign((VariableInitializer) node);
		} else {
			switch (node.getType()) {
			case Token.BLOCK:
				sb.append("{");
				println();
				for (Node kid : node) {
					write((AstNode) kid);
				}
				makeIndent();
				sb.append("}");
				println();
				break;
			case Token.TRY:
				writeTry((TryStatement) node);
				break;
			case Token.CATCH:
				writeCatch((CatchClause) node);
				break;
			case Token.THROW:
				writeThrow((ThrowStatement) node);
				break;
			case Token.HOOK:
				writeCondition((ConditionalExpression) node);
				break;
			case Token.CALL:
				writeCall((FunctionCall) node);
				break;
			case Token.NEW:
				writeNew((NewExpression) node);
				break;
			case Token.IF:
				writeIf((IfStatement) node);
				break;
			case Token.CASE:
				writeCase((SwitchCase) node);
				break;
			case Token.YIELD:
				writeYield((Yield) node);
				break;
			case Token.WITH:
				writeWith((WithStatement) node);
				break;

			case Token.ERROR:
			case Token.COMMENT:
			case Token.EMPTY:
				break;
			default:
				// xml name 不能和 javascript name 混淆
				if (!processXML(node)) {
					throw new IllegalArgumentException("unknow token:"
							+ node.toSource());
				}

			}
		}

	}

	private boolean processXML(AstNode node) {
		if (node instanceof XmlDotQuery) {
			XmlDotQuery dq = (XmlDotQuery) node;
			write(dq.getLeft());
			sb.append(".(");
			write(dq.getRight());
			sb.append(")");
		} else if (node instanceof XmlRef) {
			if (node instanceof XmlElemRef) {
				XmlElemRef ref = (XmlElemRef) node;
				if (ref.isAttributeAccess()) {
					sb.append("@");
				}
				Name namespace = ref.getNamespace();
				if (namespace != null) {
					sb.append(namespace.toSource(0));
					sb.append("::");
				}
				sb.append("[");
				write(ref.getExpression());
				sb.append("]");
			} else {
				sb.append(node.toSource(0));
			}
		} else if (node instanceof XmlFragment) {
			if (node instanceof XmlExpression) {
				sb.append("{");
				write(((XmlExpression) node).getExpression());
				sb.append("}");
			} else {// xml string
				sb.append(node.toSource(depth));
			}
		} else if (node instanceof XmlLiteral) {
			for (XmlFragment frag : ((XmlLiteral) node).getFragments()) {
				write(frag);
			}
		} else {
			return false;
		}
		return true;
	}

	private void writeJump(AstNode node) {
		if (node instanceof Scope) {
			if (node instanceof Loop) {
				writeLoop((Loop) node);
			} else if (node instanceof ScriptNode) {
				if (node instanceof FunctionNode) {
					writeFunction((FunctionNode) node);
				} else {// AstRoot
					for (Node c : node) {
						write(((AstNode) c));
					}
				}

			} else if (node instanceof LetNode) {
				writeLetScope((LetNode) node);
			} else if (node instanceof ArrayComprehension) {
				writeArrayComprehension((ArrayComprehension) node);
			}

		} else if (node instanceof SwitchStatement) {
			writeSwitch((SwitchStatement) node);
		} else {// break,continue label
			writeLabelJump(node);
		}
	}

	private void writeFunction(FunctionNode fn) {
		sb.append("function");
		Name functionName = fn.getFunctionName();
		if (functionName != null) {
			sb.append(" ");
			sb.append(toSource(functionName, 0));
		}
		List<AstNode> params = fn.getParams();
		if (params == null) {
			sb.append("() ");
		} else {
			sb.append("(");
			writeList(params);
			sb.append(") ");
		}
		AstNode body = fn.getBody();
		if (fn.isExpressionClosure()) {
			sb.append(" ");
			sb.append(toSource(body, depth));
		} else {
			sb.append(toSource(body, depth).trim());
		}
		if (fn.getFunctionType() == FunctionNode.FUNCTION_STATEMENT) {
			sb.append("\n");
		}
	}

	private void writeLabelJump(AstNode node) {
		if (node instanceof Label) {
			sb.append(((Label) node).getName());
			sb.append(":\n");
		} else {
			Name label = null;
			if (node instanceof BreakStatement) {
				sb.append("break");
				label = ((BreakStatement) node).getBreakLabel();
			} else if (node instanceof ContinueStatement) {
				sb.append("continue");
				label = ((ContinueStatement) node).getLabel();
			} else {
				throw new IllegalStateException("unknow status:"
						+ node.toSource());
			}
			if (label != null) {
				sb.append(" ");
				sb.append(toSource(label, 0));
			}
			sb.append(";\n");
		}
	}

	private void writeSwitch(SwitchStatement ss) {
		sb.append("switch(");
		sb.append(toSource(ss.getExpression(), depth + 1).trim());
		sb.append("){\n");
		for (SwitchCase sc : ss.getCases()) {
			sb.append(toSource(sc, depth + 1));
		}
		makeIndent();
		sb.append("}\n");
	}

	private void writeLetScope(LetNode ln) {
		sb.append("let(");
		writeList(ln.getVariables().getVariables());
		sb.append(")");
		AstNode body = ln.getBody();
		if (body != null) {
			write(body);
		}
	}

	private void writeArrayComprehension(ArrayComprehension ac) {
		sb.append("[");
		write(ac.getResult());
		for (ArrayComprehensionLoop loop : ac.getLoops()) {
			write(loop);
		}
		AstNode filter = ac.getFilter();
		if (filter != null) {
			sb.append("if(");
			sb.append(toSource(filter, depth + 1).trim());
			sb.append(")");
		}
		sb.append("]");
	}

	private void writeLoop(Loop node) {
		AstNode body = node.getBody();
		if (node instanceof ForLoop) {
			ForLoop fl = (ForLoop) node;
			sb.append("for(");
			sb.append(toSource(fl.getInitializer(), depth + 1));
			sb.append(";");
			sb.append(toSource(fl.getCondition(), depth + 1));
			sb.append("; ");
			sb.append(toSource(fl.getIncrement(), depth + 1));
			sb.append(") ");
			if (body instanceof Block) {
				sb.append(toSource(body, depth).trim()).append("\n");
			} else {
				sb.append("\n").append(toSource(body, depth + 1));
			}
		} else if (node instanceof WhileLoop) {
			sb.append("while (");
			sb.append(toSource(((WhileLoop) node).getCondition(), depth + 1)
					.trim());
			sb.append(") ");
			if (body instanceof Block) {
				sb.append(toSource(body, depth));
				sb.append("\n");
			} else {
				sb.append("\n");
				sb.append(toSource(body, depth));
			}

			sb.append("do");
			;
			sb.append("while (");
			sb.append(");\n");
		} else if (node instanceof DoLoop) {
			sb.append("do");
			sb.append(toSource(body, depth).trim());
			sb.append("while (");
			sb.append(toSource(((DoLoop) node).getCondition(), depth + 1)
					.trim());
			sb.append(");\n");
		} else if (node instanceof ForInLoop) {
			ForInLoop fil = (ForInLoop) node;
			if (node instanceof ArrayComprehensionLoop) {
				sb.append(" for (");
				sb.append(toSource(fil.getIterator(), depth + 1).trim());
				sb.append(" in ");
				sb.append(toSource(fil.getIteratedObject(), depth + 1).trim());
				sb.append(")");
			} else {
				sb.append("for ");
				if (fil.isForEach()) {
					sb.append("each ");
				}
				sb.append("(");
				sb.append(toSource(fil.getIterator(), depth + 1).trim());
				sb.append(" in ");
				sb.append(toSource(fil.getIteratedObject(), depth + 1).trim());
				sb.append(") ");
				if (body instanceof Block) {
					sb.append(toSource(body, depth + 1).trim());
					sb.append("\n");
				} else {
					sb.append("\n").append(toSource(body, depth + 1));
				}
			}
		}

	}

	private void writeUnary(UnaryExpression ue) {
		int type = ue.getType();
		String op = AstNode.operatorToString(type);
		if (!ue.isPostfix()) {
			sb.append(op);
			if (type == Token.TYPEOF || type == Token.DELPROP) {
				sb.append(" ");
			}
		}
		sb.append(toSource(ue.getOperand(), 0));
		if (ue.isPostfix()) {
			sb.append(op);
		}
	}

	private void writeVar(VariableDeclaration node) {
		sb.append(Token.typeToName(node.getType()).toLowerCase());
		sb.append(" ");
		writeList(node.getVariables());
		if (!(node.getParent() instanceof Loop)) {
			sb.append(";");
			println();
		}
	}

	private void writeVarAssign(VariableInitializer node) {
		sb.append(toSource(node.getTarget(), 0));
		AstNode initializer = node.getInitializer();
		if (initializer != null) {
			sb.append("=");
			sb.append(toSource(initializer, depth + 1).trim());
		}
	}

	private void writeYield(Yield node) {
		AstNode value = node.getValue();
		if (value == null) {
			sb.append("yield");
		} else {
			sb.append("yield ");
			write(value);
		}
	}

	private void writeCase(SwitchCase node) {
		AstNode expression = node.getExpression();
		List<AstNode> statements = node.getStatements();
		if (expression == null) {
			sb.append("default:");
			println();
		} else {
			sb.append("case ");
			sb.append(toSource(expression, 0));
			sb.append(":");
			println();
		}
		if (statements != null) {
			for (AstNode s : statements) {
				sb.append(toSource(s, depth + 1));
			}
		}
	}

	private void writeWith(WithStatement node) {
		sb.append("with (");
		sb.append(toSource(node.getExpression(), depth + 1));
		sb.append(") ");
		AstNode statement = node.getStatement();
		sb.append(toSource(statement, depth + 1).trim());
		if (!(statement instanceof Block)) {
			sb.append(";");
			println();
		}
	}

	private void writeReturn(ReturnStatement node) {
		sb.append("return");
		AstNode returnValue = node.getReturnValue();
		if (returnValue != null) {
			sb.append(" ");
			sb.append(toSource(returnValue, depth).trim());
		}
		sb.append(";");
		println();
	}

	private void writeLabeledStatement(LabeledStatement node) {
		for (Label label : node.getLabels()) {
			write(label); // prints newline
		}
		write(node.getStatement());

	}

	private void writeThrow(ThrowStatement node) {
		sb.append("throw ");
		sb.append(toSource(node.getExpression(), depth + 1).trim());
		sb.append(";");
		println();
	}

	private String toSource(AstNode node, int depth) {
		int d0 = this.depth;
		int p0 = sb.length();
		this.depth = depth;
		write(node);
		String result = sb.substring(p0);
		this.depth = d0;
		return result;
	}

	private void writeTry(TryStatement node) {
		sb.append("try");
		sb.append(toSource(node.getTryBlock(), depth).trim());
		for (CatchClause cc : node.getCatchClauses()) {
			sb.append(toSource(cc, depth));
		}
		AstNode finallyBlock = node.getFinallyBlock();
		if (finallyBlock != null) {
			sb.append("finally");
			write(finallyBlock);
		}
	}

	private void writeInfix(InfixExpression node) {
		int type = node.getType();
		if (type == Token.DOTQUERY) {
			processXML(node);
		} else {
			write(node.getLeft());
			String op = InfixExpression.operatorToString(type);
			sb.append(" ");
			sb.append(op);
			sb.append(" ");
			write(node.getRight());
		}
	}

	private void writeIf(IfStatement ifs) {
		sb.append("if(");
		sb.append(toSource(ifs.getCondition(), depth).trim());
		sb.append(") ");
		AstNode thenPart = ifs.getThenPart();
		if (!(thenPart instanceof Block)) {
			println();
			makeIndent();
		}
		write(thenPart);
		AstNode elsePart = ifs.getElsePart();
		if (elsePart instanceof IfStatement) {
			sb.append("else");
			sb.append(toSource(elsePart, depth).trim());
		} else if (elsePart != null) {
			sb.append("else");
			sb.append(toSource(elsePart, depth).trim());
		}
		println();
	}

	private void writeNew(NewExpression nf) {
		sb.append("new ");
		writeCall(nf);
		ObjectLiteral initializer = nf.getInitializer();
		if (initializer != null) {
			sb.append(" ");
			sb.append(toSource(initializer, depth + 1).trim());
		}
	}

	private void writeCall(FunctionCall fc) {
		write(fc.getTarget());
		sb.append("(");
		List<AstNode> arguments = fc.getArguments();
		if (arguments != null) {
			writeList(arguments);
		}
		sb.append(")");
	}

	private void writeGet(ElementGet node) {
		write(node.getTarget());
		sb.append("[");
		sb.append(toSource(node.getElement(), depth + 1).trim());
		sb.append("]");
	}

	private void writeCondition(ConditionalExpression ce) {
		write(ce.getTestExpression());
		sb.append("?");
		sb.append(toSource(ce.getTrueExpression(), depth + 1).trim());
		sb.append(":");
		sb.append(toSource(ce.getFalseExpression(), depth + 1).trim());
	}

	private void writeCatch(CatchClause cc) {
		sb.append("catch(");
		sb.append(toSource(cc.getVarName(), 0));
		AstNode catchCondition = cc.getCatchCondition();
		if (catchCondition != null) {
			sb.append(" if ");
			write(catchCondition);
		}
		sb.append(") ");
		write(cc.getBody());
	}

	private void writeArray(ArrayLiteral al) {
		sb.append("[");
		List<AstNode> elements = al.getElements();
		writeList(elements);
		sb.append("]");
	}

	private void writeObject(ObjectLiteral ol) {
		sb.append("{");
		List<ObjectProperty> elements = ol.getElements();
		if (elements != null) {
			int i = 0;
			for (ObjectProperty item : elements) {
				if (i++ > 0) {
					sb.append(",");
				}
				if (item.isGetter()) {
					sb.append("get ");
				} else if (item.isSetter()) {
					sb.append("set ");
				}
				write(item.getLeft());
				if (item.getType() == Token.COLON) {
					sb.append(":");
				}
				write(item.getRight());
			}
		}
		sb.append("}");
	}

	private void writeList(List<? extends AstNode> elements) {
		if (elements != null) {
			depth++;
			int i = 0;
			for (AstNode item : elements) {
				if (i++ > 0) {
					sb.append(",");
				}
				write(item);
			}
			depth--;
		}
	}

	protected void println() {
		sb.append("\n");
	}

	protected void makeIndent() {
		for (int i = 0; i < depth; i++) {
			sb.append("  ");
		}
	}

	public String toString() {
		return null;
	}
}
