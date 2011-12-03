/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */
$import("org.xidea.jsidoc.util:loadTextByURL")
$import("org.xidea.jsidoc.util:findGlobals")
var scriptBase = this.scriptBase;
function testFindGlobals(){
//	alert([
//	findGlobals("var x='123'"),
//	findGlobals("var x=/1234/"),
//	findGlobals("var x ={\"e\":1}"),
//	findGlobals("x//....")
//	])
	var result = findGlobals(loadTextByURL(scriptBase+"test1.js.txt"));
	assertEquals(result,["$JSI","$import"]);
	
	
	assertEquals(findGlobals("var $JSI,$import"),["$JSI","$import"])
	assertEquals(findGlobals("var $JSI;var $import,xx"),["$JSI","$import",'xx'])
	
}
//
/**
 * java 接口
 * @param <String>source 脚本源码
 * @return java.util.Collection 返回全局id集合
 */
function testFindGlobalsAsList(){
    
}