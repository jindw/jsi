package cn.jside.jsi.tools.rhino;

import java.beans.XMLDecoder;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ObjToIntMap;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;


public class DebugTool {
	private static final Log log = LogFactory.getLog(DebugTool.class);
	public static final int FUNCTION_END = Token.LAST_TOKEN + 1;
	


	public static void info(Object msg) {
		log.info(msg);
	}
	public static void debug(Object msg) {
		log.debug(msg);
	}

	public static void print(ScriptOrFnNode root) {
		if (log.isInfoEnabled()) {
			print(root, root, "");
		}
	}
	static class Index {
		int index = 0;
	}

	private static void print(ScriptOrFnNode root, Node node, String blank) {
		if (log.isInfoEnabled()) {
			StringWriter out = new StringWriter();
			print(root, node, blank, new Index(),out);
			log.info(out);
		}
	}

	private static void print(ScriptOrFnNode root, Node node, String blank,
			Index index,StringWriter out) {
		if (log.isInfoEnabled()) {
			int ft = -1;

			if (node.getType() == Token.FUNCTION) {
				node = root.getFunctionNode(index.index++);
				root = (ScriptOrFnNode) node;
				index = new Index();
				FunctionNode fn = (FunctionNode) node;
				ft = fn.getFunctionType();
				// root.getFunctionNode(0).getParamOrVarIndex("")
			}
			Node child = node.getFirstChild();
			out.write(blank);
			printNode(node);

			if (ft >= 0) {
				out.write("{functionType=" + ft + "}");
			}
			blank += "  ";
			out.write("\n");
			while (child != null) {
				print(root, child, blank, index,out);
				child = child.getNext();
			}
		}
	}

