/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: template.js,v 1.4 2008/02/28 14:39:06 jindw Exp $
 */

/**
 * 模板类
 * <a href="http://code.google.com/p/jsiside/wiki/Template"> 模版基础指令说明</a>
 * @public
 */
function Template(data,base,type){
    if("org.jside.template:compile"){
        if(data.constructor == String){
            var inlineClass = {
                'xml':"org.xidea.jsidoc.util:XMLParser",
                'text':"org.xidea.jsidoc.util:TextParser"
            }
            var parser = new ($import(inlineClass[type || 'xml'] ||type))();
            parser.parse(data,base);
            data = parser.result;
        }
    }
    //alert(data.join("\n"));;
    /**
     * 模板数据
     * @private
     * @tyoeof string
     */
    this.data = compile(data,[]);
    
    //alert(this.data)
}

/**
 * 渲染模板
 * @public
 */
Template.prototype.render = function(context){
    var buf = [];
    renderList(createContext(context),this.data,buf)
    return buf.join("");
}

function createContext(context){
    function c(){}
    c.prototype = context;
    context = new c();
    context.____ = {}
    return context;
}

/**
 * 模版渲染函数
 * @internal
 */
function renderList(context,data,buf){
    for(var i=0;i<data.length;i++){
        var item = data[i];
        if(item instanceof Function){
            item(context,buf)
        }else{
            buf.push(item);
        }
    }
}

/**
 * 编译模板数据,这里没有递归调用
 * @internal
 */
function compile(items){
    var unitStack = [[]];
    for(var i = 0;i<items.length;i++){
        var item = items[i];
        //alert(typeof item)
        if(item.constructor == String){
            unitStack[0].push(item);
        }else{
            //alert(typeof item)
            item = compileItem(item,unitStack);
            item != '' && unitStack[0].push(item);
        }
    }
    return unitStack[0];
}

/**
 * 模板单元编译函数
 * @internal
 */
function compileItem(object,unitStack){
    switch(object[0]){
        case 0://":el":
            return buildExpression(object,unitStack);
        case 1://":attribute":
            return buildAttribute(object,unitStack);
        case 2://":if":
            return buildIf(object,unitStack);
        case 3://":else-if":
            return buildElseIf(object,unitStack);
        case 4://":else":
            return buildElse(object,unitStack);
        case 5://":for":
            return buildFor(object,unitStack);
        case 6://":set"://var
            return buildVar(object,unitStack);
        default://:end
            unitStack.shift();
            return '';
            //return $import(type,null,null)(object)
    }
}

/**
 * 构建表达式
 * el             [0,expression,unescape]
 * @internal
 */
function buildExpression(data){
    //var type = data[0];
    var el = data[1];
    var escape = !data[2];
    //if(data[0]){//==1
    el = createExpression(el)
    return function(context,result){
        var value = el(context);
        if(escape && value!=null ){
            value = value.toString().replace(/[<>&'"]/g,xmlReplacer)
        }
        result.push(value);
    }
}

/**
 * 构建标记属性
 * attribute      [1,name,expression]             //表达式
 * name="${}"
 * name = "${123}1230"?? 不可能出现，所以只能是i ==1
 * @internal
 */
function buildAttribute(data){
    var prefix = " "+data[1]+'="';
    var data = createExpression(data[2]);
    return function(context,result){
        var buf = data(context);
        //alert(buf)
        if(buf!=null){
            result.push(prefix,String(buf).replace(/[<>&'"]/g,xmlReplacer)+'"');
        }
    }
}

/**
 * 构建If处理
 * if             [2,expression]                  //
 * @internal
 */
function buildIf(data,unitStack){
    var data = createExpression(data[1]);
    var children = [];
    unitStack[0].push(function(context,result){
        var test = data(context);
        //alert(buf)
        if(test){
            renderList(context,children,result);
        }
        context.____.$if = test;
    })
    unitStack.unshift(children);
}

/**
 * 构建Else If处理
 * else if        [3,expression]                  //
 * @internal
 */
function buildElseIf(data,unitStack){
    unitStack.shift();
    var data = createExpression(data[1]);
    var children = [];
    unitStack[0].push(function(context,result){
        if(!context.____.$if){
            var test = data(context);
            //alert(buf)
            if(test){
                renderList(context,children,result);
            }
            context.____.$if = test;
        }
    })
    unitStack.unshift(children);
}

/**
 * 构建Else处理
 * else           [4]                             //
 * @internal
 */
function buildElse(data,unitStack){
    unitStack.shift();
    var children = [];
    unitStack[0].push(function(context,result){
         
        if(!context.____.$if){
            //alert(buf)
            renderList(context,children,result);
            //delete context.____.test;//留着也无妨
        }
    })
    unitStack.unshift(children);
}

/**
 * 构建循环处理
 * @internal
 * for:[5,var,itemExpression,status]
 */
function buildFor(data,unitStack){
    var varName = data[1];    var itemExpression = createExpression(data[2]);
    var statusName = data[3];
    var children = [];
    unitStack[0].push(function(context,result){
        data = itemExpression(context);
        //alert(data.constructor)
        if(!(data instanceof Array)){
            //hack $for as buf
            $for = [];
            //hack len as key
            for(var len in data){
                $for.push(len);
            }
            data = $for;
        }
        var status = context.____;
        var preiousStatus = status.$for;
        
        var i = 0;
        var len = data.length;
        var $for = status.$for = {end:len-1};
        //prepareFor(status);
        context[statusName] = $for;
        for(;i<len;i++){
            $for.index = i;
            context[varName] = data[i];
            renderList(context,children,result);
        }
        status.$for = preiousStatus;
        status.$if = len;
    });
    unitStack.unshift(children);
}

/**
 * 构建申明处理
 * var            [6,name,expression]             //设置某个变量（el||string）
 * @internal
 */
function buildVar(data){
    var name = data[1];
    var data = createExpression(data[2]);
    return function(context,result){
        context[name] = data(context);
    }
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

function createExpression(e){
    return /^[\w_\$]+$/.test(e)? function(context){
            return (e in context) ? context[e] : window[e];
        }:function(c){
            with(c){//
                return c.eval?c.eval(e):window.eval(e);
            }
        }
}
