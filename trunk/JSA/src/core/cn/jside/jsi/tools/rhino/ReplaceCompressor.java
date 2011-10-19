package cn.jside.jsi.tools.rhino;

import java.util.Iterator;
import java.util.LinkedList;

import org.jside.jsi.tools.JavaScriptCompressionAdvisor;
import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.mozilla.javascript.NodeTransformer;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

import cn.jside.jsi.tools.rhino.block.AbstractReplacer;
import cn.jside.jsi.tools.rhino.block.ScriptScopReplacer;

/**
 * @author jinjinyun bug for var A; function (b){ A = b; }
 */
public class ReplaceCompressor extends RhinoTool {
	private Parser parser;
	private LinkedList<AbstractReplacer> currentReplacerList = new LinkedList<AbstractReplacer>();
	private Iterator<AbstractReplacer> availableReplacerIterator = null;
	private JavaScriptCompressorConfig config;
	ReplaceCompressor(Parser parser) {
		this.parser = parser;
	}

	public synchronized String compress(String source, JavaScriptCompressionAdvisor advisor) {
		ScriptOrFnNode root = parser.parse(source, "", 1);
		new NodeTransformer().transform(root);
		this.initializeReplacer(root,source,advisor);
		char[] data = parser.getEncodedSource().toCharArray();
		String result = this.doReplace(data, 0);
		parser.parse(result, "", 1);
		data = parser.getEncodedSource().toCharArray();
		result = RhinoTool.decode(data, null, null);
		return check(result);
	}

	private void initializeReplacer(ScriptOrFnNode root,String content, JavaScriptCompressionAdvisor advisor) {
		ScriptScopReplacer replacer = new ScriptScopReplacer(root,advisor );
		currentReplacerList.clear();
		availableReplacerIterator = replacer.list().iterator();
		currentReplacerList.add(availableReplacerIterator.next());
	}

	private String check(String source) {
		if (currentReplacerList.size() == 1 && !availableReplacerIterator.hasNext()) {
			return source;

		} else {
			throw new IllegalStateException(
					"结尾状态异常，压缩工具可能有bug，请将压缩的文件提交给 xidea.org，以供测试。\n"
							+ ";depth=" + ";当前置换队列=" + currentReplacerList.size()
							+ ";可用置换序列=" + availableReplacerIterator.hasNext());

		}
	}

	/**
	 * 替换变量
	 */
	protected String doReplace(char[] data, int offset) {
		//DebugTool.printData(data, offset, data.length);
		StringBuilder result = new StringBuilder();
		StringBuilder name;
		final int end = data.length;
		int pre = 0;//
		if (data[offset] == Token.SCRIPT) {//其他的方不能有Script，只能是开始，这时offset必须为0
			pre = Token.SCRIPT;
			offset = 1;
		}
		while (offset < end) {
			int type = data[offset];
			switch (type) {
			case Token.EOF:
			case Token.EOL:
				break;
				
			case Token.NAME:
			{
				offset = printSourceString(data, offset + 1, false, name = new StringBuilder());
				if (pre != Token.DOT && data[offset] != Token.OBJECTLIT) {
					// DebugTool.println(Rhino节点字符.getName(pre));
					// DebugTool.println(Rhino节点字符.getName(data[i]));
					// DebugTool.println("find:" + name + 当前置换队列.getLast());
					if (pre == Token.BREAK || pre == Token.CONTINUE) {
						result.append(currentReplacerList.getLast().findReplacedLabel(name.toString()));
						// } else if (data[offset] == Token.COLON && pre !=
						// Token.CASE && Token.HOOK) {
						// 这里有潜在风险（判断不全）eg: var x=1;x=x?x+x:1;
						// var x=1,y=2;x=x?y?x:y:x
						// result.append(当前置换队列.getLast().置换标记(name));
					} else if (data[offset] == Token.COLON && 
							(pre == Token.EOL || pre == Token.LC || pre == Token.SCRIPT)) {
						// case xxx:
						// b?a:b;
						result.append(currentReplacerList.getLast().findReplacedLabel(name.toString()));
					} else {
						result.append(currentReplacerList.getLast().findReplacedVar(name.toString()));
					}
				} else {
					result.append(name);
				}
				pre = type;

				continue;
			}
			case Token.FUNCTION:
			{
				// DebugTool.println("FUNCTION");
				offset++; // skip function type
				result.append("function");
				if (getNext(data, offset) == Token.NAME) {
					offset++;
					result.append(' ');
					offset = printSourceString(data, offset + 1, false, name = new StringBuilder());
					result.append(currentReplacerList.getLast().findReplacedVar(name.toString()));
					pre = type;
					currentReplacerList.add(availableReplacerIterator.next());
					continue;
				} else {
					//DebugTool.println("可用置换序列.next");
					currentReplacerList.add(availableReplacerIterator.next());
					break;
				}
			}
			case Token.REGEXP: // re-wrapped in '/'s in parser...
				pre = type;
				offset = printSourceString(data, offset + 1, false,
						result);
				continue;
			case Token.LB:
			{
				if (data[offset + 1] == Token.STRING) {
					int next = printSourceString(data, offset + 2, false,
							name = new StringBuilder());
					if (isValidIdentify(name.toString()) 
							&& data[next] == Token.RB
							&& (pre == Token.NAME || pre == Token.RB || pre == Token.RP)) {
						result.append('.');
						offset = next + 1;//]
						result.append(name);
						pre = type;
						continue;

					}
				}
				pre = type;
				result.append('[');
				break;
			}
			case Token.STRING:
			{
				boolean hit = false;

				int next = printSourceString(data, offset + 1, false,
						name = new StringBuilder());
				if (isValidIdentify(name.toString())) {
					if (data[next] == Token.OBJECTLIT) {
						hit = true;
						offset = next;
						result.append(name);
					}

				}
				if (!hit) {
					offset = printSourceString(data, offset + 1, true, result);
				}
				pre = type;
				continue;
			}
			case Token.NUMBER:
				pre = type;
				offset = printSourceNumber(data, offset + 1, result);
				continue;
			case Token.BREAK:
				result.append("break");
				if (getNext(data, offset) == Token.NAME)
					result.append(' ');
				break;
			case Token.CONTINUE:
				result.append("continue");
				if (getNext(data, offset) == Token.NAME)
					result.append(' ');
				break;
			case Token.RETURN:
				result.append("return");
				if (getNext(data, offset) != Token.SEMI) {
					result.append(' ');
				}
				break;
			// keywords
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
			case FUNCTION_END:
				currentReplacerList.removeLast();
				// Do nothing
				break;
			default:
				String value = SIMPLE_TOKEN_VALUES[type];
				if (value == null) {
					// If we don't know how to decompile it, raise an exception.
					throw new RuntimeException(DebugTool.getName(type)
							+ result.toString());
				} else {
					result.append(value);
				}
			}
			pre = type;
			++offset;
		}
		return result.toString();
	}

	public void setCompressorConfig(JavaScriptCompressorConfig config) {
		this.config = config;
	}

}
