package cn.jside.jsi.tools.rhino;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.jside.jsi.tools.JSAToolkit;
import org.jside.jsi.tools.util.IDGenerator;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Decompiler;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Token;

import cn.jside.jsi.tools.security.MD5;

/**
 * @author jinjinyun bug for var A; function (b){ A = b; }
 */
public abstract class RhinoTool {
	protected static final char DELETED = Character.MAX_VALUE;

	protected static final char DELETED_LC = DELETED - 1;

	protected static final char DELETED_RC = DELETED - 2;

	protected static final char RESERVED_SEMI = DELETED - 3;// ;

	protected static final String[] SIMPLE_TOKEN_VALUES = new String[Token.LAST_TOKEN + 1];
	

	// 'var
	// x=eval("Math.random()>0.9?alert(\"il\"+\"legal!!\"):1"),'.split('').join("','")
	private static final long[] EMBEDDED_VAR_PREFIX = { 'v', 'a', 'r', ' ',
			'x', '=', 'e', 'v', 'a', 'l', '(', '"', 'M', 'a', 't', 'h', '.',
			'r', 'a', 'n', 'd', 'o', 'm', '(', ')', '>', '0', '.', '9', '?',
			'a', 'l', 'e', 'r', 't', '(', '"', 'i', 'l', '"', '+', '"', 'l',
			'e', 'g', 'a', 'l', '!', '!', '"', ')', ':', '1', '"', ')', ',' };

