package org.xidea.jsi.util.scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.FunctionNode;
import org.xidea.jsi.util.IDGenerator;
import org.xidea.jsi.util.JavaScriptConfig;


public abstract class AbstractReplacer implements Replacer{

	private static final Map<String, Integer> defaultUnsafeFunctionMap;

	static {
		Map<String, Integer> unsafeFunctionMap = new HashMap<String, Integer>();
		unsafeFunctionMap.put("eval", JavaScriptConfig.UNSAFE_SCOPE);
		unsafeFunctionMap.put("Function", JavaScriptConfig.UNSAFE_GLOBAL);
		defaultUnsafeFunctionMap = Collections
				.unmodifiableMap(unsafeFunctionMap);
	}

	protected Map<String, Integer> unsafeFunctionMap = null;

	protected IDGenerator varableIDGenerator = new IDGenerator();;

	protected IDGenerator labelIDGenerator = new IDGenerator();;

	/**
	 * 当前范围（函数）的申明（var） 本地申明的变量，替换时向上查找同名， //同名者优先同值（若未压缩则可压缩之），无则取新值。
	 * 不与保留值，外部值同名。
	 */
	protected Map<String, String> localVarMap = new HashMap<String, String>();

	/**
	 * 自下向上报告。 本地未申明的变量，替换时向上查找真名。 遇申明中指报告，申明处未本地变量
	 */
	protected Map<String, String> extenalVarMap = new HashMap<String, String>();

	/**
	 * 用于With块，自下向上报告。 遇申明中指报告，但申明处宁需保留
	 */
	protected Collection<String> reservedVars = new HashSet<String>();

	/**
	 * 注意动态脚本
	 */
	protected Collection<String> calls = new HashSet<String>();

	/**
	 * 关注引用出现次数
	 */
	protected Map<String, Integer> referenceCountMap = new HashMap<String, Integer>();

	/**
	 * 标记是否可以与变量同名？
	 */
	protected Map<String, String> labelMap = new HashMap<String, String>();

	protected List<AbstractReplacer> subReplacerList = new ArrayList<AbstractReplacer>();

	protected AbstractReplacer parentReplacer = null;

	private int unsafeLevel = 0;

	protected AbstractReplacer(AbstractReplacer parentReplacer) {
		this.parentReplacer = parentReplacer;
		if (this.parentReplacer == null) {
			this.unsafeFunctionMap = defaultUnsafeFunctionMap;
		} else {
			this.unsafeFunctionMap = this.parentReplacer.unsafeFunctionMap;
		}
	}

	protected final void collectSubNodes(Node parent) {
		// DebugTool.printNode(parent);
		Node node = parent.getFirstChild();
		while (node != null) {
			AbstractReplacer subReplacer;
			// DebugTool.println(Rhino节点字符.getName(node.getType()));
			switch (node.getType()) {
			case Token.BINDNAME:// ?
				// Token.SETNAME+ Token.BINDNAME+ Token.NAME
				// X = Y;
			case Token.TYPEOFNAME:
				// typeof x
			case Token.NAME:
				String name = node.getString();
				this.addVarReference(name);
				break;
			case Token.IFNE: {
				Node feature = node.getFirstChild();
				if (feature.getType() == Token.NOT) {
					if(feature.getNext() == null){
						feature = feature.getFirstChild();
					}else{
						break;
					}
				}
//				if(feature == null){
//					DebugTool.printNode(node);
//				}else{
					if (feature.getType() == Token.STRING
							&& feature.getNext() == null) {
						this.addFeature(feature.getString());
					}
//				}
				
				break;
			}
			case Token.FUNCTION:
				//这条非常诡异
				// TODO:可能有问题
				subReplacer = new FunctionScopeReplacer(this.nextFunction(),
						this,parent,node);
				this.subReplacerList.add(subReplacer);
				break;
			case Token.CALL:
				if (node.getFirstChild().getType() == Token.NAME) {
					addCall(node.getFirstChild().getString());
				}
				break;
			case Token.WITH:
				// TODO:这里仍需要优化
				// DebugTool.println("%%%%WITH");
				// TODO:ignore Math...?
				subReplacer = new WithReplacer(node, this);
				this.subReplacerList.add(subReplacer);
				// this.包含不安全因素();
				node = node.getNext();
				continue;
			case Token.CATCH_SCOPE:
				// TODO:
				// DebugTool.println("%%%%CATCH_SCOPE");
				// this.本地变量表.put(node.getFirstChild().getString(),null);
				this.addVarReference(node.getFirstChild().getString());
				/*
				 * {type:LOCAL_BLOCK,class:org.mozilla.javascript.Node}
				 * ＋＋{type:CATCH_SCOPE,class:org.mozilla.javascript.Node}
				 * {type:NAME,value:e,class:org.mozilla.javascript.Node$StringNode}
				 * {type:LOCAL_LOAD,class:org.mozilla.javascript.Node}
				 * {type:BLOCK,class:org.mozilla.javascript.Node}
				 * {type:ENTERWITH,class:org.mozilla.javascript.Node}
				 * {type:LOCAL_LOAD,class:org.mozilla.javascript.Node}
				 * {type:WITH,class:org.mozilla.javascript.Node}
				 * {type:BLOCK,class:org.mozilla.javascript.Node}
				 */
				node = node.getNext()/* BLOCK */;
				Node catchContent = node.getFirstChild()/* ENTERWITH */
				.getNext();/* WITH */
				this.collectSubNodes(catchContent);
				return;// no next.
				// node = node.getNext()/*FILLALY*/;
				// continue;//while to finally 错！
				// 子置换表 = new 捕捉块名称置换表(node, this);
				// this.子置换表列表.add(子置换表);
				// DebugTool.println("%%%%DEBUG");
				// 跳过CATCH, Catch Block
				// return;
			}

			this.collectSubNodes(node);
			node = node.getNext();
		}
	}


