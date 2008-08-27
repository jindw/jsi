/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */

this.addScript = function(){
    if(arguments[1] == "*"){
        arguments[1] = findGlobals($this.getSource(arguments[0])+'');
    }
    //println([arguments[2]? arguments[2]:null,arguments[2]? 123:null,typeof arguments[2],typeof arguments[3],null,arguments[2] || null,arguments[3] || null])
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



//    function(){
//        var a1,a2,a3;
//        (function(){...;a1=1,a2=1,a3=1..})();
//        if(a1)this.push('a1');
//        if(a2)this.push('a2');
//        return this;
//    }

var reserved = ["break", "delete", "function", "return", "typeof", 
    "case", "do", "if", "switch", "var", 
    "catch", "else", "in", "this", "void", 
    "continue", "false", "instanceof", "throw", "while", 
    "debugger", "finally", "new", "true", "with", 
    "default", "for", "null", "try",   
    
    "abstract", "double", "goto", "native", "static", 
    "boolean", "enum", "implements", "package", "super", 
    "byte", "export", "import", "private", "synchronized", 
    "char", "extends", "int", "protected", "throws", 
    "class", "final", "interface", "public", "transient", 
    "const", "float", "long", "short", "volatile"];
var reservedMap = {};
var i = reserved.length;
while(i--){
    try{
        new Function("var "+reserved[i]);
    }catch(e){
        reservedMap[reserved[i]] = true;
    }
}

function findGlobals(source){
    var idMap = {};
    var emptyMap = {};
    var ids = [];
    source.replace(/(\.)?\s*([\w\$]+)/g,function(text,propFlag,id){
        if(reservedMap[id] != true && idMap[id]!=true){
            if(!propFlag && /^[^\d]/.test(id)){
                idMap[id] = true;
                ids.push(id);
            }
        }
    });
    if(ids.length == 0){
        return ids;
    }
    var buf = ["var ",ids.join(','),';'];
    buf.push('(function(){',source,'\n;',ids.join('=1,'),'=1})();\n');
    buf.push(ids.join(' ').replace(/[\S]+/g,"if(!$&)this.push('$&');\n"));
    buf.push("return this;} ");
    return new Function(buf.join('')).call([]);
}