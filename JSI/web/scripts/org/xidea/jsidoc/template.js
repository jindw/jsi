/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: jsidoc.js,v 1.9 2008/02/28 14:39:09 jindw Exp $
 */

/**
 * @internal
 */
//var Template=function (path){
//    if(path instanceof Function){
//        this.data = path;
//    }else{
//        var impl = $import('org.xidea.lite:Template',{} );
////        var ParseContext = $import('org.xidea.lite.parse:ParseContext',{} );
////        var parser = new ParseContext(null,path);
////        parser.nodeParsers.push(function(node,context,chain){
////		    if(node.localName == 'a'){
////			    if(!node.getAttribute("onclick")){
////			    	node = node.cloneNode(true);
////			    	node.setAttribute("onclick","return parent.JSIDoc.jump(this)");
////			    }
////		    }
////		    chain.process(node);
////        });
//        return new impl(path);
//    }
//}
//Template.prototype.render = function(context){
//    return this.data(context)
//}
//$import("org.xidea.jsidoc.util.$log")
//$log.error = function(msg){
//	prompt("@@@",msg);
//	return true;
//}
var scriptBase = this.scriptBase
var templateMap = {
     "package": new Template(scriptBase+"html/package.xhtml"),
     "constructor": new Template(scriptBase+"html/constructor.xhtml"),
     "export":  new Template(scriptBase+"html/export.xhtml"),
     "function":  new Template(scriptBase+"html/function.xhtml"),
     "menu":  new Template(scriptBase+"html/menu.xhtml"),
     "native":  new Template(scriptBase+"html/native.xhtml"),
     "object":  new Template(scriptBase+"html/object.xhtml"),
     "source":  new Template(scriptBase+"html/source.xhtml")
}

