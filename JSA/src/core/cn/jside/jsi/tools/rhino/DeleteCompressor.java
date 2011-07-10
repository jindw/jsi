package cn.jside.jsi.tools.rhino;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.jside.jsi.tools.JavaScriptCompressionAdvisor;
import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;

/**
 * @author jinjinyun bug for var A; function (b){ A = b; }
 * 
 * 
 * //examples if("$debug"){ alert(1) alert(1) }else{ alert(2) }
 * 
 * function(x,b,c){ var x=2,b=3; var c=4,d=5; }
 * 
 * $log.deb4ug("1",3,function(){ }); $log.trac5e("1",3,function(){
 * $log.debug("1",3,function(){ }); $log.trace("1",3,function(){ });
 * $log.deb4ug("1",3,function(){ }); $log.trac5e("1",3,function(){ }); });
 * $log.trace("1",3,function(){ $log.debug("1",3,function(){ });
 * $log.trace("1",3,function(){ }); $log.deb4ug("1",3,function(){ });
 * $log.trac5e("1",3,function(){ }); });
 */
public class DeleteCompressor extends RhinoTool {
	private Parser parser;
	// private GlobalReplacerHolder globalReplacerHolder =
	// GlobalReplacerHolder.EMPTY;
	private Collection<String> debugCalls;
	private JavaScriptCompressorConfig config;

	public DeleteCompressor(Parser parser) {
		this.parser = parser;
	}

	public synchronized String compress(String source, JavaScriptCompressionAdvisor globalReplacer) {
		parser.parse(source, "", 1);

		char[] data = parser.getEncodedSource().toCharArray();
		this.debugCalls = this.config.getDebugCalls();
//		if(!this.featureFlags.contains(JavaScriptCompressorConfig.FEATURE_JSI_LOG)){
//			this.debugCalls = new HashSet<String>(this.debugCalls);
//			this.debugCalls.add(JavaScriptCompressorConfig.DEBUG_CALL_LOG_FATAL);
//			this.debugCalls.add(JavaScriptCompressorConfig.DEBUG_CALL_LOG_ERROR);
//			this.debugCalls.add(JavaScriptCompressorConfig.DEBUG_CALL_LOG_INFO);
//			this.debugCalls.add(JavaScriptCompressorConfig.DEBUG_CALL_LOG_DEBUG);
//			this.debugCalls.add(JavaScriptCompressorConfig.DEBUG_CALL_LOG_TRACE);
//		}

		this.doDeleteDebugs(data);
		this.doDeleteEmptyTry(data);
		this.doDeleteVars(data);
		String result = RhinoTool.decode(data, null, null);
		// result = this.文本处理(result);
		return result;
	}