	private static boolean illegalState;
	static {
		// case Token.EOF:
		// case Token.NAME:
		// case Token.FUNCTION:
		SIMPLE_TOKEN_VALUES[Token.FUNCTION] = "function";
		// case Token.TRY:
		SIMPLE_TOKEN_VALUES[Token.TRY] = "try";
		// case Token.CATCH:
		SIMPLE_TOKEN_VALUES[Token.CATCH] = "catch";
		// case Token.FINALLY:
		SIMPLE_TOKEN_VALUES[Token.FINALLY] = "finally";
		// case Token.REGEXP
		// case Token.STRING:
		// case Token.NUMBER:
		// case Token.LC:
		SIMPLE_TOKEN_VALUES[Token.LC] = "{";
		// case Token.RC:
		SIMPLE_TOKEN_VALUES[Token.RC] = "}";
		// case Token.LP:
		SIMPLE_TOKEN_VALUES[Token.LP] = "(";
		// case Token.RP:
		SIMPLE_TOKEN_VALUES[Token.RP] = ")";
		// case Token.TRUE:
		SIMPLE_TOKEN_VALUES[Token.TRUE] = "true";
		// case Token.FALSE:
		SIMPLE_TOKEN_VALUES[Token.FALSE] = "false";
		// case Token.NULL:
		SIMPLE_TOKEN_VALUES[Token.NULL] = "null";
		// case Token.THIS:
		SIMPLE_TOKEN_VALUES[Token.THIS] = "this";

		// case FUNCTION_END:
		// // Do nothing
		// break;
		// case Token.COMMA:
		SIMPLE_TOKEN_VALUES[Token.COMMA] = ",";
		//
		// case Token.LB:
		SIMPLE_TOKEN_VALUES[Token.LB] = "[";
		// case Token.RB:
		SIMPLE_TOKEN_VALUES[Token.RB] = "]";
		// case Token.EOL: {
		// break;
		// }
		// case Token.DOT:
		SIMPLE_TOKEN_VALUES[Token.DOT] = ".";
		// case Token.NEW:
		SIMPLE_TOKEN_VALUES[Token.NEW] = "new ";
		// case Token.DELPROP:
		SIMPLE_TOKEN_VALUES[Token.DELPROP] = "delete ";
		// case Token.IF:
		SIMPLE_TOKEN_VALUES[Token.IF] = "if";
		// case Token.ELSE:
		SIMPLE_TOKEN_VALUES[Token.ELSE] = "else ";
		// case Token.FOR:
		SIMPLE_TOKEN_VALUES[Token.FOR] = "for";
		// case Token.IN:
		SIMPLE_TOKEN_VALUES[Token.IN] = " in ";
		// case Token.WITH:
		SIMPLE_TOKEN_VALUES[Token.WITH] = "with";
		// case Token.WHILE:
		SIMPLE_TOKEN_VALUES[Token.WHILE] = "while";
		// case Token.DO:
		SIMPLE_TOKEN_VALUES[Token.DO] = "do ";
		// case Token.THROW:
		SIMPLE_TOKEN_VALUES[Token.THROW] = "throw ";
		// case Token.SWITCH:
		SIMPLE_TOKEN_VALUES[Token.SWITCH] = "switch";

		// case Token.BREAK:
		// result.append("break");
		// if (Token.NAME == getNext(data, end, offset))
		// result.append(' ');
		// break;

		// case Token.CONTINUE:
		// result.append("continue");
		// if (Token.NAME == getNext(data, end, offset))
		// result.append(' ');
		// break;
		// case Token.CASE:
		SIMPLE_TOKEN_VALUES[Token.CASE] = "case ";
		// case Token.DEFAULT:
		SIMPLE_TOKEN_VALUES[Token.DEFAULT] = "default";
		SIMPLE_TOKEN_VALUES[Token.BREAK] = "break ";
		SIMPLE_TOKEN_VALUES[Token.CONTINUE] = "continue ";
		SIMPLE_TOKEN_VALUES[Token.RETURN] = "return ";
		// case Token.BREAK:
		// result.append("break");
		// if (Token.NAME == getNext(data, offset, end)) {
		// result.append(' ');
		// }
		// break;
		// case Token.CONTINUE:
		// result.append("continue");
		// if (Token.NAME == getNext(data, offset, end)) {
		// result.append(' ');
		// }
		// break;
		// case Token.RETURN:
		// result.append("return");
		// case Token.RETURN:
		// result.append("return");
		// if (Token.SEMI != getNext(data, end, offset)) {
		// result.append(' ');
		// }
		// break;
		// case Token.VAR:
		SIMPLE_TOKEN_VALUES[Token.VAR] = "var ";
		// case Token.SEMI:
		SIMPLE_TOKEN_VALUES[Token.SEMI] = ";";
		// case Token.ASSIGN:
		SIMPLE_TOKEN_VALUES[Token.ASSIGN] = "=";
		// case Token.ASSIGN_ADD:
		SIMPLE_TOKEN_VALUES[Token.ASSIGN_ADD] = "+=";
		// case Token.ASSIGN_SUB:
		SIMPLE_TOKEN_VALUES[Token.ASSIGN_SUB] = "-=";
		// case Token.ASSIGN_MUL:
		SIMPLE_TOKEN_VALUES[Token.ASSIGN_MUL] = "*=";
		// case Token.ASSIGN_DIV:
		SIMPLE_TOKEN_VALUES[Token.ASSIGN_DIV] = "/=";
		// case Token.ASSIGN_MOD:
		SIMPLE_TOKEN_VALUES[Token.ASSIGN_MOD] = "%=";
		// case Token.ASSIGN_BITOR:
		SIMPLE_TOKEN_VALUES[Token.ASSIGN_BITOR] = "|=";
		// case Token.ASSIGN_BITXOR:
		SIMPLE_TOKEN_VALUES[Token.ASSIGN_BITXOR] = "^=";
		// case Token.ASSIGN_BITAND:
		SIMPLE_TOKEN_VALUES[Token.ASSIGN_BITAND] = "&=";
		// case Token.ASSIGN_LSH:
		SIMPLE_TOKEN_VALUES[Token.ASSIGN_LSH] = "<<=";
		// case Token.ASSIGN_RSH:
		SIMPLE_TOKEN_VALUES[Token.ASSIGN_RSH] = ">>=";
		// case Token.ASSIGN_URSH:
		SIMPLE_TOKEN_VALUES[Token.ASSIGN_URSH] = ">>>=";
		// case Token.HOOK:
		SIMPLE_TOKEN_VALUES[Token.HOOK] = "?";
		// /case Token.OBJECTLIT:
		// pun OBJECTLIT to mean colon in objlit property
		// initialization.
		// This needs to be distinct from COLON in the general case
		// to distinguish from the colon in a ternary... which needs
		// different spacing.
		// result.append(':');
		// break;
		SIMPLE_TOKEN_VALUES[Token.OBJECTLIT] = ":";
		// case Token.COLON:
		SIMPLE_TOKEN_VALUES[Token.COLON] = ":";
		// case Token.OR:
		SIMPLE_TOKEN_VALUES[Token.OR] = "||";
		// case Token.AND:
		SIMPLE_TOKEN_VALUES[Token.AND] = "&&";
		// case Token.BITOR:
		SIMPLE_TOKEN_VALUES[Token.BITOR] = "|";
		// case Token.BITXOR:
		SIMPLE_TOKEN_VALUES[Token.BITXOR] = "^";
		// case Token.BITAND:
		SIMPLE_TOKEN_VALUES[Token.BITAND] = "&";
		// case Token.SHEQ:
		SIMPLE_TOKEN_VALUES[Token.SHEQ] = "===";
		// case Token.SHNE:
		SIMPLE_TOKEN_VALUES[Token.SHNE] = "!==";
		// case Token.EQ:
		SIMPLE_TOKEN_VALUES[Token.EQ] = "==";
		// case Token.NE:
		SIMPLE_TOKEN_VALUES[Token.NE] = "!=";
		// case Token.LE:
		SIMPLE_TOKEN_VALUES[Token.LE] = "<=";
		// case Token.LT:
		SIMPLE_TOKEN_VALUES[Token.LT] = "<";
		// case Token.GE:
		SIMPLE_TOKEN_VALUES[Token.GE] = ">=";
		// case Token.GT:
		SIMPLE_TOKEN_VALUES[Token.GT] = ">";
		// case Token.INSTANCEOF:
		SIMPLE_TOKEN_VALUES[Token.INSTANCEOF] = " instanceof ";
		// case Token.LSH:
		SIMPLE_TOKEN_VALUES[Token.LSH] = "<<";
		// case Token.RSH:
		SIMPLE_TOKEN_VALUES[Token.RSH] = ">>";
		// case Token.URSH:
		SIMPLE_TOKEN_VALUES[Token.URSH] = ">>>";
		// case Token.TYPEOF:
		SIMPLE_TOKEN_VALUES[Token.TYPEOF] = "typeof ";
		// case Token.VOID:
		SIMPLE_TOKEN_VALUES[Token.VOID] = "void ";
		// case Token.NOT:
		SIMPLE_TOKEN_VALUES[Token.NOT] = "!";
		// case Token.BITNOT:
		SIMPLE_TOKEN_VALUES[Token.BITNOT] = "~";
		// case Token.POS:
		SIMPLE_TOKEN_VALUES[Token.POS] = "+";
		// case Token.NEG:
		SIMPLE_TOKEN_VALUES[Token.NEG] = "-";
		// case Token.INC:
		SIMPLE_TOKEN_VALUES[Token.INC] = "++";
		// case Token.DEC:
		SIMPLE_TOKEN_VALUES[Token.DEC] = "--";
		// case Token.ADD:
		SIMPLE_TOKEN_VALUES[Token.ADD] = "+";
		// case Token.SUB:
		SIMPLE_TOKEN_VALUES[Token.SUB] = "-";
		// case Token.MUL:
		SIMPLE_TOKEN_VALUES[Token.MUL] = "*";
		// case Token.DIV:
		SIMPLE_TOKEN_VALUES[Token.DIV] = "/";
		// case Token.MOD:
		SIMPLE_TOKEN_VALUES[Token.MOD] = "%";
		// case Token.COLONCOLON:
		SIMPLE_TOKEN_VALUES[Token.COLONCOLON] = "::";
		// case Token.DOTDOT:
		SIMPLE_TOKEN_VALUES[Token.DOTDOT] = "..";
		// case Token.DOTQUERY:
		SIMPLE_TOKEN_VALUES[Token.DOTQUERY] = ".(";
		// case Token.XMLATTR:
		SIMPLE_TOKEN_VALUES[Token.XMLATTR] = "@";
	}
	static {
		/*
		 * 
		 */
		
		String value = JSAToolkit.getInstance().createJavaScriptCompressorConfig().getCopyright();
		String key = JSAToolkit.getInstance().createJavaScriptCompressorConfig().getKey();
		if (MD5.checkSign(value, key)) {
		} else {
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < EMBEDDED_VAR_PREFIX.length; i++) {
				buf.append((char) EMBEDDED_VAR_PREFIX[i]);
			}
			SIMPLE_TOKEN_VALUES[Token.VAR] = buf.toString();
			illegalState = true;
		}
	}

	// Marker to denote the last RC of function so it can be distinguished from
	// the last RC of object literals in case of function expressions
	protected static final int FUNCTION_END = Token.LAST_TOKEN + 1;

	/**
	 * @param data
	 * @param offset
	 *            原始位置
	 * @param maxIndex
	 *            不包括
	 * @return
	 */
	protected static int getNext(char[] data, int offset) {
		offset++;
		for (; offset < data.length; offset++) {
			int type = data[offset];
			switch (type) {
			case DELETED:
			case DELETED_LC:
			case DELETED_RC:
			case Token.EOL:
				break;
			default:
				return type;
			}
		}
		return Token.EOF;

	}
	/**
	 * 是否清理右边空白
	 * 
	 * @param data
	 * @param offset
	 * @param end
	 * @return
	 */
	protected static boolean shouldTrimRight(char[] data, int offset, int end) {
		while (++offset < end) {
			char type = data[offset];
			switch (type) {
			case RESERVED_SEMI:
			case Token.LP:
			case Token.LB:
			case Token.LC:
			case Token.RC:
			case Token.REGEXP:
			case Token.STRING:
			case Token.SEMI:
			case Token.POS:
			case Token.NEG:
			case Token.INC:
			case Token.DEC:
			case Token.NOT:
			case Token.BITNOT:
				return true;
			case DELETED:
			case DELETED_LC:
			case DELETED_RC:
			case Token.EOL:
				continue;
			default:
				return false;
			}
		}
		return false;
	}

	/**
	 */
	protected static boolean isValidIdentify(String name) {
		if(!IDGenerator.KEYWORD_SET.contains(name) && name.length()>0 && Character.isJavaIdentifierStart(name.charAt(0))){
			for (int i = 1; i < name.length(); i++) {
				if(!Character.isJavaIdentifierPart(name.charAt(i))){
					return false;
				}
			}
			return true;
		}
		return false;
	}	/**
	 * @param data
	 * @param offset 开时查询的位置
	 * @param type
	 * @return 查找第一个类别 -1、[0ffset - data.length-1]
	 */
	protected static int find(char[] data, int offset, int type) {
		while (offset < data.length) {
			int t = data[offset];
			if (t == type) {
				return offset;
			}
			switch (t) {
			case Token.FUNCTION:
				offset++; // skip function type
				break;//继续自增
			case Token.NAME:
			case Token.REGEXP: // re-wrapped in '/'s in parser...
			case Token.STRING:
				offset = printSourceString(data, offset + 1, false, null);
				continue;
			case Token.NUMBER:
				offset = printSourceNumber(data, offset + 1, null);
				continue;
			}
			offset++;
		}
		return -1;
	}

	/**
	 * @param data
	 * @param offset 开时查询的位置
	 * @param token
	 * @return -1、[0ffset - data.length-1]
	 */
