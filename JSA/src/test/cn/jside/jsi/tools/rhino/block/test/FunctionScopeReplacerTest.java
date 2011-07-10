package cn.jside.jsi.tools.rhino.block.test;


import org.jside.jsi.tools.JSAToolkit;
import org.jside.jsi.tools.JavaScriptCompressor;
import org.jside.jsi.tools.util.ScriptNode;
import org.junit.Assert;
import org.junit.Test;

public class FunctionScopeReplacerTest {

	//原先IDE的需求，放弃
	@Test
	public void testGetName() {
		JavaScriptCompressor compressor = JSAToolkit.getInstance().createJavaScriptCompressor();
		ScriptNode node = (ScriptNode) compressor.analyse("var xx1 = function(){};xx1.yy2 = function(){}");
		
		System.out.println(compressor.analyse("var xx1 = function(){}").getLocalVars());
		System.out.println(node.getChildren().size());
		System.out.println(node.getChildren().get(0).getName());
		System.out.println(node.getChildren().get(1).getName());
		System.out.println(node.getChildren().get(2).getName());
		Assert.assertEquals("变量申明为匿名函数", node.getChildren().get(0).getName(), "xx1");
		Assert.assertEquals("变量直接赋值为匿名函数", node.getChildren().get(2).getName(), "xx1.yy2");
	}

}
