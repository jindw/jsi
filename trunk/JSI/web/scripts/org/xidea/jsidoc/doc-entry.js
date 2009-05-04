/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: doc-entry.js,v 1.8 2008/02/28 14:39:09 jindw Exp $
 */



/**
 * 用于解析及记录一个文档块的数据
 * @public
 * @param <SourceEntry> sourceEntry 文档块对应的源文件数据
 * @param <int> begin 相对文档的开始位置
 * @param <int> end 相对文档的结束位置[不包括]
 */
function DocEntry(sourceEntry,begin,end){
    this.sourceEntry = sourceEntry;
    this.source = sourceEntry.source;
    this.begin = begin;
    this.end = end;
    this.tagAttributes = {'constructor':null};
    this.sourceAttributes = {'constructor':null};
    parseSource(this);
    parseDocument(this);
}



var FUN_1 = /^\s*function\s+[\w\$_]+\s*\([\w\$,\s]*\)/;//function fn(){}
var FUN_2 = /^\s*function\s*\([\w\$,\s]*\)/; //function(){}
var OBJ_1 = /^\s*var\s+[\w\$]+\s*=/;//xxx = ""
var OBJ_2 = /^\s*[\w\$\.]+\s*=/; //xxx.yy = ""
var OBJ_3 = /^\s*[\w\$]+\s*:/;//key:"value"

var EXT_1_ = /^[\s\S]*?([\w\.\$_]+)\s*[=,]\s*$/
var FUN_1_ = /^[\s\S]*?function\s+([\w\$_]+)\s*\([\w\$_,\s]*\)\s*$/;//function fn(){}

