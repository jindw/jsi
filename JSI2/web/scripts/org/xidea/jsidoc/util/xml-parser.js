/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: template.js,v 1.4 2008/02/28 14:39:06 jindw Exp $
 */
//parse

//add as default
function XMLParser(){
    var list = this.parserList.concat([]);
    this.parserList = list;
    this.result = [];
}
XMLParser.prototype = new TextParser()
XMLParser.prototype.parse = function(data,base){
    if(base!=null){
        data = load(data,base);
    }
    this.parseNode(data);
    return this.reuslt;
}
/**
 * 装载模板
 * @public
 */
function load(data,base){
    var url = base?base.replace(/[^\/]+(?:#.*)?$/,'') + data : data;
    var pos = url.indexOf('#');
    var xhr = new XMLHttpRequest();
    
    xhr.open("get",url.substr(0,pos),false)
    xhr.send('');
    if(/\/xml/.test(xhr.getResponseHeader("Content-Type"))){//text/xml,application/xml...
        var doc = xhr.responseXML;
    }else{
        var text = xhr.responseText;
        if(window.DOMParser){
            var doc = new DOMParser().parseFromString(text,"text/xml");
        }else{
            //["Msxml2.DOMDocument.6.0", "Msxml2.DOMDocument.3.0", "MSXML2.DOMDocument", "MSXML.DOMDocument", "Microsoft.XMLDOM"];
            var doc = new ActiveXObject("Microsoft.XMLDOM");
            doc.loadXML(text);
        }
    }
    
    if(pos>0){
        doc = selectNodes(doc,url.substr(pos+1));
    }
    return doc;
}

/**
 * 解析函数集
 * @private
 */
XMLParser.prototype.addParser(function(node,context){
    switch(node.nodeType){
        case 1: //NODE_ELEMENT 
            return parseElement(node,context)
        case 2: //NODE_ATTRIBUTE                             
            return parseAttribute(node,context)
        case 3: //NODE_TEXT                                        
            return parseTextNode(node,context)
        case 4: //NODE_CDATA_SECTION                     
            return parseCDATA(node,context)
        case 5: //NODE_ENTITY_REFERENCE                
            return parseEntityReference(node,context)
        case 6: //NODE_ENTITY            
            return parseEntity(node,context)
        case 7: //NODE_PROCESSING_INSTRUCTION    
            return parseProcessingInstruction(node,context)
        case 8: //NODE_COMMENT                                 
            return parseComment(node,context)
        case 9: //NODE_DOCUMENT                                
        case 11://NODE_DOCUMENT_FRAGMENT             
            return parseDocument(node,context)
        case 10://NODE_DOCUMENT_TYPE                     
            return parseDocumentType(node,context)
        //case 11://NODE_DOCUMENT_FRAGMENT             
        //    return parseDocumentFragment(node,context)
        case 12://NODE_NOTATION 
            return parseNotation(node,context)
        default://文本节点
            //this.println("<!-- ERROR： UNKNOW nodeType:"+node.nodeType+"-->")
    }
});


var htmlLeaf = /^(?:meta|link|img|br|hr)$/i;
var scriptTag = /^script$/i

XMLParser.prototype.addParser(function(node,context){
    if(node.nodeType ==1){
        var next = node.attributes;
        context.append('<'+node.tagName);
        for (var i=0; i<next.length; i++) {
            context.parseNode(next.item(i),context)
        }
        if(htmlLeaf.test(node.tagName)){
            context.append('/>')
            return true;
        }
        context.append('>')
        next = node.firstChild
        if(next){
            do{
                context.parseNode(next,context)
            }while(next = next.nextSibling)
        }
        context.append('</'+node.tagName+'>')
        return true;
    }
});

//:core
XMLParser.prototype.addParser(function(node,context){//for
    if(node.nodeType ==1){
        var tagName = node.tagName.toLowerCase();
        if(/^c\:/.test(tagName)){
            switch(tagName.substr(2)){
            case 'if':
                parseIfTag(node,context);
                break;
            case 'elseif':
            case 'else-if':
                parseElseIfTag(node,context);
                break;
            case 'else':
                parseElseTag(node,context);
                break;
            case 'for':
                parseForTag(node,context);
                break;
            case 'set':
            case 'var':
                parseVarTag(node,context);
                break;
            case 'out':
                parseOutTag(node,context);
                break;
                
                
            //for other
            case 'include':
                processIncludeTag(node,context);
                break;
            
            }
            return true;
        }
    }
});
/**
 * 
 */
function processIncludeTag(node,context){
    var attributes = loadAttribute(node.attributes,{url:0,xpath:0});
    var doc = node.ownerDocument;
    var base = doc.documentURI || doc.url;
    if(attributes.url!=null){
        var doc = load(attributes.url,base)
    }
    if(attributes.xpath!=null){
        doc = selectNodes(doc,attributes.xpath);
    }
    context.parseNode(doc,context)
}
function parseIfTag(node,context){
    var next = node.firstChild;
    var attributes = loadAttribute(node.attributes,{test:3});
    context.append([2,attributes.test]);
    if(next){
        do{
            context.parseNode(next,context)
        }while(next = next.nextSibling)
    }
    context.append([]);
}

function parseElseIfTag(node,context){
    context.removeLastEnd();
    var next = node.firstChild;
    var attributes = loadAttribute(node.attributes,{test:3});
    context.append([3,attributes.test]);
    if(next){
        do{
            context.parseNode(next,context)
        }while(next = next.nextSibling)
    }
    context.append([]);
}


function parseElseTag(node,context){
    context.removeLastEnd();
    var next = node.firstChild;
    var attributes = loadAttribute(node.attributes,{});
    context.append([4]);
    if(next){
        do{
            context.parseNode(next,context)
        }while(next = next.nextSibling)
    }
    context.append([]);
}


function parseForTag(node,context){
    var next = node.firstChild;
    var attributes = loadAttribute(node.attributes,{items:3,'var':1,begin:0,end:0,status:0});
    context.append([5,attributes['var'],attributes.items,status]);
    if(next){
        do{
            context.parseNode(next,context)
        }while(next = next.nextSibling)
    }
    context.append([]);
}
function parseVarTag(node,context){
    var attributes = loadAttribute(node.attributes,{name:1,value:1});
    var valueEl = toEL(attributes.value)
    context.append([6,attributes.name,valueEl]);

}

function parseOutTag(node,context){
    var attributes = loadAttribute(node.attributes,{value:3});
    context.append([0,attributes.value,true]);
}















//parser element
function parseElement(node,context){
    var next = node.attributes;
    context.append('<'+node.tagName);
    for (var i=0; i<next.length; i++) {
        context.parseNode(next.item(i))
    }
    var next = node.firstChild;
    if(next){
        context.append('>')
        var postfix = '</'+node.tagName+'>';
        do{
            context.parseNode(next)
        }while(next = next.nextSibling)
        context.append(postfix)
    }else{
        context.append('/>')
    }
    return true;
}
//parser attribute
function parseAttribute(node,context){
    var name = node.name;
    var value = node.value;
    var buf = parseText(value);
    var isStatic;
    var isDynamic;
    //hack parseText is void 
    var i =  buf.length;
    while(i--){
        //hack reuse value param
        var value = buf[i];
        if(value.constructor == String){
            if(value){
                buf[i] = value = value.replace(/[<>&']/g,xmlReplacer);
                isStatic = true;
            }else{
                buf.splice(i,1);
            }
        }else{
            isDynamic = true;
        }
    }
    if(isDynamic && !isStatic){
        //remove attribute;
        //context.append(" "+name+'=""');
        if(buf.length > 1){
            //TODO:....
            throw Error();
        }else{//只考虑单一EL表达式的情况
            buf = buf[0];
            if(buf[0] != 0){
                throw Error("属性内只能有单一EL表达式！！");
            }
            buf = buf[1];
        }
        context.append( [1,name,buf]);
        return true;
    }
    context.append(" "+name+'="');
    if(/^xmlns$/i.test(name)){
        if(buf[0] == 'http://www.xidea.org/taglib/xhtml'){
            buf[0] = 'http://www.w3.org/1999/xhtml'
        }
    }
    context.append.apply(context,buf)
    context.append('"')
    return true;
}
function parseTextNode(node,context){
    var data = node.data;
    context.append.apply(context,parseText(data.replace(/^\s+|\s+$/g,'')))
    return true;
}

function parseCDATA(node,context){
    context.append("<![CDATA[");
    context.append.apply(context,parseText(node.data,true));
    context.append("]]>");
    return true;
}
function parseEntityReference(){
    return true;//not support
}
function parseEntity(){
    return true;//not support
}
function parseProcessingInstruction(node,context){
    context.append("<?"+node.nodeName+" "+node.data+"?>");
    return true;
}
function parseComment(){
    return true;//not support
}
function parseDocument(node,context){
    for(var n = node.firstChild;n!=null;n = n.nextSibling){
        context.parseNode(n);
    }
}
/**
 * @protected
 */
function parseDocumentType(node,context){
    if(node.xml){
        context.append(node.xml);
    }else{
        if(node.publicId){
            context.append('<!DOCTYPE ');
            context.append(node.nodeName);
            context.append(' PUBLIC "');
            context.append(node.publicId );
            context.append( '" "');
            context.append(node.systemId);
            context.append('">');
        }else{
            context.append("<!DOCTYPE ");
            context.append(node.nodeName);
            context.append("[");
            context.append(node.internalSubset);
            context.append("]>");
        }
    }
    return true;
}
//    /**
//     * @protected
//     */
//    function parseDocumentFragment(node,context){
//        var nl = node.childNodes;
//        for (var i=0; i<nl.length; i++) {
//            context.parseNode(nl.item(i));
//        }
//        return true;
//    }
/**
 */
function parseNotationfunction(node,context){
    return true;//not support
}

//1 2



/**
 * @internal
 */
var stringRegexp = /["\\\x00-\x1f\x7f-\x9f]/g;
/**
 * 转义替换字符
 * @internal
 */
var charMap = {
    '\b': '\\b',
    '\t': '\\t',
    '\n': '\\n',
    '\f': '\\f',
    '\r': '\\r',
    '"' : '\\"',
    '\\': '\\\\'
};

/**
 * 转义替换函数
 * @internal
 */
function charReplacer(item) {
    var c = charMap[item];
    if (c) {
        return c;
    }
    c = item.charCodeAt().toString(16);
    return '\\u00' + (c.length>1?c:'0'+c);
}


function xmlReplacer(c){
    switch(c){
        case '<':
          return '&lt;';
        case '>':
          return '&gt;';
        case '&':
          return '&amp;';
        case "'":
          return '&#39;';
        case '"':
          return '&#34;';
    }
}
/**
 * 1   必要
 * 2   EL属性
 * 从XML属性集中载入需要的属性集合，同时报告缺失和冗余
 */
function loadAttribute(attributes,setting){
    var i = attributes.length;
    var data = {};
    while(i--){
        var item = attributes.item(i);
        var key = item.name;
        if(key in setting){
            data[key] = item.value;
        }else{
            $log.error("未知属性：", key,item.parentNode);
        }
    }
    for(var key in setting){
        var type = setting[key];
        if(type & 1){
            var value = data[key].replace(/^\s+|\s+$/g,'');
            if(value == null){
                $log.error("缺少必要属性：", key);
            }
        }
        if(type & 2){
            if(value){
                var value2 = value.replace(/^\s*\$\{([\S\s]*)\}\s*$/,'$1')
                if(value2 != value){
                    data[key] = value2;
                }else{
                    $log.error("属性需要为表达式（${...}）：", key,value,type);
                }
            }
        }
    }
    return data;
}

function toEL(value,type){
    var value2 = value.replace(/^\s*\$\{([\S\s]*)\}\s*$/,'$1')
    if(value2 != value){
        return value2;
    }else{
        if(type == Number){//int
            
        }else{//String
            value = '"' + (stringRegexp.test(value) ?
                            value.replace(stringRegexp,charReplacer) :
                            value)
                       + '"';
        }
        return value;
    }
}
/**
 * TODO:貌似需要importNode
 */
function selectNodes(doc,xpath){
    var docFragment = doc.createDocumentFragment();
    if(document.all){
        var nodes = doc.selectNodes(xpath);
        var buf = [];
        for (var i=0; i<nodes.length; i++) {
            buf.push(nodes.item(i))
        }
    }else{
        var xpe = new XPathEvaluator();
        var nsResolver = xpe.createNSResolver(doc.documentElement);
        var result = xpe.evaluate(xpath, doc.documentElement, nsResolver, 5, null);
        var node;
        var buf = [];
        while (node = result.iterateNext()){
            buf.push(node);
        }
    }
    while (node = buf.shift()){
        node.parentNode.removeChild(node);
        docFragment.appendChild(node);
    }
    return docFragment;
}