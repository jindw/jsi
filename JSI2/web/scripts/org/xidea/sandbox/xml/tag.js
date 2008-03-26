/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: tag.js,v 1.5 2008/02/28 14:39:06 jindw Exp $
 */



/**
 * 默认的 xml 标记 处理对象。
 * @public
 */
function DefaultTag(){
}
DefaultTag.prototype.$namespaceURI = '*';
/**
 * @protected
 */
DefaultTag.prototype.$Element = function(context,node){
    context.printIndent();
    context.print('<');
    context.print(node.tagName);
    var attrs = node.attributes;
    for (var i=0; i<attrs.length; i++) {
        var attr = attrs.item(i);
        this.$Attribute(context,attr)
    }
    var nl = node.childNodes;
    if(nl==null || nl.length == 0){
        context.println('/>');
    }else{
        context.print('>');
        context.depth++;
        for (var i=0; i<nl.length; i++) {
            context.output(nl.item(i))
        }
        context.depth--;
        context.printIndent();
        context.print('</');
        context.print(node.tagName);
        context.println('>');
    }
}


var xmlns = /^xmlns$|^xmlns:/
/**
 * @protected
 */
DefaultTag.prototype.$Attribute = function(context,node){
    var value = context.evalText(node.value);
    if(value == null || value == ''){return}
    if(xmlns.test(node.name)){
        var tag = context.getTaglib(value);
        if(tag){
            value = tag.$realNamespaceURI || value;
        }
    }
    context.print(' ');
    context.print(node.name);
    context.print('="');
    context.print(context.encodeAttribute(value));
    context.print('"');
}
/**
 * @protected
 */
DefaultTag.prototype.$Text = function(context,node){
    context.print(context.evalText(node.data));
}
/**
 * @protected
 */
DefaultTag.prototype.$CDATASection = function(context,node){
    context.print("<![CDATA[");
    context.print(context.evalText(node.data));
    context.print("]]>");
}

/**
 * @protected
 */
DefaultTag.prototype.$EntityReference = function(context,node){
}
/**
 * @protected
 */
DefaultTag.prototype.$Entity = function(context,node){
}
/**
 * @protected
 */
DefaultTag.prototype.$ProcessingInstruction = function(context,node){
    context.print("<?");
    context.print(node.nodeName);
    context.print(" ");
    context.print(node.data+"?>");
}
/**
 * @protected
 */
DefaultTag.prototype.$Comment = function(context,node){
     context.print("<!--");
     context.print(context.encodeText(node.data));
     context.print("-->");
}
/**
 * @protected
 */
DefaultTag.prototype.$Document = function(context,node){
    for(var n = node.firstChild;n!=null;n = n.nextSibling){
        context.output(n);
    }
}
/**
 * @protected
 */
DefaultTag.prototype.$DocumentType = function(context,node){
    //context.print("<!DOCTYPE "+node.nodeName+"[]>");
    if(node.xml){
        context.print(node.xml);
    }else{
        if(node.publicId){
            context.print('<!DOCTYPE ');
            context.print(node.nodeName);
            context.print(' PUBLIC "');
            context.print(node.publicId );
            context.print( '" "');
            context.print(node.systemId);
            context.print('">');
        }else{
            context.print("<!DOCTYPE ");
            context.print(node.nodeName);
            context.print("[");
            context.print(node.internalSubset);
            context.print("]>");
        }
    }
}
/**
 * @protected
 */
DefaultTag.prototype.$DocumentFragment = function(context,node){
    var nl = node.childNodes;
    for (var i=0; i<nl.length; i++) {
        context.output(nl.item(i))
    }
}
/**
 * @protected
 */
DefaultTag.prototype.$Notation = function(context,node){
}
function AbstractTag(){
}
AbstractTag.prototype = new DefaultTag();
AbstractTag.prototype.$Element = function(context,node){
    var localName = node.localName || node.baseName;
    if(this[localName] instanceof Function){
        this[localName](context,node);
    }else{
        DefaultTag.prototype.$Element.apply(this,context,node);
    }
}

function XHTMLTag(){
}
XHTMLTag.prototype = new AbstractTag();
XHTMLTag.prototype.$namespaceURI = "http://www.xidea.org/taglib/xhtml";
XHTMLTag.prototype.$realNamespaceURI = "http://www.w3.org/1999/xhtml";

var htmlLeaf = /^meta$|^link$|^img$|^br$|^hr$/i;
/**
 * @protected
 */
