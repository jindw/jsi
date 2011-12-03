/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */

this.addScript = function(scriptPath, objectNames, beforeLoadDependences, afterLoadDependences){
    $this.addScript(scriptPath,toJavaObject(objectNames),toJavaObject(beforeLoadDependences), toJavaObject(afterLoadDependences));
};
this.addDependence = function(thisPath,targetPath,afterLoad){
    $this.addDependence(thisPath,toJavaObject(targetPath),!!afterLoad)
};
this.setImplementation = function(implementation){
    $this.setImplementation(implementation)
};

function toJavaObject(object) {
    if (object instanceof Array) {
        var result = new java.util.ArrayList();
        for (var i = 0; i < object.length; i++) {
            result.add(object[i]);
        }
        return result;
    } else if(object == null){
        return null;
    }else{
        return object;
    }
}