	/**
	 * @param data
	 */
	private void doDeleteEmptyTry(char[] data) {
		// DebugTool.printData(data, 0, data.length);
		int offset = 0;
		ArrayList<Integer> tryList = new ArrayList<Integer>();
		while (offset < data.length) {
			switch (data[offset]) {
			case Token.TRY:
				tryList.add(offset);
				if (getNext(data, offset + 1) == Token.RC) {// 空Catch Token.LC
					// Token.EOL
					// Token.RC
					int pos = find(data, offset, Token.RC);
					if (getNext(data, pos) == Token.CATCH) {// catch|finally
						pos = skipBlock(data, skipBlock(data, pos + 1));// 连续跳越
						// DebugTool.println("#");
						// DebugTool.print(data[pos-1]);
						// carch(){}两块
					} else {
						pos++;
					}
					for (; offset < pos; offset++) {
						data[offset] = DELETED;
					}
					// data[offset++] = DELETED;
					// offset == pos == pos:Token.EOL
					if (getNext(data, offset) == Token.FINALLY) {// Token.EOL
						// Token.FINALLY
						pos = find(data, offset, Token.FINALLY);
						for (; offset < pos; offset++) {
							data[offset] = DELETED;
						}
						pos = skipBlock(data, pos);// 跳过Finally
						data[offset++] = DELETED;// delete Token.FINALLY,指向
						// Token.LC
						data[offset] = DELETED;
						data[pos - 1] = DELETED;
					}
				}
				break;// 跳出自增
			case Token.CATCH:
				// 如果仅仅是throw e;且无final 踢除 try
			{
				int p = offset;
				p++;//(;
				p++;//name
				StringBuilder buf;
				p = printSourceString(data, p + 1, false, buf = new StringBuilder());
				String catchVar = buf.toString();
				p++;//)
				p++;//{
				p++;//\n
				if(data[p] == Token.THROW){
					p++;//name
					p = printSourceString(data, p + 1, false, buf = new StringBuilder());
					String throwVar = buf.toString();
					if(throwVar.equals(catchVar) && data[p] == Token.SEMI){
						//hit;
						p=skipBlock(data, offset);
						p=skipBlock(data, p);
						for (int i = offset; i < p; i++) {
							data[i] = DELETED;
						}
						if(data[Math.min(p+1,data.length-1)] != Token.FINALLY){//\n\finally
							while (true) {
								int tryPos = tryList.remove(tryList.size() - 1);
								int checkPos = skipBlock(data, tryPos);//
								checkPos ++;//\n
								if(checkPos == offset){
									//hit
									data[offset-2] = DELETED;//}
									data[offset-1] = DELETED;//n
									data[tryPos] = DELETED;//try
									data[tryPos+1] = DELETED;//{
									data[tryPos+2] = DELETED;//\n
									break;
								}
							}
						}
						offset = p;
						continue;
					}
				}
				offset++;
				continue;
			}
			case Token.NAME:
				offset = printSourceString(data, offset + 1, false, null);
				continue;
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
	}

	private void doDeleteDebugs(char[] data) {
		int offset = 0;
		while (offset < data.length) {
			StringBuilder name;
			switch (data[offset]) {
			case Token.NAME: {
				int offset2 = printSourceString(data, offset + 1, false,
						name = new StringBuilder());
				while (data[offset2] == Token.DOT
						&& data[offset2 + 1] == Token.NAME) {
					name.append('.');
					offset2 = printSourceString(data, offset2 + 2, false, name);
				}
				if (data[offset2] == Token.LP
						&& debugCalls.contains(name.toString())) {
					offset2 = RhinoTool.skipBlock(data, offset2);
					while (offset < offset2) {
						data[offset] = DELETED;
						offset++;
					}
				}
				// DebugTool.print(data[offset2]);
				offset = offset2;
				continue;
			}
			case Token.REGEXP: // re-wrapped in '/'s in parser...
			case Token.STRING:
				offset = printSourceString(data, offset + 1, false, null);
				continue;
			case Token.NUMBER:
				offset = printSourceNumber(data, offset + 1, null);
				continue;
			case Token.FUNCTION:
				offset++; // skip function type
				// 一定要特殊处理，以免与函数调用混淆
				if (getNext(data, offset) == Token.NAME) {
					offset++;
					offset = printSourceString(data, offset + 1, false, null);
					continue;
				} else {
					break;
				}
			case Token.IF:// if("$debug")
				if ((data[offset + 1] == Token.LP)) {
					final int offsetValueBegin;
					final boolean isNot = data[offset + 2] == Token.NOT;
					if (isNot) {
						offsetValueBegin = offset + 3;
					} else {
						offsetValueBegin = offset + 2;
					}
					if (data[offsetValueBegin] == Token.STRING) {//特征编译
						int offsetStringEnd = printSourceString(data,
								offsetValueBegin + 1, false,
								name = new StringBuilder());
						if (data[offsetStringEnd] == Token.RP) {
							// 踢除if
							boolean deleteIf = !config.containsFeature(name
									.toString());
									//^ isNot;
							offset = deleteIfElse(data, offset,
									offsetStringEnd + 1, deleteIf);
							continue;
						}
					} else if (data[offsetValueBegin] == Token.NUMBER) {//静态优化
						int offsetNumberEnd = printSourceNumber(data,
								offsetValueBegin + 1,
								name = new StringBuilder());
						if (data[offsetNumberEnd] == Token.RP) {
							// 踢除if
							boolean deleteIf = name.toString().equals("0")
									^ isNot;
							offset = deleteIfElse(data, offset,
									offsetNumberEnd + 1, deleteIf);
							continue;
						}
					} else if (data[offsetValueBegin] == Token.FALSE
							|| data[offsetValueBegin] == Token.TRUE) {//静态优化
						int offsetBooleanEnd = offsetValueBegin + 1;
						if (data[offsetBooleanEnd] == Token.RP) {
							// 踢除if
							boolean deleteIf = data[offsetValueBegin] == Token.FALSE
									^ isNot;
							offset = deleteIfElse(data, offset,
									offsetBooleanEnd + 1, deleteIf);
							continue;
						}
					}
				}
				break;// ++
			}

			++offset;
		}
	}

	private int deleteIfElse(char[] data, int offset, int ifBlockStart,
			boolean deleteIf) {
		final int ifBlockEnd = RhinoTool.skipBlock(data, ifBlockStart);
		if (deleteIf) {
			while (offset < ifBlockEnd) {
				data[offset] = DELETED;
				offset++;
			}
			// offset == elseBlockEnd
			if (data[offset] == Token.ELSE) {
				int elseBlockEnd = RhinoTool.skipBlock(data, offset);
				data[offset++] = DELETED;// else
				data[offset++] = DELETED;// {
				data[elseBlockEnd - 1] = DELETED;// }

			}
		} else {// 剔除else
			while (offset < ifBlockStart) {
				data[offset] = DELETED;
				offset++;
			}
			// offset == ifBlockStart
			data[offset++] = DELETED;// {
			data[ifBlockEnd - 1] = DELETED;// }

			if (data[ifBlockEnd] == Token.ELSE) {
				offset = ifBlockEnd;
				int elseBlockEnd = RhinoTool.skipBlock(data, offset);
				while (offset < elseBlockEnd) {
					data[offset] = DELETED;
					offset++;
				}

			}
		}
		return ifBlockStart;
	}

	protected void doDeleteVars(char[] data) {
		LinkedList<Collection<String>> localVarsList = new LinkedList<Collection<String>>();
		localVarsList.add(new HashSet<String>());
		int offset = 0;
		if (data[offset] == Token.SCRIPT) {
			offset = 1;
		}
		boolean inFunctionVar = false;
		while (offset < data.length) {
			StringBuilder name;
			switch (data[offset]) {
			case Token.VAR:
				this.processVars(data, offset, localVarsList.getLast());
				break;
			case Token.REGEXP: // re-wrapped in '/'s in parser...
			case Token.STRING:
			case Token.NAME:
				// 函数名 长度不可能占用两个字节吧？？
				if (inFunctionVar) {
					offset = printSourceString(data, offset + 1, false,
							name = new StringBuilder());
					localVarsList.getLast().add(name.toString());
				} else {
					offset = printSourceString(data, offset + 1, false, null);
				}
				continue;
			case Token.NUMBER:
				offset = printSourceNumber(data, offset + 1, null);
				continue;
			case Token.FUNCTION:
				offset++; // skip function type
				inFunctionVar = true;
				if (getNext(data, offset) == Token.NAME) {
					offset++;
					offset = printSourceString(data, offset + 1, false,
							name = new StringBuilder());
					// 有点拖后了，这个也很复杂。
					localVarsList.getLast().add(name.toString());
					localVarsList.add(new HashSet<String>());
					continue;
				} else {
					localVarsList.add(new HashSet<String>());
					break;
				}
			case Token.RP:
				inFunctionVar = false;
				break;
			case FUNCTION_END:
				localVarsList.removeLast();
				break;
			}
			++offset;
		}
		assert localVarsList.size() == 1;
	}

	/**
	 * 有问题的！！！
	 * 
	 * @param data
	 * @param offset
	 * @param collection
	 * @return
	 */
	private int processVars(char[] data, int offset,
			Collection<String> localVars) {
		int varPos = offset;
		// 略过 var
		offset++;
		ArrayList<String> names = new ArrayList<String>();
		outer: while (offset < data.length) {
			if (data[offset] == Token.NAME) {
				StringBuilder buf;
				final int nameEnd = printSourceString(data, offset + 1, false,
						buf = new StringBuilder());
				String name = buf.toString();
				// 清理重复申明
				if (names.contains(name) || localVars.contains(name)) {
					int next = getNext(data, nameEnd - 1);
					// DebugTool.print(next);
					if (next == Token.SEMI || next == Token.COMMA) {
						// 没有赋值
						do {
							data[offset++] = DELETED;
						} while (offset <= nameEnd);
						// offset = nameEnd +1;
						if (next == Token.SEMI) {
							break outer;
						} else {
							continue outer;
						}
					} else {
						DebugTool.debug(DebugTool.getName((char) next));
					}
				} else {
					names.add(name);
				}
				offset = nameEnd;
			}
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
				case Token.LP:// (
				case Token.LC:// {
				case Token.LB:// [
					offset = RhinoTool.skipBlock(data, offset);
					continue;
				case Token.IN:// )for(var x in y){}
				case Token.RP:// )for(var x in y){}
				case Token.RC:// {
					break outer;
				case Token.COMMA:// ,
					offset++;
					continue outer;
				case Token.SEMI:// ;
					if (getNext(data, offset) == Token.VAR) {
						data[offset++] = Token.COMMA;// ;->,//有问题
						while (data[offset] != Token.VAR) {
							data[offset++] = DELETED;
						}
						data[offset++] = DELETED;// delete var
						// offset++;
						continue outer;
					} else {
						break outer;
					}
				}
				offset++;
			}
		}
		if (localVars.containsAll(names)) {
			data[varPos] = DELETED;
		}
		int reserveIndex = offset;
		while (data[--reserveIndex] == DELETED)
			;
		if (data[reserveIndex] == Token.COMMA) {
			data[reserveIndex] = Token.SEMI;
		}
		localVars.addAll(names);
		return offset;
	}



	public void setCompressorConfig(JavaScriptCompressorConfig config) {
		this.config = config;
	}

}
