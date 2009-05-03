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
function getTemplate(path){
    return templateMap[path];
}
function Template(path){
    if(path instanceof Function){
        this.data = path;
    }else{
        var t = $import('org.xidea.lite:Template',{} );
        t = new t(path);
        return t;
    }
}
Template.prototype.render = function(context){
    return this.data(context)
}
var scriptBase = this.scriptBase
var templateMap = {
	"package.xhtml" : new Template(scriptBase+"html/package.xhtml"),
	"constructor.xhtml" : new Template(scriptBase+"html/constructor.xhtml"),
	"export.xhtml" : new Template(scriptBase+"html/export.xhtml"),
	"function.xhtml" : new Template(scriptBase+"html/function.xhtml"),
	"menu.xhtml" : new Template(scriptBase+"html/menu.xhtml"),
	"native.xhtml" : new Template(scriptBase+"html/native.xhtml"),
	"object.xhtml" : new Template(scriptBase+"html/object.xhtml"),
	"source.xhtml" : new Template(scriptBase+"html/source.xhtml")
};