	protected FunctionNode nextFunction() {
		return parentReplacer.nextFunction();
	}

	private void markUnsafe(int level) {
		this.unsafeLevel = Math.max(level, this.unsafeLevel);
		if (this.parentReplacer != null) {
			this.parentReplacer.markUnsafe(level);
		}
	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.util.scope.Replacer#isUnsafe()
	 */
	public boolean isUnsafe() {
		return this.unsafeLevel > 0;
	}

	protected boolean isScopeUnsafe() {
		return this.unsafeLevel > 1;
	}

	public int getUnsafeLevel() {
		return this.unsafeLevel;
	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.util.scope.Replacer#getReferenceCountMap()
	 */
	public Map<String, Integer> getReferenceCountMap() {
		return referenceCountMap;
	}

	/**
	 * 添加一个可选编译特征
	 * @param feature
	 */
	protected void addFeature(String feature) {
		if (this.parentReplacer != null) {
			// 外部变量命中
			this.parentReplacer.addFeature(feature);
		}
	}
	/**
	 * 任何变量引用都要触发该函数一次
	 * 
	 * @param name
	 */
	private final void addVarReference(String name) {
		AbstractReplacer replacer = this;
		boolean containsWith = false;// 0 未申明，11（3） with外申明，01
		boolean inFunction = false;								// （1）有效申明(无with或with内)
		while (true) {
			if (replacer instanceof WithReplacer) {
				containsWith = true;
			}
			if (replacer instanceof FunctionScopeReplacer  && !(replacer instanceof ScriptReplacer )) {
				inFunction = true;
			}
			if (replacer.localVarMap.containsKey(name)) {
				hitReference(name);//多出来的，因为有return 
				if (containsWith) {
					this.addReservedReference(name);
				} else {
					// return 正常申明;
					// this.本地变量表.add(name,null);
				}
				// 遇申明终止上访
				return;
			} else {
				// 如果本地无申明，那么对于本地来说，这就是外部变量
				replacer.extenalVarMap.put(name, null);
			}
			if (replacer.parentReplacer == null) {
				// 外部变量命中
				hitReference(name);
				//顶部直接命中外部依赖
				((ScriptReplacer)replacer).hitTopDependence(name,inFunction);
				return;
			} else {
				replacer = replacer.parentReplacer;
			}
		}
	}

	/**
	 * @friend #addVarReference 其他的方不许调用
	 * @param name
	 */
	private void hitReference(String name) {
		Integer inc = referenceCountMap.get(name);
		if (inc == null) {
			referenceCountMap.put(name, 1);
		} else {
			referenceCountMap.put(name, inc + 1);
		}
	}

	/**
	 * @friend #addVarReference 其他的方不许调用
	 * @param name
	 */
	private void addReservedReference(String name) {
		reservedVars.add(name);
		if (this.parentReplacer != null &&
		// 如果本地有申明，那么父表就无需保留
				!this.localVarMap.containsKey(name)) {
			this.parentReplacer.addReservedReference(name);
		}
	}

	private void addCall(String name) {
		calls.add(name);
		if (this.unsafeFunctionMap.containsKey(name)) {
			this.markUnsafe(this.unsafeFunctionMap.get(name));
		}
		if (this.parentReplacer != null) {
			this.parentReplacer.addCall(name);
		}
	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.util.scope.Replacer#findReplacedVar(java.lang.String)
	 */
	public String findReplacedVar(String name) {
		AbstractReplacer replacer = this;
		while (true) {
			if (replacer.localVarMap.containsKey(name)) {
				return replacer.localVarMap.get(name);
			}
			if(replacer.parentReplacer == null){
				String external = replacer.extenalVarMap.get(name);
				return external == null?name:external;
			}else{
				replacer = replacer.parentReplacer;
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.xidea.jsi.util.scope.Replacer#findReplacedLabel(java.lang.String)
	 */
	public String findReplacedLabel(String name) {
		if (!labelMap.containsKey(name)) {
			labelMap.put(name, this.newLabelName(name));
		}
		return labelMap.get(name);
	}

	private boolean containsExtenalVar(String name) {
		return this.extenalVarMap.containsValue(name);
	}

	private String newVariableName() {
		while (true) {
			String var = this.varableIDGenerator.newId();
			if (!this.containsExtenalVar(var)) {
				// TODO:
				return var;
			}
		}
	}

	private String newLabelName(String name) {
		return this.labelIDGenerator.newId();
	}

	protected void fillNullVariable(Map<String, String> variableNameMap) {
		for (Iterator<Map.Entry<String, String>> it = variableNameMap
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = it.next();
			if (entry.getValue() == null) {
				entry.setValue(entry.getKey());
			}
		}
	}

	protected final void postInitialize() {
		if (this instanceof ScriptReplacer) {
			throw new RuntimeException(
					"ScriptScopReplacer can not call postInitialize ,call postInitialize(GlobalReplacer)");
		}
		if (this.isScopeUnsafe()) {
			fillNullVariable(localVarMap);
			fillNullVariable(extenalVarMap);
		} else {
			for (Iterator<Map.Entry<String, String>> it = extenalVarMap
					.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, String> entry = it.next();
				if (entry.getValue() == null) {
					String name = entry.getKey();
					if (parentReplacer != null) {
						if (parentReplacer.localVarMap.containsKey(name)) {// 我时保留的，你也是保留的
							name = parentReplacer.localVarMap.get(name);
						} else {// 不是你本地的，就是你外地的
							name = parentReplacer.extenalVarMap.get(name);
						}
					}
					entry.setValue(name);
				}
			}
			for (Iterator<Map.Entry<String, String>> it = localVarMap.entrySet()
					.iterator(); it.hasNext();) {
				Map.Entry<String, String> entry = it.next();
				if (entry.getValue() == null) {
					// TODO:保留变量不许更改.
					if (this.reservedVars.contains(entry.getKey())) {
						entry.setValue(entry.getKey());
					} else {
						// 更改结果不许与外部变量、保留变量同名
						while (true) {
							String value = this.newVariableName();
							if (this.reservedVars.contains(value)) {
								continue;
							}
							if (this.extenalVarMap.containsValue(value)) {
								continue;
							}
							entry.setValue(value);
							break;
						}
					}
				}
			}
		}
		for (Iterator<AbstractReplacer> it = this.subReplacerList.iterator(); it.hasNext();) {
			AbstractReplacer replacer = (AbstractReplacer) it.next();
			// 子置换表.全部变量表.putAll(this.全部变量表);
			replacer.postInitialize();
		}
	}

	private void buildReplacerList(List<AbstractReplacer> list,
			AbstractReplacer replacer) {
		if (!(replacer instanceof NotReplacer)) {
			list.add(replacer);
		}
		Iterator<AbstractReplacer> subIterator = replacer.subReplacerList
				.iterator();
		while (subIterator.hasNext()) {
			buildReplacerList(list, subIterator.next());
		}
	}

	public List<AbstractReplacer> list() {
		List<AbstractReplacer> list = new ArrayList<AbstractReplacer>();
		buildReplacerList(list, this);
		// DebugTool.println(">>>>>"+列表);
		return list;
	}

	public String toString() {
		return "本地变量表:" + this.localVarMap;
	}
}
