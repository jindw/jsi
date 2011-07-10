package cn.jside.jsi.tools.rhino;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jside.jsi.tools.JavaScriptAnalysisResult;
import org.jside.jsi.tools.JavaScriptError;
import org.jside.jsi.tools.util.JavaScriptConstants;
import org.jside.jsi.tools.util.ScriptNode;

import cn.jside.jsi.tools.rhino.block.ScriptScopReplacer;

public class AnalysisResultImpl implements JavaScriptAnalysisResult ,ScriptNode {
	private ScriptScopReplacer scriptNode;
	private List<JavaScriptError> errors;
	public AnalysisResultImpl(ScriptScopReplacer scriptNode,Collection<JavaScriptError> errors){
		this.scriptNode = scriptNode;
	}

	public int getReferenceCount(String var) {
		Integer count = scriptNode.getReferenceCountMap().get(var);
		return count == null?0:count;
	}
	
	/* (non-Javadoc)
	 * @see 金大为.工具集.脚本处理.JavaScriptAnalyser#获取本地变量集()
	 */
	public Collection<String> getLocalVars() {
		return this.scriptNode.getLocalVars();
	}

	/* (non-Javadoc)
	 * @see 金大为.工具集.脚本处理.JavaScriptAnalyser#获取外部变量集()
	 */
	public Collection<String> getExternalVars() {
		return this.scriptNode.getExternalVars();
	}

	public Collection<String> getTopExternalVars() {
		return scriptNode.getTopExternalVars();
	}
	/* (non-Javadoc)
	 * @see 金大为.工具集.脚本处理.JavaScriptAnalyser#获取错误信息()
	 */
	public List<JavaScriptError> getErrors() {
		return this.errors;
	}

	/* (non-Javadoc)
	 * @see 金大为.工具集.脚本处理.JavaScriptAnalyser#获取未知变量集()
	 */
	public Collection<String> getUnknowVars() {
		HashSet<String> variables = new HashSet<String>(this.scriptNode.getExternalVars());
		variables.removeAll(JavaScriptConstants.ALL_VARIBALES);
		return variables;
	}

	public Collection<String> getReservedVars() {
		return this.scriptNode.getReservedVars();
	}

	public Collection<String> getFeatures() {
		return this.scriptNode.getFeatures();
	}

	public boolean isUnsafe() {
		return this.scriptNode.isUnsafe();
	}
	public ScriptNode getScriptTree() {
		return this.scriptNode;
	}

	public List<ScriptNode> getChildren() {
		return scriptNode.getChildren();
	}

	public int getEndLine() {
		return scriptNode.getEndLine();
	}

	public String getName() {
		return scriptNode.getName();
	}

	public ScriptNode getParent() {
		return scriptNode.getParent();
	}

	public int getStartLine() {
		return scriptNode.getStartLine();
	}

	public int getType() {
		return scriptNode.getType();
	}

}
