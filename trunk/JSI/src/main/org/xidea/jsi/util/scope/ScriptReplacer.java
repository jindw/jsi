package org.xidea.jsi.util.scope;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.ast.ScriptNode;
import org.xidea.jsi.util.ReplaceAdvisor;

public class ScriptReplacer  extends FunctionScopeReplacer{
	private Collection<String> features = new HashSet<String>();
	public ScriptReplacer(ScriptNode scriptNode, ReplaceAdvisor globalReplacer) {
		this(scriptNode,globalReplacer,null);
	}
	public ScriptReplacer(ScriptNode scriptNode,ReplaceAdvisor globalReplacer, Map<String,Integer> unsafeFunctionMap) {
		super(null);
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
	
	/**
	 * @see 
	 * @param replacer
	 */
	protected void postInitialize(ReplaceAdvisor replacer) {
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
		for (Iterator<AbstractReplacer> it = this.subReplacerList.iterator(); it.hasNext();) {
			AbstractReplacer subReplacer = it.next();
			subReplacer.postInitialize();
		}
	}
	private void fillDependenceVariable(Map<String, String> extenalVars,
			ReplaceAdvisor replacer) {
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