XHTMLTag.prototype.$Element = function(context,node){
    context.printIndent();
    context.print('<');
    context.print(node.tagName);
    var attrs = node.attributes;
    for (var i=0; i<attrs.length; i++) {
        var attr = attrs.item(i);
        this.$Attribute(context,attr)
    }
    var nl = node.childNodes;
    if(nl==null || nl.length == 0){
        if(htmlLeaf.test(node.tagName)){//for html
            context.println('/>');
        }else{
            context.println('>');
            context.print('</');
            context.print(node.tagName);
            context.println('>');
        }
    }else{
        context.print('>');
        context.depth++;
        //fix opera bug
        if(node.tagName.toUpperCase() == 'SCRIPT'){
            for (var i=0; i<nl.length; i++) {
                var c = nl.item(i);
                if(c.nodeType == 4){ //NODE_CDATA_SECTION 
                    if(/^[\s$]*\/\//.test(c.data)){
                        context.print('//');
                    }
                }
                context.output(c);
            }
        }else{
            for (var i=0; i<nl.length; i++) {
                context.output(nl.item(i));
            }
        }
        context.depth--;
        context.printIndent();
        context.print('</');
        context.print(node.tagName);
        context.println('>');
    }
}
function CoreTag(){
}
CoreTag.prototype = new AbstractTag();
CoreTag.prototype.$namespaceURI = "http://www.xidea.org/taglib/core";
/*
getBegin()
getCount()
getCurrent()
getEnd()
getIndex()
getStep()
isFirst()
isLast()
*/
function VarStatus(items){
    this.items = items;
    this.begin = 0;
    this.end = this.items.length-1;
    this.step = 1;
    this.current = this.items[0];
    this.count = this.items.length;
    this.index = this.begin;
    this.first = true;
    this.last = this.count == 1;
}
VarStatus.prototype.next = function(){
    this.index++;
    this.first = false;
    this.last = this.index+1>=this.count;
    this.current = this.items[this.index];
}
function Iterator(items){
    if(items instanceof Array){
        this.items = items;
    }else if(items.hasNext && items.next){
        return items;
    }else{
        this.items = [items];
    }
    this.index = 0;
}
Iterator.prototype.hasNext = function(){
    return this.index<this.items.length;
}
Iterator.prototype.next = function(){
    return this.items[this.index++];
}
CoreTag.prototype.forEach = function(context,node){
    var items = node.getAttribute("items");
    items = context.evalText(items);
    var varStatus = node.getAttribute("varStatus");
    if(varStatus){
        var status = new VarStatus(items);
        context.valueStack.setVariable(varStatus,status);
    }
    var id = node.getAttribute('var');
    if(items ){
        
        items = new Iterator(items);
        while(items.hasNext()){
            try{
                var item = items.next();
                if(id){
                    context.valueStack.setVariable(id,item);
                }else{
                    context.valueStack.push(item);
                }
                var nl = node.childNodes;
                for (var j=0; j<nl.length; j++) {
                    context.output(nl.item(j))
                }
            }finally{
                if(!id)context.valueStack.pop();
                if(status)status.next();
            }
        }
    }
}
CoreTag.prototype['if'] = function(context,node){
    var test = node.getAttribute("test");
    test = context.evalText(test);
    context.valueStack.setVariable("if",test);
    if(test){
        var nl = node.childNodes;
        for (var j=0; j<nl.length; j++) {
            context.output(nl.item(j))
        }
    }
}
CoreTag.prototype['elseif'] = function(context,node){
    var test = context.valueStack.getVariable("if");
    if(test){
        this['if'](context,node);
    }
}
CoreTag.prototype['else'] = function(context,node){
    var test = context.valueStack.getVariable("if");
    if(!test){
        var nl = node.childNodes;
        for (var j=0; j<nl.length; j++) {
            context.output(nl.item(j))
        }
    }
}
CoreTag.prototype.choose = function(context,node){
    try{
        context.valueStack.push({choose:false});
        
        var nl = node.childNodes;
        for (var j=0; j<nl.length; j++) {
            context.output(nl.item(j))
        }
    }finally{
        context.valueStack.pop();
    }
}

CoreTag.prototype.include = function(context,node){
    try{
        var path = node.getAttribute('path');
        var xpath = node.getAttribute('xpath');
        
        if(path == null){
            var doc = node.ownerDocument;
        }else{
            var doc = context.template.load(node.ownerDocument.documentURI,path);//BUG document base....
            
        }
        if(xpath != null){
            if(document.all){
                var nodes = doc.selectNodes(xpath);
                for (var i=0; i<nodes.length; i++) {
                    context.output(nodes.item(i))
                }
            }else{
                var xpe = new XPathEvaluator();
                var nsResolver = xpe.createNSResolver(doc.documentElement);
                var result = xpe.evaluate(xpath, doc.documentElement, nsResolver, 5, null);
                var node;
                while (node = result.iterateNext()){
                    node.removeAttribute("id")
                    context.output(node)
                }
            }
        }
    }finally{
    }
}
CoreTag.prototype.when = function(context,node){
    var test = context.valueStack.getVariable("choose");
    if(!test){
        var test = node.getAttribute("test");
        test = context.evalText(test);
        context.valueStack.setVariable("choose",test);
        if(test){
            var nl = node.childNodes;
            for (var j=0; j<nl.length; j++) {
                context.output(nl.item(j))
            }
        }
    }
}
CoreTag.prototype.otherwise = function(context,node){
    var test = context.valueStack.getVariable("choose");
    if(!test){
        var nl = node.childNodes;
        for (var j=0; j<nl.length; j++) {
            context.output(nl.item(j))
        }
    }
}
CoreTag.prototype.script = function(context,node){
    with(context){
        with(context.valueStack.context){
            var nl = node.childNodes;
            for (var j=0; j<nl.length; j++) {
                var t = nl.item(j);
                if(t.nodeType == 3 || t.nodeType == 4){//        case 3: //NODE_TEXT case 4: //NODE_CDATA_SECTION 
                    eval(t.data);
                }
            }
        }
    }
}

CoreTag.prototype.out = function(context,node){
    var esc = context.evalBoolean(node.getAttribute('escapeXml'));
    var value = node.getAttribute('value')||'';
    if(value){
        try{
            value = context.evalText(value)
        }catch(e){value = '';}
    }
    if(!value && (value = node.getAttribute('default'))){
        try{
            value = context.evalText(value)
        }catch(e){value = '';}
    }
    if(esc){
        value = context.encodeText(value);
    }
    context.print(value);
}
/**
 * eg:&lt;c:set var="bookId" value="${param.Remove}"/&gt;
 */
CoreTag.prototype.set = function(context,node){
    var value = node.getAttribute("value");
    value = context.evalText(value);
    context.valueStack.setVariable(node.getAttribute("var"),value);
}