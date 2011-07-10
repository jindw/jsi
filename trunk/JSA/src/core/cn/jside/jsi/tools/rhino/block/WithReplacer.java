package cn.jside.jsi.tools.rhino.block;

import org.mozilla.javascript.Node;

/**
 * With没有任何申明,置换环境与父类相同,算是伪区域，只是构造置换表时的需要。 迭代器需要避开with节点
 * 
 * @author jinjinyun
 * 
 */
class WithReplacer extends AbstractReplacer implements NotReplacer {

	private Node scriptNode;

	protected WithReplacer(Node withNode, AbstractReplacer parentReplacer) {
		super(parentReplacer);
		this.initialize(withNode);
	}

	protected void initialize(Node withNode) {
		// 本地变量表.putAll(父置换表.本地变量表);
		// Catch 块使用父区域标记生成器;
		this.scriptNode = withNode;
		this.labelIDGenerator = this.parentReplacer.labelIDGenerator;
		this.labelMap = this.parentReplacer.labelMap;
		this.varableIDGenerator = this.parentReplacer.varableIDGenerator;
		this.referenceCountMap = this.parentReplacer.referenceCountMap;
		
		//只要没有本地变量，保留变量就会上升到上一级别，所以这两个必须同生同灭
		//this.本地变量表 = this.父置换表.本地变量表;
		//this.保留变量集合 = this.父置换表.保留变量集合;
		
		
		// for(String name : this.父置换表.本地变量表){

		// }
		// this.添加保留变量(name);
		// DebugTool.println("this.本地变量表" + this.本地变量表);
		// this.begin = catch节点.getExistingIntProp(propType)
		this.collectSubNodes(withNode);// 避开一个with操作
		// DebugTool.println("收集数据 完毕");

	}


}
