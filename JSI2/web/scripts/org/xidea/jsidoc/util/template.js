/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: template.js,v 1.4 2008/02/28 14:39:06 jindw Exp $
 */

/**
 * 模板类
 * 起初base 的设计是表示，他是一个url还是一段文本。
 * 感觉没有必要，感觉需要简化。
 * 设计为接受两个参数？
 * <a href="http://code.google.com/p/jsiside/wiki/Template"> 模版基础指令说明</a>
 * @public
 */
function Template(data,type){
    if("org.jside.template:compile"){
        if(data.constructor == String){
            var inlineClass = {
                'xml':"org.xidea.jsidoc.util:XMLParser",
                'text':"org.xidea.jsidoc.util:TextParser"
            }
            var parser = new ($import(inlineClass[type || 'xml'] ||type))();
            parser.parse(data);
            data = parser.result;
            var i = data.length;
            while(i--){
                var item = data[i];
                while(item instanceof Array && item.length && item[item.length-1] == undefined){
                    item.pop();
                }
            }
            this.compileData = data;
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
//    function c(){}
//    c.prototype = context;
//    context = new c()
    renderList(this,context,this.data,buf)
    return buf.join("");
}


/**
 * 模版渲染函数
 * @internal
 */
function renderList(thisObject,context,data,buf){
    for(var i=0;i<data.length;i++){
        var item = data[i];
        if(item instanceof Function){
            item.call(thisObject,context,buf)
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
    var itemsStack = [[]];
    for(var i = 0;i<items.length;i++){
        var item = items[i];
        //alert(typeof item)
        if(item.constructor == String){
            if(":debug"){
                if(!itemsStack[0]){
                    $log.error("无效结构",i,items,itemsStack)
                }
            }
            itemsStack[0].push(item);
        }else{
            //alert(typeof item)
            compileItem(item,itemsStack);
        }
    }
    return itemsStack[0];
}

/**
 * 模板单元编译函数
 * @internal
 */
function compileItem(object,itemsStack){
    switch(object[0]){
        case 0://":el":
            return buildExpression(object,itemsStack);
        case 1://":attribute":
            return buildAttribute(object,itemsStack);
        case 2://":if":
            return buildIf(object,itemsStack);
        case 3://":else-if":
            return buildElseIf(object,itemsStack);
        case 4://":else":
            return buildElse(object,itemsStack);
        case 5://":for":
            return buildFor(object,itemsStack);
        case 6://":set"://var
            return buildVar(object,itemsStack);
        default://:end
            itemsStack.shift();
            //return $import(type,null,null)(object)
    }
}

/**
 * 构建表达式
 * el             [0,expression,unescape]
 * @internal
 */
function buildExpression(data,itemsStack){
    //var type = data[0];
    var el = data[1];
    var escape = !data[2];
    //if(data[0]){//==1
    el = createExpression(el)
    itemsStack[0].push(function(context,result){
        var value = el.call(this,context);
        if(escape && value!=null ){
            value = String(value).replace(/[<>&'"]/g,xmlReplacer)
        }
        result.push(value);
    });
}

/**
 * 构建标记属性
 * attribute      [1,name,expression]             //表达式
 * name="${}"
 * name = "${123}1230"?? 不可能出现，所以只能是i ==1
 * @internal
 */
function buildAttribute(data,itemsStack){
    var prefix = " "+data[1]+'="';
    var data = createExpression(data[2]);
    itemsStack[0].push(function(context,result){
        var buf = data.call(this,context);
        //alert(buf)
        if(buf!=null){
            result.push(prefix,String(buf).replace(/[<>&'"]/g,xmlReplacer)+'"');
        }
    });
}

/**
 * 构建If处理
 * if             [2,expression]                  //
 * @internal
 */
function buildIf(data,itemsStack){
    var data = createExpression(data[1]);
    var children = [];
    itemsStack[0].push(function(context,result){
        var test = data(context);
        //alert(buf)
        if(test){
            renderList(this,context,children,result);
        }
        this.$if = test;
    })
    itemsStack.unshift(children);
}

/**
 * 构建Else If处理
 * else if        [3,expression]                  //
 * @internal
 */
function buildElseIf(data,itemsStack){
    itemsStack.shift();
    var data = createExpression(data[1]);
    var children = [];
    itemsStack[0].push(function(context,result){
        if(!this.$if){
            var test = data.call(this,context);
            //alert(buf)
            if(test){
                renderList(this,context,children,result);
            }
            this.$if = test;
        }
    })
    itemsStack.unshift(children);
}

/**
 * 构建Else处理
 * else           [4]                             //
 * @internal
 */
function buildElse(data,itemsStack){
    itemsStack.shift();
    var children = [];
    itemsStack[0].push(function(context,result){
        if(!this.$if){
            //alert(buf)
            renderList(this,context,children,result);
            //delete this.test;//留着也无妨
        }
    })
    itemsStack.unshift(children);
}

/**
 * 构建循环处理
 * @internal
 * for:[5,var,itemExpression,status]
 */
function buildFor(data,itemsStack){
    var varName = data[1];    var itemExpression = createExpression(data[2]);
    var statusName = data[3];
    var children = [];
    itemsStack[0].push(function(context,result){
        data = itemExpression.call(this,context);
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
        var preiousStatus = this.$for;
        var i = 0;
        var len = data.length;
        var $for = this.$for = {end:len-1};
        //prepareFor(this);
        statusName && (context[statusName] = $for);
        for(;i<len;i++){
            $for.index = i;
            context[varName] = data[i];
            renderList(this,context,children,result);
        }
        statusName && (context[statusName] = preiousStatus);
        this.$for = preiousStatus;
        this.$if = len;
    });
    itemsStack.unshift(children);
}

/**
 * 构建申明处理
 * var            [6,name,expression]             //设置某个变量（el||string）
 * @internal
 */
function buildVar(data,itemsStack){
    var name = data[1];
    var data = data[2];
    if(data){
        data = createExpression(data);
        itemsStack[0].push(function(context,result){
            context[name] = data.call(this,context);
        })
    }else{
        //hack reuse data for hack
        data = [];
        itemsStack[0].push(function(context,result){
            result = [];
            renderList(this,context,data,result);
            context[name] = result.join('');
        })
        itemsStack.unshift(data);//#end
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
	/*
	 * /^[\w_\$]+$/.test(e)? function(context){
            return (e in context) ? context[e] : window[e];
        }:
	 */
    return function(_){
            with(_){//
	            try{
	                return _.eval?_.eval(e) : window.eval(e,_);
	            }catch(x){
	            	//$log.error(x,this,c.constructor,1,e)
	            }
            }
        }
}
