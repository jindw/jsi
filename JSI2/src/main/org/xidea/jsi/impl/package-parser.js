/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */

this.addScript = function(){
    $this.addScript(arguments[0],toJavaObject(arguments[1]),toJavaObject(arguments[2]), toJavaObject(arguments[3]));
};
this.addDependence = function(){
    $this.addDependence(arguments[0],arguments[1],!!arguments[2])
};
this.setImplementation = function(){
    $this.setImplementation(arguments[0])
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



