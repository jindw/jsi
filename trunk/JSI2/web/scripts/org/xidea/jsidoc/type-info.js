/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: type-info.js,v 1.7 2008/02/25 01:55:59 jindw Exp $
 */


function ParamInfo(params,avaliableObjectInfoMap){
    avaliableObjectInfoMap =  avaliableObjectInfoMap|| {};
    var data = this.data = [];
    for(var i = 0;i<params.length;i++){    
        if(params[i]){
            data.push(new TypeInfo(params[i],avaliableObjectInfoMap));
        }
    }
    this.length = data.length;
}
ParamInfo.prototype = [];
ParamInfo.prototype.toString = function(){
    return "("+this.data.join(" , ")+")";
}
/**
 * @public
 */
function TypeInfo(text,avaliableObjectInfoMap){
    var m = /^\s*([\w\$]+)?\s*(\{|<)/.exec(text);
    if(m){
        i = m[0].length;
        if(m[0][i-1] == '{'){
            var j = searchEnd(text,i,'{','}')
        }else{
            var j = searchEnd(text,i,'<','>')
        }
        if(j){
            var type = text.substring(i,j);
            if(m[1]){
                var name =  m[1];
                var description = text.substr(j+1);
                return;
            }else{
                text = text.substr(j+1);
            }
        }
    }
    if(!name){
        m = /^\s*([\w\$]+)(?:\s+|$)([\s\S]*)?$/.exec(text);
        if(m){
            var name = m[1];// || dn;
            var description = m[2];
        }else{
            var description = text;
        }
    }
    if(name === 'null'){
        name ='';
    }
    
    this.type = type = type || '';
    this.name = name = name || '';
    this.description = description ||'';
    this.html = buildTypeHTML(avaliableObjectInfoMap,type) + name
}
  
TypeInfo.prototype.isValid = function(){
    return this.name || this.type;
}
TypeInfo.prototype.toString = function(){
    return this.html;
}
var nativeTypeAlias = {
    'int':"Number",
    'double':"Number",
    'float':"Number",
    'byte':"Number",
    'char':"String"
}
//var nativeURLPattern = "file:///F:/javascriptref/js55/html/jsobj%1.htm";
//var nativeURLPattern = "file:///F:/javascriptref/mozilla/%1.html";
//var nativeURLPattern = "http://www.w3school.com.cn/js/jsref_obj_%1.asp";
var nativeURLPattern = "http://www.xidea.org/project/jsidoc/js1.5/%1.html";

var nativeTypeURLMap = {}
var nativeTypes = ["Object","Function","RegExp","Array","String","Date","Number","Boolean"];//"Math","Global",
var type = nativeTypes.pop();
do{
    nativeTypeAlias[type.toLowerCase()] = type
    nativeTypeURLMap[type] = nativeURLPattern.replace("%1",type.toLowerCase());
}while(type = nativeTypes.pop())
function buildTypeHTML(avaliableObjectInfoMap,type){
    var types = type.split(/[| ]+/);
    var buf = [];
    var i = types.length;
    while(i--){
        type = types[i];
        var typeInfo = avaliableObjectInfoMap[type];
        if(!typeInfo){
            //test native
            var name = nativeTypeAlias[type] || type;
            var href = nativeTypeURLMap[name];
        }else{
            var href ="?"+typeInfo.getObjectInfo(type).getPath();
        }
        if(href){
            if(typeInfo){
                buf.push("<a href='",href,"'>",type,"</a>");
            }else{
                buf.push("<a target='_native' onclick=\"open('about:blank','_native','scrollbars=1,toolbar=1,menubar=0,resizable=1,channelmode=1,width:600,height:600')\" href='",href,"'>",type,"</a>");
            }
            
        }else if(type){
            buf.push(type);
        }
//        else{
//            buf.push("void");
//        }
        if(i){
            buf.push('|')
        }
    }
    if(buf.length){
        buf.unshift("&lt;");
        buf.push("&gt;");
    }
    return buf.join('');
}
function searchEnd(text,i,b,e){
    var d = 0;
    while(++i<text.length){
        switch(text.charAt(i)){
            case b:
                d++;
                break;
            case e:
                if(d == 0){
                    return i;
                }else if(d<0){
                    return null;
                }
                d--;
        }
    }
    return null;
};
