package cn.jside.jsi.tools.rhino.block;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jside.jsi.tools.JavaScriptCompressionAdvisor;
import org.mozilla.javascript.ScriptOrFnNode;

public class ScriptScopReplacer  extends FunctionScopeReplacer{
	private Collection<String> features = new HashSet<String>();
	public ScriptScopReplacer(ScriptOrFnNode scriptNode, JavaScriptCompressionAdvisor globalReplacer) {
		this(scriptNode,globalReplacer,null);
	}
	public ScriptScopReplacer(ScriptOrFnNode scriptNode,JavaScriptCompressionAdvisor globalReplacer, Map<String,Integer> unsafeFunctionMap) {
		super(null);
		//RhinoTool.print(scriptNode);
		this.scriptNode = scriptNode;
		this.preInitialize(scriptNode);
		if(unsafeFunctionMap!=null){
			this.unsafeFunctionMap.putAll(unsafeFunctionMap);
		}
		this.postInitialize(globalReplacer);
	}
	public Collection<String> getLocalVars(){
		return this.localVarMap.keySet();
	}
	public Collection<String> getExternalVars(){
		return this.extenalVarMap.keySet();
	}
	public Collection<String> getTopExternalVars(){
		return this.topExtenalVars;
	}
	public Collection<String> getReservedVars(){
		return this.reservedVars;
	}

	/**
	 * 添加一个可选编译特征
	 * @param feature
	 */
	protected void addFeature(String feature) {
		features.add(feature);
	}
	public Collection<String> getFeatures() {
		return features;
	}
	
	@Override
	public String getName() {
		return "";
	}
	/**
	 * @see 
	 * @param replacer
	 */
	protected void postInitialize(JavaScriptCompressionAdvisor replacer) {
		if (this.isUnsafe()) {
			fillNullVariable(extenalVarMap);
			fillNullVariable(localVarMap);
		} else {
			fillDependenceVariable(extenalVarMap,replacer);
			for (Iterator<Map.Entry<String, String>> it = localVarMap.entrySet()
					.iterator(); it.hasNext();) {
				Map.Entry<String, String> entry = it.next();
				if (entry.getValue() == null) {
					String name = entry.getKey();
					// TODO:保留变量不许更改.
					if (this.reservedVars.contains(name)) {
						entry.setValue(name);
					} else {
						// 更改结果不许与外部变量、保留变量同名
						//DebugTool.println(replacer);
						String value = replacer.getReplacedName(name,false);
						if(value == null){
							while (true) {
								value = replacer.newVaribaleName();
								//DebugTool.println(":"+value);
								if (this.reservedVars
										.contains(value)) {
									continue;
								}
								if (this.extenalVarMap
										.containsValue(value)) {
									continue;
								}
								break;
							}
						}
						entry.setValue(value);
					}
				}
			}
		}
		for (Iterator it = this.subReplacerList.iterator(); it.hasNext();) {
			AbstractReplacer subReplacer = (AbstractReplacer) it.next();
			subReplacer.postInitialize();
		}
	}
	private void fillDependenceVariable(Map<String, String> extenalVars,
			JavaScriptCompressionAdvisor replacer) {
		for (Iterator<Map.Entry<String, String>> it = extenalVars
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = it.next();
			if (entry.getValue() == null) {
				String replaced = replacer.getReplacedName(entry.getKey(), true);
				entry.setValue(replaced != null?replaced:entry.getKey());
			}
		}
	}

	/**
	 * 关注引用出现次数
	 */
	protected Set<String> topExtenalVars = new HashSet<String>();

	public void hitTopDependence(String name, boolean inFunction) {
		if(!inFunction){
			topExtenalVars.add(name);
		}
	}
}

