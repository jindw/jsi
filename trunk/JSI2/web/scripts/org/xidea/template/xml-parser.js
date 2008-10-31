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

function isTemplateNS(value,shortAvaliable){
    return shortAvaliable && (value=="#" || value=="#core" || value ==null) || TEMPLATE_NS_REG.test(value);
}
XMLParser.prototype = new TextParser()
XMLParser.prototype.parse = function(url){
    var pos = url.indexOf('#')+1;
    var data = this.load( pos?url.substr(0,pos-1):url,pos && url.substr(pos));
    this.parseNode(data);
    return this.reuslt;
}
XMLParser.prototype.load = function(url,xpath){
	try{
		if(/^[\s\ufeff]*</.test(url)){
	        var doc =toDoc(url)
			//alert([data,doc.documentElement.tagName])
	    }else{
		    var xhr = new XMLHttpRequest();
		    xhr.open("GET",url,false)
		    xhr.send('');
		    if(/\/xml/.test(xhr.getResponseHeader("Content-Type"))){//text/xml,application/xml...
		        var doc = xhr.responseXML;
		    }else{
		        var doc = toDoc(xhr.responseText)
		    }
		    if(xpath){
		        doc = selectNodes(doc,xpath);
		    }
		    this.url = url;
		}
		return doc;
	}catch(e){
		$log.error("文档解析失败",e)
		throw e;
	}
}
/**
 * 解析函数集
 * @private
 */
XMLParser.prototype.addParser(function(node,context){
    switch(node.nodeType){
        //case 1: //NODE_ELEMENT 
        //    return parseElement(node,context)
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
            context.parseNode(next[i],context)
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
        if(isTemplateNS(node.namespaceURI,/^c\:/i.test(tagName))){
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
            case 'foreach':
                parseForTag(node,context);
                break;
            case 'set':
            case 'var':
                parseVarTag(node,context);
                break;
            case 'out':
                parseOutTag(node,context);
                break;
            case 'choose':
                parseChooseTag(node,context);
                break;
            case 'when':
            case 'otherwise':
                break;
            
            
            //for other
            case 'include':
                processIncludeTag(node,context);
                break;
            default:
                $log.error("未知标签：",tagName,node.ownerDocument.documentURI)
            }
            return true;
        }
    }
});
/**
 * 
 */