var list = [accessTag,flagTag,valueTag,valuesTag];
var tagAlias = {'constructor':null};
for(var i = 0;i<list.length;i++){
    for(var n in list[i]){
        var info = list[i][n];
        if(info.alias){
            for(var j = 0;j<info.alias.length;j++){
                tagAlias[info.alias[j]] = n;
            }
        }
    }
}
function findPropertyOwner(entry){
    var pos = entry.begin;
    var depths = entry.sourceEntry.depths;
    var pos2 = findBeginPos(depths,pos);
    if(pos2){
        var source = entry.source.substr(0,pos2);
        var id = source.replace(EXT_1_,'$1');
        if(id != source){
            return id;
        }
    }
}
function findThisOwner(entry){
    var pos = entry.begin;
    var depths = entry.sourceEntry.depths;
    var pos2 = findBeginPos(depths,pos);
    if(pos2){
        var source = entry.source.substr(0,pos2);
        var id = source.replace(FUN_1_,'$1');
        //alert([entry.source.substring(entry.begin,entry.end),source.substr(source.length-50,50)])
        if(id != source){
            return id+".this";
        }
        var id = source.replace(EXT_1_,'$1');//no , 
        if(id != source){
            return id.replace(/\.[a-z_\$][\w\$_]*$/,'');
        }
        //pos2 = findBeginPos(depths,pos2);
    }
}
function findBeginPos(depths,pos){
    var i = depths.length;
    while(i-- && depths[i][0]>pos){
    }
    var depthNode = depths[i];
    if(depthNode){
        var depth = Math.max(depthNode[1],depthNode[2])-1;//doc depth
        while(i){
            if(depth == depthNode[2]&& depthNode[1]<depth){
                return depthNode[0]
            }
            depthNode = depths[--i]
        }
    }
}
function parseSource(entry){
    var source = entry.source.substr(entry.end);
    var name,type,instance;
    if(FUN_1.test(source)){
        var p = source.indexOf("function");
        var p1 = source.indexOf("(");
        var p2 = source.indexOf(")");
        name = source.substring(p+"function".length,p1).replace(/\s*/g,'');
        type = "function";
    }else{
        if(OBJ_1.test(source)){//var xxx
            var p = source.indexOf("=");
            name = source.substring(source.indexOf("var")+3,p).replace(/\s*/g,'');
        }else if(OBJ_2.test(source)){
            var p = source.indexOf("=");
            name = source.substring(0,p).replace(/\s*/g,'');
        }else if(OBJ_3.test(source)){
            var p = source.indexOf(":");
            entry.isProperty = true;
            name = source.substring(0,p).replace(/\s*/g,'');
        }
        source = source.substr(p+1);
        if(FUN_2.test(source)){
            type = "function";
        }else if(/^\s*(\[|\{|(new\s+))/.test(source) ){
            type = "object";
        }
    }
    if(name && name.length != (name = name.replace(/^this\./,'')).length){
        entry.isThis = true;
    }
    entry.sourceAttributes.name = name;
    entry.sourceAttributes['typeof'] = type;
};
function parseDocument(entry){
    var text = entry.source.substring(entry.begin,entry.end);
    var pp = /^\s*\*\s*(?:@([\w\d\-_]+))?\s*((?:(?:{@)|[^@])*)$/gm;
    var m = null;
    while(m = pp.exec(text)){
        var part = m[0];
        var tag = m[1];
        var content = m[2];
        if(content){
            content = content.replace(/(?:\s*\*\/\s*$)|(?:^\s*\*\s?)/gm,'');
        }
        if(tag){
            switch(tag){
                case 'public':
                case 'private':
                case 'protected':
                case 'internal':
                case 'friend':
                case 'intenal':
                    processTag(entry,"access",tag);
                    break;
                default:
                    processTag(entry,tag,content);
            }
        }else{
            entry.description = content;
        }
    }
};

function processTag(entry,tag,value){
    if(tagAlias[tag]){
        tag = tagAlias[tag];
    }
    var values = entry.tagAttributes[tag];
    if(values == null){
        values = entry.tagAttributes[tag] = [];
    }
    values.push(value);
}

if(":debug"){
    DocEntry.prototype.toString = function(){
        var buf = "sourceId="+this.sourceAttributes.name+"\n";
        buf += "description="+this.description+"\n";
        for(var key in this.tagAttributes){
            buf+= "@"+key+":"+this.tagAttributes[key]+"\n";
        }
        return buf+"\n\n\n";
    }
}


DocEntry.prototype.getAttribute = function(key){
    var values = this.tagAttributes[key];
    if(values){
        return values[values.length-1];
    }else{
        return null;
    }
};

DocEntry.prototype.getExtend = function(){
    var e =this.getAttribute('extend');
    if(e){
        return e.replace(/^\s*([\w\.]+)[\s\S]*$/,'$1');
    }
};
DocEntry.prototype.isConstructor = function(){
    //$log.info(this.tagAttributes['constructor']);
    return this.tagAttributes['constructor'] != null;
};
DocEntry.prototype.getInstanceof  = function(){
    return this.getAttribute('instanceof');
};
DocEntry.prototype.getTypeof = function(){
    return this.getAttribute('typeof');
};




DocEntry.prototype.getAccess = function(){
    return this.getAttribute('access');
};

DocEntry.prototype.getParams = function(){
    return this.tagAttributes["param"];;
}
DocEntry.prototype.getDescription = function(){
    return this.description || '';
}
DocEntry.prototype.getArguments = function(){
    return this.getAttribute('arguments');
}
DocEntry.prototype.getReturn = function(){
    return this.getAttribute('return');
}
DocEntry.prototype.getReturnType = function(){
    return this.getAttribute('returnType');
}
DocEntry.prototype.getStatic = function(){
    return this.tagAttributes['static'] && true;
};
DocEntry.prototype.getName = function(){
    var n = this.getAttribute('name');
    if(n){
        return n;
    }
    return this.sourceAttributes['name'];
};
DocEntry.prototype.isFileoverview = function(){
    return this.getAttribute('fileoverview') && true;
};

DocEntry.prototype.getId = function(){
    if(this.id){
        return this.id;
    }
    if(this.tagAttributes.id){
        return this.id = this.tagAttributes.id[0].replace(/\s+/g,'');
    }
    var name = this.getName();
    if(!name){
        return null;
    }
    var owner = this.getOwner();
    if(owner){
        // || !(this.getConstructor() || /^\s*'?"?function/.test(this.type))
        this.id = owner+"."+name;
        return this.id;
    }
    return this.id = name;;
}
DocEntry.prototype.getOwner = function(){
    if('owner' in this){
        return this.owner;
    }
    var o = this.getAttribute('owner');
    if(o){
        return this.owner = o.replace(/([\w\d\.\$\_]*)[\s\S]*$/,'$1');
    }else{
        if(this.isThis){
            //alert(this.id)
            return this.owner = findThisOwner(this)
        }
        if(this.isProperty){
            return this.owner = findPropertyOwner(this)
        }
        return this.owner = null;
    }
};

DocEntry.prototype.isTop = function(){
    if(this.isConstructor()){
        return true;
    }else{
        var id = this.getId();
        return this.getOwner()==null&&id!=null && id.indexOf('.')<0;
    }
}
DocEntry.prototype.isFiledoc = function(){
    return this.isFileoverview() || this.getId() == null;
}

/**
 * 空DocEntry对象
 * @internal
 */
DocEntry.EMPTY= new (function(){
    for(n in DocEntry.prototype){
        this[n] = DocEntry.prototype[n];
    }
    this.tagAttributes = {'constructor':null};
    this.sourceAttributes = {'constructor':null};
})();