	public static void printNode(Node node) {
		if (log.isInfoEnabled()) {
			StringWriter out = new StringWriter();
			out.write("{");
			out.write("type:");
			out.write(getName(node.getType()));
			try {
				String value = node.getString();
				out.write(",value:");
				out.write(value);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (node instanceof Node.Jump) {
				Node.Jump jp = (Node.Jump) node;
				out.write(",jump:");
				out.write(new ObjToIntMap().get(jp.target, -1));
			} else {
				out.write(",class:");
				out.write(node.getClass().getName());
			}
			log.info(out);
		}
	}


	protected static void printTokenType(char data) {
		if (log.isInfoEnabled()) {
			log.info("<" + getName(data) + getLabel(data) + ">\n");
		}
	}

	protected static void printData(char[] data, int offset, int end) {
		if (log.isInfoEnabled()) {
			StringBuilder buf = new StringBuilder();
			int depth = 0;
			for (; offset < end; offset++) {
				char token = data[offset];
				switch (token) {
				case Token.REGEXP: // re-wrapped in '/'s in parser...
				case Token.STRING:
				case Token.NAME:
					buf.append("<" + getName(token) + ">");
					buf.append(":");
					offset = RhinoTool.printSourceString(data, offset + 1,
							true, buf) - 1;
					break;
				case Token.NUMBER:
					buf.append("<" + getName(token) + ">");
					offset = RhinoTool
							.printSourceNumber(data, offset + 1, null) - 1;
					break;
				case Token.FUNCTION:
					buf.append("<" + getName(token) + ">");
					offset++; // skip function type
					break;
				case Token.LC:// {
					depth++;
					buf.append(getLabel(token));
					break;
				case Token.RC:// {
					depth--;
					buf.append(getLabel(token));
					break;
				case Token.EOL:// {
					buf.append('\n');
					int d2 = (data[Math.min(offset + 1, end - 1)] == Token.RC) ? depth - 1
							: depth;
					for (int j = 0; j < d2; j++) {
						buf.append('\t');
					}
					break;
				default:
					buf.append(getName(token));
				}
			}
			log.info(buf.append("\r\n"));
		}
	}
	public static Map<String, String> loadCase(Class<? extends Object> clazz,String path) {
		XMLDecoder dec = new XMLDecoder(clazz
				.getResourceAsStream(path));
		return (Map<String, String>) dec.readObject();
	}
	public static ScriptOrFnNode parse(String source){
		AnalyserErrorReporter errorReporter = new AnalyserErrorReporter();
		Parser parser = RhinoTool.createParser(new CompilerEnvironsImpl(), errorReporter);
		return parser.parse(source, "", 0);
	}
	public static String getLabel(int type) {
		switch (type) {
		// case Token.NAME:
		// case Token.REGEXP: // re-wrapped in '/'s in parser...
		// case Token.STRING:
		// case Token.NUMBER:
		// return null;
		case Token.TRUE:
			return ("true");
		case Token.FALSE:
			return ("false");
		case Token.NULL:
			return ("null");
		case Token.THIS:
			return ("this");
		case Token.FUNCTION:
			return ("function");
		case FUNCTION_END:
			// Do nothing
			return "";
		case Token.COMMA:
			return (",");
		case Token.LC:
			return ("{");
		case Token.RC:
			return ("}");
		case Token.LP:
			return ("(");
		case Token.RP:
			return (")");
		case Token.LB:
			return ("[");
		case Token.RB:
			return ("]");
		case Token.EOL:
			return "";
		case Token.DOT:
			return (".");
		case Token.NEW:
			return ("new ");
		case Token.DELPROP:
			return ("delete ");
		case Token.IF:
			return ("if");
		case Token.ELSE:
			return ("else");
		case Token.FOR:
			return ("for");
		case Token.IN:
			return (" in ");
		case Token.WITH:
			return ("with");
		case Token.WHILE:
			return ("while");
		case Token.DO:
			return ("do");
		case Token.TRY:
			return ("try");
		case Token.CATCH:
			return ("catch");
		case Token.FINALLY:
			return ("finally");
		case Token.THROW:
			return ("throw ");
		case Token.SWITCH:
			return ("switch");
		case Token.BREAK:
			return ("break");
		case Token.CONTINUE:
			return ("continue");
		case Token.CASE:
			return ("case ");
		case Token.DEFAULT:
			return ("default");
		case Token.RETURN:
			return ("return");
		case Token.VAR:
			return ("var ");
		case Token.SEMI:
			return (";");
		case Token.ASSIGN:
			return ("=");
		case Token.ASSIGN_ADD:
			return ("+=");
		case Token.ASSIGN_SUB:
			return ("-=");
		case Token.ASSIGN_MUL:
			return ("*=");
		case Token.ASSIGN_DIV:
			return ("/=");
		case Token.ASSIGN_MOD:
			return ("%=");
		case Token.ASSIGN_BITOR:
			return ("|=");
		case Token.ASSIGN_BITXOR:
			return ("^=");
		case Token.ASSIGN_BITAND:
			return ("&=");
		case Token.ASSIGN_LSH:
			return ("<<=");
		case Token.ASSIGN_RSH:
			return (">>=");
		case Token.ASSIGN_URSH:
			return (">>>=");
		case Token.HOOK:
			return ("?");
		case Token.OBJECTLIT:
			// pun OBJECTLIT to mean colon in objlit property
			// initialization.
			// This needs to be distinct from COLON in the general case
			// to distinguish from the colon in a ternary... which needs
			// different spacing.
			return (":");
		case Token.COLON:
			return (":");
		case Token.OR:
			return ("||");
		case Token.AND:
			return ("&&");
		case Token.BITOR:
			return ("|");
		case Token.BITXOR:
			return ("^");
		case Token.BITAND:
			return ("&");
		case Token.SHEQ:
			return ("===");
		case Token.SHNE:
			return ("!==");
		case Token.EQ:
			return ("==");
		case Token.NE:
			return ("!=");
		case Token.LE:
			return ("<=");
		case Token.LT:
			return ("<");
		case Token.GE:
			return (">=");
		case Token.GT:
			return (">");
		case Token.INSTANCEOF:
			return (" instanceof ");
		case Token.LSH:
			return ("<<");
		case Token.RSH:
			return (">>");
		case Token.URSH:
			return (">>>");
		case Token.TYPEOF:
			return ("typeof ");
		case Token.VOID:
			return ("void ");
		case Token.NOT:
			return ("!");
		case Token.BITNOT:
			return ("~");
		case Token.POS:
			return ("+");
		case Token.NEG:
			return ("-");
		case Token.INC:
			return ("++");
		case Token.DEC:
			return ("--");
		case Token.ADD:
			return ("+");
		case Token.SUB:
			return ("-");
		case Token.MUL:
			return ("*");
		case Token.DIV:
			return ("/");
		case Token.MOD:
			return ("%");
		case Token.COLONCOLON:
			return ("::");
		case Token.DOTDOT:
			return ("..");
		case Token.DOTQUERY:
			return (".(");
		case Token.XMLATTR:
			return ("@");
		default:
			return null;

		}
	}

	public static String getName(int token) {
		switch (token) {
		case Token.ERROR:
			return "ERROR";
		case Token.EOF:
			return "EOF";
		case Token.EOL:
			return "EOL";
		case Token.ENTERWITH:
			return "ENTERWITH";
		case Token.LEAVEWITH:
			return "LEAVEWITH";
		case Token.RETURN:
			return "RETURN";
		case Token.GOTO:
			return "GOTO";
		case Token.IFEQ:
			return "IFEQ";
		case Token.IFNE:
			return "IFNE";
		case Token.SETNAME:
			return "SETNAME";
		case Token.BITOR:
			return "BITOR";
		case Token.BITXOR:
			return "BITXOR";
		case Token.BITAND:
			return "BITAND";
		case Token.EQ:
			return "EQ";
		case Token.NE:
			return "NE";
		case Token.LT:
			return "LT";
		case Token.LE:
			return "LE";
		case Token.GT:
			return "GT";
		case Token.GE:
			return "GE";
		case Token.LSH:
			return "LSH";
		case Token.RSH:
			return "RSH";
		case Token.URSH:
			return "URSH";
		case Token.ADD:
			return "ADD";
		case Token.SUB:
			return "SUB";
		case Token.MUL:
			return "MUL";
		case Token.DIV:
			return "DIV";
		case Token.MOD:
			return "MOD";
		case Token.NOT:
			return "NOT";
		case Token.BITNOT:
			return "BITNOT";
		case Token.POS:
			return "POS";
		case Token.NEG:
			return "NEG";
		case Token.NEW:
			return "NEW";
		case Token.DELPROP:
			return "DELPROP";
		case Token.TYPEOF:
			return "TYPEOF";
		case Token.GETPROP:
			return "GETPROP";
		case Token.SETPROP:
			return "SETPROP";
		case Token.GETELEM:
			return "GETELEM";
		case Token.SETELEM:
			return "SETELEM";
		case Token.CALL:
			return "CALL";
		case Token.NAME:
			return "NAME";
		case Token.NUMBER:
			return "NUMBER";
		case Token.STRING:
			return "STRING";
		case Token.NULL:
			return "NULL";
		case Token.THIS:
			return "THIS";
		case Token.FALSE:
			return "FALSE";
		case Token.TRUE:
			return "TRUE";
		case Token.SHEQ:
			return "SHEQ";
		case Token.SHNE:
			return "SHNE";
		case Token.REGEXP:
			return "OBJECT";
		case Token.BINDNAME:
			return "BINDNAME";
		case Token.THROW:
			return "THROW";
		case Token.RETHROW:
			return "RETHROW";
		case Token.IN:
			return "IN";
		case Token.INSTANCEOF:
			return "INSTANCEOF";
		case Token.LOCAL_LOAD:
			return "LOCAL_LOAD";
		case Token.GETVAR:
			return "GETVAR";
		case Token.SETVAR:
			return "SETVAR";
		case Token.CATCH_SCOPE:
			return "CATCH_SCOPE";
		case Token.ENUM_INIT_KEYS:
			return "ENUM_INIT_KEYS";
		case Token.ENUM_INIT_VALUES:
			return "ENUM_INIT_VALUES";
		case Token.ENUM_NEXT:
			return "ENUM_NEXT";
		case Token.ENUM_ID:
			return "ENUM_ID";
		case Token.THISFN:
			return "THISFN";
		case Token.RETURN_RESULT:
			return "RETURN_RESULT";
		case Token.ARRAYLIT:
			return "ARRAYLIT";
		case Token.OBJECTLIT:
			return "OBJECTLIT";
		case Token.GET_REF:
			return "GET_REF";
		case Token.SET_REF:
			return "SET_REF";
		case Token.DEL_REF:
			return "DEL_REF";
		case Token.REF_CALL:
			return "REF_CALL";
		case Token.REF_SPECIAL:
			return "REF_SPECIAL";
		case Token.DEFAULTNAMESPACE:
			return "DEFAULTNAMESPACE";
		case Token.ESCXMLTEXT:
			return "ESCXMLTEXT";
		case Token.ESCXMLATTR:
			return "ESCXMLATTR";
		case Token.REF_MEMBER:
			return "REF_MEMBER";
		case Token.REF_NS_MEMBER:
			return "REF_NS_MEMBER";
		case Token.REF_NAME:
			return "REF_NAME";
		case Token.REF_NS_NAME:
			return "REF_NS_NAME";
		case Token.TRY:
			return "TRY";
		case Token.SEMI:
			return "SEMI";
		case Token.LB:
			return "LB";
		case Token.RB:
			return "RB";
		case Token.LC:
			return "LC";
		case Token.RC:
			return "RC";
		case Token.LP:
			return "LP";
		case Token.RP:
			return "RP";
		case Token.COMMA:
			return "COMMA";
		case Token.ASSIGN:
			return "ASSIGN";
		case Token.ASSIGN_BITOR:
			return "ASSIGN_BITOR";
		case Token.ASSIGN_BITXOR:
			return "ASSIGN_BITXOR";
		case Token.ASSIGN_BITAND:
			return "ASSIGN_BITAND";
		case Token.ASSIGN_LSH:
			return "ASSIGN_LSH";
		case Token.ASSIGN_RSH:
			return "ASSIGN_RSH";
		case Token.ASSIGN_URSH:
			return "ASSIGN_URSH";
		case Token.ASSIGN_ADD:
			return "ASSIGN_ADD";
		case Token.ASSIGN_SUB:
			return "ASSIGN_SUB";
		case Token.ASSIGN_MUL:
			return "ASSIGN_MUL";
		case Token.ASSIGN_DIV:
			return "ASSIGN_DIV";
		case Token.ASSIGN_MOD:
			return "ASSIGN_MOD";
		case Token.HOOK:
			return "HOOK";
		case Token.COLON:
			return "COLON";
		case Token.OR:
			return "OR";
		case Token.AND:
			return "AND";
		case Token.INC:
			return "INC";
		case Token.DEC:
			return "DEC";
		case Token.DOT:
			return "DOT";
		case Token.FUNCTION:
			return "FUNCTION";
		case Token.EXPORT:
			return "EXPORT";
		case Token.IMPORT:
			return "IMPORT";
		case Token.IF:
			return "IF";
		case Token.ELSE:
			return "ELSE";
		case Token.SWITCH:
			return "SWITCH";
		case Token.CASE:
			return "CASE";
		case Token.DEFAULT:
			return "DEFAULT";
		case Token.WHILE:
			return "WHILE";
		case Token.DO:
			return "DO";
		case Token.FOR:
			return "FOR";
		case Token.BREAK:
			return "BREAK";
		case Token.CONTINUE:
			return "CONTINUE";
		case Token.VAR:
			return "VAR";
		case Token.WITH:
			return "WITH";
		case Token.CATCH:
			return "CATCH";
		case Token.FINALLY:
			return "FINALLY";
		case Token.RESERVED:
			return "RESERVED";
		case Token.EMPTY:
			return "EMPTY";
		case Token.BLOCK:
			return "BLOCK";
		case Token.LABEL:
			return "LABEL";
		case Token.TARGET:
			return "TARGET";
		case Token.LOOP:
			return "LOOP";
		case Token.EXPR_VOID:
			return "EXPR_VOID";
		case Token.EXPR_RESULT:
			return "EXPR_RESULT";
		case Token.JSR:
			return "JSR";
		case Token.SCRIPT:
			return "SCRIPT";
		case Token.TYPEOFNAME:
			return "TYPEOFNAME";
		case Token.USE_STACK:
			return "USE_STACK";
		case Token.SETPROP_OP:
			return "SETPROP_OP";
		case Token.SETELEM_OP:
			return "SETELEM_OP";
		case Token.LOCAL_BLOCK:
			return "LOCAL_BLOCK";
		case Token.SET_REF_OP:
			return "SET_REF_OP";
		case Token.DOTDOT:
			return "DOTDOT";
		case Token.COLONCOLON:
			return "COLONCOLON";
		case Token.XML:
			return "XML";
		case Token.DOTQUERY:
			return "DOTQUERY";
		case Token.XMLATTR:
			return "XMLATTR";
		case Token.XMLEND:
			return "XMLEND";
		case Token.TO_OBJECT:
			return "TO_OBJECT";
		case Token.TO_DOUBLE:
			return "TO_DOUBLE";
		case Token.LAST_TOKEN + 1:
			return "FUNCTION_END";
		}

		// Token without name
		return token + "";
		// throw new IllegalStateException(String.valueOf(token));
	}

}
