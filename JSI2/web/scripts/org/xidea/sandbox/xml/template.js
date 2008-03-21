/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: template.js,v 1.4 2008/02/28 14:39:06 jindw Exp $
 */

/**
 * 加载文档
 * @param <string>value xml url or xml string
 */
function loadDoc(value){
    if(/^[\s\ufeff]*</.test(value)){
        value = value.replace(/^[\s\ufeff]*</,'<');
    }else{
        var doc = new Request(value).send('',true).getXML();
        doc.documentURI = value;
        return doc;
    }
    if(window.DOMParser){//code for Mozilla, Firefox, Opera, etc.
        return new DOMParser().parseFromString(value,"text/xml");
    }else{
        var doc=new ActiveXObject("Microsoft.XMLDOM");
        doc.async="false";
        doc.loadXML(value);
        return doc;
    }
}

/**
 * @public
 * @param xmlDoc
 */
function Template(xmlDoc){
    this.taglib = {};
    this.putTaglib(new DefaultTag(), '*');
    this.putTaglib(new XHTMLTag());
    this.putTaglib(new CoreTag());
    if(typeof xmlDoc == 'string'){
        this.doc = loadDoc(xmlDoc);
    }else if(xmlDoc != null){
        this.doc = xmlDoc;
    }
}

Template.prototype.load = function(base,url){
    base = base || this.doc.documentURL || this.url;
    if(base && !/^\w{3-8}\:\/\//.test(url)){
        if(url.charAt() == '/'){
            url = base.replace(/^(\w{3-8}\:\/\/[^\/\\]+).*$/,"$1"+url);
        }else{
            url = base.replace(/[^\/]*$/,'')+url;
            while(url != (url = url.replace(/\w+\.\.\//,'')));
        }
    }
    return loadDoc(url);
    
}
Template.prototype.putTaglib = function(tl,ns){
    this.taglib[ns||tl.$namespaceURI] = tl;
}


/**
 * @public
 * @param data <Object|OutputContext> 数据集合
 * @param out <Array|Writer|Document> 数组 或者 输出接口至少需要一个成员函数 write(string)
 */
Template.prototype.render = function(data,out){
    if(out == null){
        if(data instanceof OutputContext){
            var context = data;
        }else{
            var out2 = [];
            var context = new OutputContext(data,out2);
        }
    }else{
        var context = new OutputContext(data,out);
    }
    context.template = this;
    var ctl = context.taglib;
    context.taglib = new StackMap(this.taglib);
    for(var n in ctl){
        context.taglib[n] = ctl[n];
    }
    context.output(this.doc);
    if(out2){
        return out2.join('');
    }
}
/**
 * 值栈对象
 */
function ValueStack(data){
    this.context = data;
    this.stack = [];
}
/**
 * 压入新的上下文根
 */
ValueStack.prototype.push = function(data){
    this.stack.push(this.context);
    this.context = new StackMap(this.context);
    for(var n in    data){
        this.context[n] = data[n];
    }
}
/**
 * 弹出顶层上下文根
 */
ValueStack.prototype.pop = function(){
    this.context = this.stack.pop();
}
/**
 * 在上下文对象上设置一个变量
 */
ValueStack.prototype.setVariable = function(id,value){
    this.context[id] = value
}
/**
 * 在上下文对象上获取一个变量
 */
ValueStack.prototype.getVariable = function(id){
    return this.context[id];
}
/**
 * 输出上下文对象
 * @protected
 * @param data <Object> 数据集合
 * @param out <Writer|Document> 输出接口 至少需要一个成员函数 write(string)
 */
function OutputContext(data,out){
    if(data == null){
        data = {};
    }
    this.taglib = {};
    this.valueStack = new ValueStack(data);
    this.out = out;
    if(out instanceof Array){
        this.print = this.print = arrayWrite;
    }else if(out.writeln){
        this.writeln = this.println = nativeWriteln;
    }
    this.depth = 0;
}

/**
 * @public
 */
OutputContext.prototype.putTaglib = Template.prototype.putTaglib;
/**
 * @public
 */
OutputContext.prototype.print = OutputContext.prototype.write = function(str){
    this.out.write(str)
}

/**
 * @public
 */
OutputContext.prototype.writeln = OutputContext.prototype.println = function(str){
    this.print(str);
    this.print('\r\n');
}
/**
 * @private
 */
function nativeWriteln(str){
    this.out.writeln(str);
}
function arrayWrite(str){
    this.out.push(str);
}

/**
 * @private
 */
OutputContext.prototype.printIndent = function(str){
    return ;
    this.out.write("\r\n");
    for(var i = 0;i<this.depth;i++){
        this.out.write("    ");
    }
}
/**
 * 计算表达式
 * @protected
 */
OutputContext.prototype.evalExpression = function(el){
    try{
        with(this.valueStack.context){
            return eval(el);
        }
    }catch(e){
        return this.processException(e);
    }
}
/**
 * 处理表达式计算时发生的异常
 * @protected
 * @param <Error> e 异常对象
 * @return <string> 异常后显示的字符串
 */
OutputContext.prototype.processException = function(e){
    $log.debug('expression eval error:',e);
    return '';
}
/**
 * 计算boolean值
 * @protected
 */
OutputContext.prototype.evalBoolean = function(str){
    if(str){
        switch(str.toLowerCase()){
            case 'true':
                return true;
            case 'false':
                return false;
        }
        try{
            str = this.evalText(str);
            if(str == '' || (str.toLowerCase())=='false'){
                return false;
            }else{
                return true;
            }
        }catch(e){
            return null;
        }
    }else{
        return null;
    }
}
/**
 * 计算带表达式的字符块
 * @protected
 * @param <string>str 需要计算的字符块 
 */
OutputContext.prototype.evalText = function(str){
    var k = 0;
    var result = "";
    if(!str){
        return str;
    }
    while(true){
        var i = str.indexOf("${",k);
        while(str.charAt(i-1) == '\\'){
            result += str.substring(k,i-1);
            k = i;
            i = str.indexOf("${",k+1);
        }
        if(i>=0){
            var j = str.indexOf('}',i);
            var x = str.indexOf('{',i+2);
            if(x>i && x<j){
                for(var d = 1,j = i+2;j<str.length;j++){
                    var c = str.charAt(j);
                    if(c == '\'' || c == '\"'){//跳过字符串
                        while(j<str.length){
                            j = str.indexOf(c,j+1);
                            for(var n = j-1;str.charAt(n) == '\\'; n--);
                            if((j - n)%2==1){
                                break;
                            }
                        }
                    }else if( c == '{'){
                        d++;
                    }else if (c == '}'){
                        d--;
                        if(d == 0){
                            break;
                        }
                    }
                }
            }
            if(i == 0 && j == (str.length-1)){
                var el = str.substring(i+2,j);
                return this.evalExpression(el);
            }
            if(j>0){
                var el = str.substring(i+2,j);
                var escape = str[i-1]=='!';
                if(escape){
                    result += str.substring(k,i-1);
                }else{
                    result += str.substring(k,i);
                }
                try{
                    result += this.evalExpression(el);
                }catch(e){
                    //TODO:
                }finally{
                    k = j+1;
                    //firefox bug for function toString
                    //continue;
                }
                continue;
            }else{
                result += str.substring(k,str.length);
                break;
            }
        }else{
            result += str.substring(k,str.length);
            break;
        }
    }
    return result;
};

/**
 * 编码 xml 字符节点
 * @private
 */
OutputContext.prototype.encodeText = function(str){
    return str?str.toString()
        .replace(/</g,'&lt;')
        .replace(/>/g,'&gt;')
        //.replace(/&/g,'&amp;')
        :'';
};
/**
 * 编码 xml 属性
 * @private
 */
OutputContext.prototype.encodeAttribute = function(str){
    return str?str.toString()
        .replace(/</g,'&lt;')
        .replace(/>/g,'&gt;')
        //.replace(/&/g,'&amp;')
        :'';//TODO:'"
};

/**
 * 分发节点输出处理
 * @private
 */
OutputContext.prototype.getTaglib = function(ns){
    if(ns){
        return this.taglib[ns] || this.taglib['*'];
    }else{
        return this.taglib['*'];
    }
}
/**
 * 分发节点输出处理
 * @private
 */
OutputContext.prototype.output = function(node){
    if(node==null){return;}
    var taglib = this.getTaglib(node.namespaceURI);
    switch(node.nodeType){
        case 1: //NODE_ELEMENT 
            taglib.$Element(this,node)
            break;
        case 3: //NODE_TEXT                                        
            taglib.$Text(this,node)
            break;
        case 4: //NODE_CDATA_SECTION                     
            taglib.$CDATASection(this,node)
            break;
        case 5: //NODE_ENTITY_REFERENCE                
            this.$EntityReference(this,node)
            break;
        case 6: //NODE_ENTITY                                    
            taglib.$Entity(this,node)
            break;
        case 7: //NODE_PROCESSING_INSTRUCTION    
            taglib.$ProcessingInstruction(this,node)
            break;
        case 8: //NODE_COMMENT                                 
            taglib.$Comment(this,node)
            break;
        case 9: //NODE_DOCUMENT                                
            taglib.$Document(this,node)
            break;
        case 10://NODE_DOCUMENT_TYPE                     
            taglib.$DocumentType(this,node)
            break;
        case 11://NODE_DOCUMENT_FRAGMENT             
            taglib.$DocumentFragment(this,node)
            break;
        case 12://NODE_NOTATION 
            taglib.$Notation(this,node)
            break;
        case 2: //NODE_ATTRIBUTE                             
            taglib.$Attribute(this,node)
            break;
        default:
            this.println("<!-- ERROR： UNKNOW nodeType:"+node.nodeType+"-->")
    }
};
/**
 * 栈表
 * @public 
 * @constructor
 */
function StackMap(p){
    function tc(){};
    tc.prototype = p;
    return new tc();
};