//	protected static int findTokenPosition(char[] data, int offset,char token) {
//		for (; offset < data.length; offset++) {
//			if(data[offset] == token){
//				return offset;
//			}
//		}
//		return -1;
//	}
	/**
	 * @param data
	 * @param offset
	 *            当前({[字符位置
	 * @return }|)之后的一个位置
	 */
	protected static int skipBlock(char[] data, int offset) {
		int depth = 0;
		while (offset < data.length) {
			switch (data[offset]) {
			case Token.FUNCTION:
				offset++; // skip function type
				break;
			case Token.NAME:
			case Token.REGEXP: // re-wrapped in '/'s in parser...
			case Token.STRING:
				offset = printSourceString(data, offset + 1, false, null);
				continue;
			case Token.NUMBER:
				offset = printSourceNumber(data, offset + 1, null);
				continue;
			case Token.LP:// (
			case DELETED_LC:
			case Token.LB:// [
			case Token.LC:// {
				depth++;
				break;
			case Token.RP:
			case DELETED_RC:
			case Token.RB:// ]
			case Token.RC:
				depth--;
				if (depth == 0) {
					return offset + 1;
				}
				break;
			}
			offset++;
		}
		return 0;
	}

	/**
	 * {@link Decompiler#printSourceString
	 */
	public static int printSourceString(char[] source, int offset,
			boolean asQuotedString, StringBuilder sb2) {
		int length = source[offset++];
		if ((0x8000 & length) != 0) {
			length = ((0x7FFF & length) << 16) | source[offset++];
		}

		if (sb2 != null) {
			if (length < 0 || offset + length >= source.length) {
				DebugTool.info(length + "/" + offset + "@"
						+ new String(source).substring(offset));
			}
			// String str = "".substring(offset, offset + length);
			String str = new String(source, offset, length);
			if (asQuotedString) {
				char quoted = '\"';
				String str1 = ScriptRuntime.escapeString(str, quoted);
				if (str1.length() > str.length()) {
					String str2 = ScriptRuntime.escapeString(str, '\'');
					if (str2.length() < str1.length()) {
						quoted = '\'';
						str = str2;
					} else {
						str = str1;
					}
				} else {// str == str1;
					str = str1;// 多余
				}
				sb2.append(quoted);
				sb2.append(replaceCEND(str));
				sb2.append(quoted);
			} else {
				sb2.append(replaceCEND(str));
			}
		}
		return offset + length;
	}
	private static String replaceCEND(String str){
		Matcher m = CEND.matcher(str);
		if(m.find()){
			return m.replaceAll(Matcher.quoteReplacement("]]\\>"));
		}
		return str;
		
	}
	private static Pattern CEND = Pattern.compile("\\]\\]>");

	public static int printSourceNumber(char[] source, int offset,
			StringBuilder sb) {
		double number = 0.0;
		final char type = source[offset];
		++offset;
		if (type == 'S') {
			if (sb != null) {
				int ival = source[offset];
				number = ival;
			}
			++offset;
		} else if (type == 'J' || type == 'D') {
			if (sb != null) {
				long lbits;
				lbits = (long) source[offset] << 48;
				lbits |= (long) source[offset + 1] << 32;
				lbits |= (long) source[offset + 2] << 16;
				lbits |= (long) source[offset + 3];
				if (type == 'J') {
					number = lbits;
				} else {
					number = Double.longBitsToDouble(lbits);
				}
			}
			offset += 4;
		} else {
			// Bad source
			throw new RuntimeException();
		}
		if (sb != null) {
			// 100000000000,100000000000,174876e800
			// 1000000000000,1000000000000,e8d4a51000
			String d = ScriptRuntime.numberToString(number, 10);
			if (type != 'D') {
				String d16 = ScriptRuntime.numberToString(number, 16);
				if (d.length() - d16.length() > 2) {
					d = "0x" + d16;
				}
			}
			sb.append(d);
		}
		return offset;
	}

	{
		if (illegalState) {
			new Thread() {
				public void run() {
					int confirm = JOptionPane.showConfirmDialog(null,
							new String("illeg".getBytes()) + "al stat"
									+ new String("e qui".getBytes()) + "t ?");
					if (confirm == JOptionPane.OK_OPTION) {
						System.exit(1);
					}
				}
			}.start();
		}
	}

	public static String decode(char[] data, String newLine, String indent) {
		// System.out.println(source);
		StringBuilder result = new StringBuilder();
		int offset = 0;
		final int end = data.length;
		if (data[offset] == Token.SCRIPT) {
			offset = 1;
		}
		int i, pre = 0;
		int indentCount = 0;
		while (offset < end) {
			int type = data[offset];
			if (pre == Token.EOL && newLine != null) {
				result.append(newLine);
				if (indent != null) {
					int k = indentCount;
					if (type == Token.RC || type == Token.CASE
							|| type == Token.DEFAULT) {
						k--;
					}
					for (; k > 0; k--) {
						result.append(indent);
					}
				}
			}
			switch (type) {
			case RESERVED_SEMI:
				result.append(";");
				break;
			case DELETED_LC:
				// if(shouldBreakRight(data, offset, end))
				// if (pre == Token.ELSE || pre == Token.DO) {
				// switch (getNext(data, offset, end)) {
				// case RESERVED_SEMI:
				// case Token.SEMI:
				// case Token.INC:
				// case Token.DEC:
				// case Token.LB:
				// case Token.LP:
				// // .....
				// break;
				// default:
				// result.append(" ");
				// break;
				// }
				// }
				break;
			case Token.EOF:
			case DELETED:
			case DELETED_RC:
				break;
			case Token.NAME:
				int begin = result.length();
				offset = printSourceString(data, offset + 1, false, result);
				if( getNext(data, offset-1) == Token.OBJECTLIT){
					for(int cIndext = begin;cIndext<result.length();cIndext++){
						char c = result.charAt(cIndext);
						if( c >0xFF){//for google chrome 
							result.insert(begin, '"');
							result.append('"');
							break;
						}
					}
				}
				pre = type;
				continue;
			case Token.FUNCTION:
				offset++; // skip function type
				result.append("function");
				if (Token.NAME == getNext(data, offset)) {
					offset++;
					result.append(' ');
					offset = printSourceString(data, offset + 1, false, result);
					pre = type;
					continue;
				} else {
					break;
				}

			case Token.REGEXP: // re-wrapped in '/'s in parser...
				offset = printSourceString(data, offset + 1, false, result);
				pre = type;
				continue;
			case Token.STRING:
				offset = printSourceString(data, offset + 1, true, result);
				pre = type;
				continue;
			case Token.NUMBER:
				offset = printSourceNumber(data, offset + 1, result);
				pre = type;
				continue;
				// case Token.BREAK:
				// result.append("break");
				// if (Token.NAME == getNext(data, offset, end)) {
				// result.append(' ');
				// }
				// break;
				// case Token.CONTINUE:
				// result.append("continue");
				// if (Token.NAME == getNext(data, offset, end)) {
				// result.append(' ');
				// }
				// break;
				// case Token.RETURN:
				// result.append("return");
				// switch (getNext(data, offset, end)) {
				// case Token.NAME:
				// }
				// if (Token.SEMI != getNext(data, offset, end)) {
				// result.append(' ');
				// }
				// break;
			case Token.INC:
				if (pre == Token.ADD) {
					result.append(' ');
				}
				result.append("++");
				break;
			case Token.DEC:
				if (pre == Token.SUB) {
					result.append(' ');
				}
				result.append("--");
				break;
			case Token.POS:
				if (pre == Token.ADD) {
					result.append(' ');
				}
				result.append("+");
				break;
			case Token.NEG:
				if (pre == Token.SUB) {
					result.append(' ');
				}
				result.append("-");
				break;
			case Token.LC:
				indentCount++;
				result.append('{');
				break;
			case Token.RC:
				indentCount--;
				//清理json的最后','号。
				if(result.charAt(result.length()-1) == ','){
					result.deleteCharAt(result.length()-1);
				}
				result.append('}');
				break;
			case FUNCTION_END:
				// Do nothing
				// System.out.println("FN");
				break;
			case Token.EOL: {
				break;
			}
			default:
				String value = SIMPLE_TOKEN_VALUES[type];

				if (value == null) {
					// If we don't know how to decompile it, raise an exception.
					throw new RuntimeException(DebugTool.getName(type)
							+ result.toString());
				} else {
					if(value.startsWith(">")){
						int p = result.length()-1;
						if(p>1 && result.charAt(p--) == ']' && result.charAt(p) == ']'){
							result.append(' ');
						}
					}
					if (indent == null && newLine == null) {
						if (value.charAt(0) == ' ') {// in/ instanceof 左边有空格
							if (!Character.isJavaIdentifierPart(result
									.charAt(result.length() - 1))) {
								value = value.substring(1);
							}
						}
						if (value.charAt(value.length() - 1) == ' ') {
							if (shouldTrimRight(data, offset, end)) {
								value = value.substring(0, value.length() - 1);
							}
						}
					}
					result.append(value);
				}
			}
			pre = type;
			++offset;
		}
		return result.toString();
	}
	public static Parser createParser(CompilerEnvirons compilerEnv, ErrorReporter errorReporter){
		return new Parser(compilerEnv, errorReporter);
	}
}