function processIncludeTag(node,context){
    var var_ = getAttribute(node,'var');
    var path = getAttribute(node,'path');
    var xpath = getAttribute(node,'xpath');
    var name = getAttribute(node,'name');
    var doc = node.ownerDocument || node;
    var parentURL = context.url;
	try{
		if(name){
			var docFragment = doc.createDocumentFragment();
			var next = node.firstChild;
            if(next){
                do{
                    docFragment.appendChild(next)
                }while(next = next.nextSibling)
            }
            context['#'+name] = docFragment;
		}
	    if(var_){
            var next = node.firstChild;
            context.append([VAR_TYPE,var_]);
            if(next){
                do{
                    context.parseNode(next,context)
                }while(next = next.nextSibling)
            }
            context.append([]);
	    }
	    if(path!=null){
	    	if(path.charAt() == '#'){
	    		doc = context['#'+name];
	    		context.url = doc.documentURI;
	    	}else{
		        var url = parentURL.replace(/[^\/]*(?:[#\?].*)?$/,path);
		        var doc = context.load(url);
	    	}
	    }
	    if(xpath!=null){
	        doc = selectNodes(doc,xpath);
	    }
	    context.parseNode(doc,context)
    }finally{
        context.url = parentURL;
    }
}
function parseIfTag(node,context){
    var next = node.firstChild;
    var test = getAttribute(node,'test',true,true);
    context.append([IF_TYPE,test]);
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
    var test = getAttribute(node,'test',true,false);
    context.append([ELSE_TYPE,test]);
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
    context.append([ELSE_TYPE]);
    if(next){
        do{
            context.parseNode(next,context)
        }while(next = next.nextSibling)
    }
    context.append([]);
}


function parseChooseTag(node,context){
	var next = node.firstChild;
	var first = true;
	var whenTag = node.tagName.split(':')[0];
	var elseTag = whenTag + ':otherwise';
	whenTag += ':when';
    if(next){
        do{
        	if(next.tagName == whenTag){
        		if(first){
        			first = false;
        			parseIfTag(next,context);
        		}else{
		            parseElseIfTag(next,context);
        		}
        	}else if(next.tagName == elseTag){
        		parseElseTag(next,context);
        	}
        	//else if(next.tagName){
        	//	$log.error("choose 只接受 when，otherwise 节点");
        	//}
        	//context.parseNode(next,context)//
		}while(next = next.nextSibling)
    }
}

function parseForTag(node,context){
    var next = node.firstChild;
    var items = getAttribute(node,'items',true);
    var var_ = getAttribute(node,'var');
    var status = getAttribute(node,'status');
    
    context.append([FOR_TYPE,var_,items,status]);
    if(next){
        do{
            context.parseNode(next,context)
        }while(next = next.nextSibling)
    }
    context.append([]);
}
function parseVarTag(node,context){
    var name = getAttribute(node,'name');
    var value = getAttribute(node,'value');
    if(value){
    	var value = parseText(value,false);
    	if(value.length == 1){
    		value = value[0];
    		if(value instanceof Array){
    			value = value[1];
    		}
    		context.append([VAR_TYPE,name,value]);
    	}else{
    		context.append([VAR_TYPE,name]);
	        context.append.apply(context,value)
	        context.append([]);
    	}
    }else{
        var next = node.firstChild;
        context.append([VAR_TYPE,name]);
        if(next){
            do{
                context.parseNode(next,context)
            }while(next = next.nextSibling)
        }
        context.append([]);
    }
}

function parseOutTag(node,context){
    var value = getAttribute(node,"value");
    value = parseText(value,false);
    context.append.apply(context,value);
}

//parser element
/*
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
*/
//parser attribute
function parseAttribute(node,context){
    var name = node.name;
    var value = node.value;
	if(isTemplateNS(value, name.toLowerCase() == "xmlns:c")){
		return true;
	}
    var buf = parseText(value,true,true);
    var isStatic;
    var isDynamic;
    //hack parseText is void 
    var i =  buf.length;
    while(i--){
        //hack reuse value param
        var value = buf[i];
        if(value.constructor == String){
            if(value){
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
            throw new Error("属性内只能有单一EL表达式！！");
        }else{//只考虑单一EL表达式的情况
            buf = buf[0];
	        context.append( [ATTRIBUTE_TYPE,buf[1],name]);
	        return true;
        }
    }
    context.append(" "+name+'="');
    if(/^xmlns$/i.test(name)){
        if(buf[0] == 'http://www.xidea.org/ns/template/xhtml'){
            buf[0] = 'http://www.w3.org/1999/xhtml'
        }
    }
    context.append.apply(context,buf);
    context.append('"');
    return true;
}
function parseTextNode(node,context){
    var data = node.data;
    context.append.apply(context,parseText(data.replace(/^\s+|\s+$/g,' '),true))
    return true;
}

function parseCDATA(node,context){
    context.append("<![CDATA[");
    context.append.apply(context,parseText(node.data));
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
    return true;
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
function parseNotation(node,context){
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

function getAttribute(node,key,isEL,required){
	var value = node.getAttribute(key);
	if(value){
		if(isEL){
	         return findFirstEL(value);
		}else{
			return value.replace(/^\s+|\s+$/g,'');
		}
	}else if(required){
		$log.error("属性"+key+"为必须值");
		throw new Error();
	}
}
function findFirstEL(value){
	var els = parseText(value,false);
	var i = els.length;
	while(i--) {
		var el = els[i];
		if(el instanceof Array){//el
		    return el[1];
		}else if(el){
			return '"' + (stringRegexp.test(value) ?
                            value.replace(stringRegexp,charReplacer) :
                            value)
                       + '"';
		}
	}
}

/**
 * @public
 */
function toDoc(text){
	if(window.DOMParser){
        var doc = new DOMParser().parseFromString(text,"text/xml");
    }else{
        //["Msxml2.DOMDocument.6.0", "Msxml2.DOMDocument.3.0", "MSXML2.DOMDocument", "MSXML.DOMDocument", "Microsoft.XMLDOM"];
        var doc = new ActiveXObject("Microsoft.XMLDOM");
        doc.loadXML(text);
    }
    return doc;
}
function getNamespaceMap(node){
	var attributes = node.attributes;
	var map = {};
	for(var i = 0;i<attributes.length;i++){
		var attribute = attributes[i];
		var name = attribute.name;
		if(/^xmlns(:.*)?$/.test(name)){
			var value = attribute.value;
			var prefix = name.substr(6) || value.replace(/^.*\/([^\/]+)\/?$/,'$1');
			map[prefix] = value;
		}
	}
	return map;
}

/**
 * TODO:貌似需要importNode
 */
function selectNodes(currentNode,xpath){
	var doc = currentNode.ownerDocument || currentNode;
    var docFragment = doc.createDocumentFragment();
    var nsMap = getNamespaceMap(doc.documentElement);
    try{
    	var buf = [];
    	for(var n in nsMap){
    		buf.push("xmlns:"+n+'="'+nsMap[n]+'"')
    	}
    	doc.setProperty("SelectionNamespaces",buf.join(' '));
    	doc.setProperty("SelectionLanguage","XPath");
        var nodes = currentNode.selectNodes(xpath);
        var buf = [];
        for (var i=0; i<nodes.length; i++) {
            buf.push(nodes.item(i))
        }
    }catch(e){
        var xpe = doc.evaluate? doc: new XPathEvaluator();
        //var nsResolver = xpe.createNSResolver(doc.documentElement);
        var result = xpe.evaluate(xpath, currentNode, function(prefix){return nsMap[prefix]}, 5, null);
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