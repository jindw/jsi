package cn.jside.jsi.tools.rhino;

import org.jside.jsi.tools.JavaScriptCompressionAdvisor;
import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * @author jinjinyun bug for var A; function (b){ A = b; }
 */
public class Delete2Compressor extends RhinoTool {
	private Parser parser;

	public Delete2Compressor(Parser parser) {
		this.parser = parser;
	}

	public synchronized String compress(String source,
			JavaScriptCompressionAdvisor advicor) {
		ScriptOrFnNode root = parser.parse(source, "", 1);

		char[] data = parser.getEncodedSource().toCharArray();
		this.deleteCurlyBracketsAndSemicolon(data, data[0] == Token.SCRIPT ? 1
				: 0, data.length);
		String result = RhinoTool.decode(data, null, null);

		// result = this.文本处理(result);
		return result;
	}

	/**
	 * 删除多余大括弧及多余分号
	 * 
	 * @param data
	 * @param offset
	 * @param end
	 */
	protected void deleteCurlyBracketsAndSemicolon(char[] data, int offset,
			int end) {
		int preDataIndexEnd = -1;
		while (offset < end) {
			char type = data[offset];
			switch (type) {
			case Token.WHILE:
			case Token.IF:
			case Token.FOR:
				int offset2 = skipBlock(data, offset);// ->{
				deleteCurlyBracketsAndSemicolon(data, offset + 1, offset2);
				offset = offset2;
				this.deleteCurlyBrackets(data, offset, type);
				break;
			case Token.DO:// IE 的do while 有bug。避免bug，不删除扩符号
				offset++;
				this.processDoWhile(data, offset, type);
				break;
			case Token.ELSE: // re-wrapped in '/'s in parser...
				offset++;
				this.deleteCurlyBrackets(data, offset, type);
				break;

			case Token.EOL:// eof
				if (offset + 1 < data.length) {
					break;
				}
			case Token.EOF:
			case Token.RC:// }
				this.deleteSemicolonBeforeCurlyBrackets(data, offset,
						preDataIndexEnd);
				break;
			case Token.REGEXP: // re-wrapped in '/'s in parser...
			case Token.STRING:
			case Token.NAME:
				offset = printSourceString(data, offset + 1, false, null);
				preDataIndexEnd = offset;
				continue;
			case Token.NUMBER:
				offset = printSourceNumber(data, offset + 1, null);
				preDataIndexEnd = offset;
				continue;
			case Token.FUNCTION:
				offset++; // skip function type
				break;
			// case FUNCTION_END:
			// break;

			}
			++offset;
		}
	}

	/**
	 * 处理闭块分号
	 * 
	 * @param data
	 * @param offset
	 * @param preDataIndexEnd
	 *            不能回退到数据区域，否则无法意料！！
	 */
	private final void deleteSemicolonBeforeCurlyBrackets(char[] data,
			int offset, int preDataIndexEnd) {
		while (offset > preDataIndexEnd) {
			offset--;
			if (data[offset] == Token.EOL) {
				continue;
			} else if (data[offset] == DELETED) {
				continue;
			} else if (data[offset] == DELETED_LC) {
				continue;
			} else if (data[offset] == DELETED_RC) {
				continue;
			} else if (data[offset] == Token.SEMI) {
				data[offset] = DELETED;
				break;
			} else {
				break;
			}
		}
	}

	/**
	 * IE 的do while 有bug。避免bug，不删除扩符号。同时间，强制删除while后分号。
	 * 
	 * @param data
	 * @param offset
	 * @param type
	 */
	private final void processDoWhile(char[] data, int offset, char type) {
		offset = skipBlock(data, offset);
		offset = skipBlock(data, offset);
		data[offset] = DELETED;
	}

	/**
	 * 删除空{} }后如果有else且，他的if钱是if块，不能删除，语义歧义
	 * 
	 * @param data
	 * @param offset
	 * @param type
	 */
	private final void deleteCurlyBrackets(char[] data, int offset, char type) {
		if (data[offset] != Token.LC) {
			return;
		}
		if (getNext(data, offset + 1) == Token.RC) {// 空块
			if (type == Token.ELSE) {// 如果是空 Else 全部删除
				data[offset - 1] = DELETED;// 删除else
				data[offset] = DELETED_LC;
				data[offset + 1] = DELETED;
				data[offset + 2] = DELETED_RC;
			} else {// 置换为空行
				data[offset] = DELETED_LC;
				data[offset + 1] = RESERVED_SEMI;
				data[offset + 2] = DELETED_RC;
			}
			return;
		}
		final int begin = offset;// {
		offset += 2;// skip } EOL
		while (offset < data.length) {
			switch (data[offset]) {
			case Token.REGEXP: // re-wrapped in '/'s in parser...
			case Token.STRING:
			case Token.NAME:
				offset = printSourceString(data, offset + 1, false, null);
				continue;
			case Token.NUMBER:
				offset = printSourceNumber(data, offset + 1, null);
				continue;
			case Token.FUNCTION:
				offset++; // skip function type
				break;
			case Token.LP:// (
			case Token.LC:// {
				offset = skipBlock(data, offset);
				continue;
			case Token.EOL:// \n
				if (getNext(data, offset) == Token.RC) {// 单行块
					if (type == Token.IF
							&& getNext(data, offset + 1) == Token.ELSE
					// && getNext(data, data.length, begin+1) == Token.IF
					// IF|WHILE|FOR
					) {
						// 特殊情况 if if else
						if (shouldReserveCurlyBracketsOfIfWithElse(data, begin,
								offset + 1)) {// 存在保露的if/else即需保留
							return;
						}
					}
					data[begin] = DELETED_LC;
					data[offset + 1] = DELETED_RC;
					return;
				} else {
					// 多行块
					return;
				}
			}
			offset++;
		}
	}

	/**
	 * 需要保留IF括弧。 目前的版本还处在检查if else边界的阶段. 而事实上这不是否需要呢？
	 * 
	 * if(b1){if(b2){x()}}else{x()} 确认需要保护外层{}否则歧义
	 * 
	 * if(b1){
	 *   if(b2){
	 *     i++;
	 *   }else if(b3){
	 *     i++;
	 *   }
	 * }else{
	 *   i++;
	 * }
	 * 确认需要保护外层{}否则歧义
	 * 
	 * @param data
	 * @param lc if{<lc>...}<rc>else{....}
	 * @param rc
	 * @return
	 * @example var i=0;if(!i){while(i++<5);}else i--;alert(i);
	 */
	private final boolean shouldReserveCurlyBracketsOfIfWithElse(char[] data,
			int lc, int rc) {
		// 有待优化getNext(data, offset + 1) == Token.ELSE
		switch (getNext(data, lc )) {//rc+1 亦可
		case Token.IF:// 这个按理说也没什么吧？
			// DebugTool.print(getNext(data, lc ));
			int end = skipBlock(data, lc + 1);// )后{
			end = skipBlock(data, end);// }<eol:pos>
			if (end + 1 == rc) {
				// TODO:理应进一步判断
				return true;
			}else{//else 里面再有if，算了，饶了我吧：（
				return true;
			}
			
		case Token.FOR://
		case Token.WHILE:// 空while永远是个麻烦事
		case Token.DO:// IE有个do while bug
			// return true;
		}
		return false;
	}

	public void setCompressorConfig(JavaScriptCompressorConfig config) {
	}
}
