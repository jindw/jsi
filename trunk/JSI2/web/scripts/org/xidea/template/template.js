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
        if(!(data instanceof Array)){
            var inlineClass = {
                'xml':"org.xidea.template:XMLParser",
                'text':"org.xidea.template:TextParser"
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
    var context2 = {};
    for(var n in context){
        context2[n] = context[n];
    }
    //context2["this"] = context2;
    renderList(context2,this.data,buf)
    return buf.join("");
}
//function Context(context){
//    for(var n in context){
//        this[n] = context[n];
//    }
//    this["this"] = this;
//}
//Context.prototype = window;
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
/*
var EL_TYPE = 0;
var VAR_TYPE = 1;//":set"://var
var IF_TYPE = 2;//":if":
var ELSE_TYPE = 3;//":else":
var FOR_TYPE = 4;//":for":

var ATTRIBUTE_TYPE = 6;//":attribute":

var FOR_KEY = "_[4]";
 */
/**
 * 模板单元编译函数
 * @internal
 */
function compileItem(object,itemsStack){
    switch(object[0]){
        case 0://":el":
            buildExpression(object,itemsStack);
            break;
        case 6://":encode_el":
            buildExpression(object,itemsStack,true);
            break;
        case 7://":attribute":
            buildAttribute(object,itemsStack);
            break;
        case 1://":set"://var
            buildVar(object,itemsStack);
            break;
        case 2://":if":
            buildIf(object,itemsStack);
            break;
        case 3://":else-if":":else":
            buildElse(object,itemsStack);
            break;
        case 4://":for":
            buildFor(object,itemsStack);
            break;
        default://:end
            itemsStack.shift();
            //return $import(type,null,null)(object)
    }
}

/**
 * 构建表达式
 * el             [EL_TYPE,expression]
 * @internal
 */
function buildExpression(data,itemsStack,encode){
    var el = data[1];
    //if(data[0]){//==1
    el = createExpression(el)
    itemsStack[0].push(function(context,result){
        var value = el(context);
        if(encode && value!=null ){
            value = String(value).replace(/[<>&]/g,xmlReplacer)
        }
        result.push(value);
    });
}

/**
 * 构建标记属性
 * attribute      [ATTRIBUTE_TYPE,expression,name]             //表达式
 * name="${}"
 * name = "${123}1230"?? 不可能出现，所以只能是i ==1
 * @internal
 */
function buildAttribute(data,itemsStack){
    var prefix = data[2];
    var data = createExpression(data[1]);
    if(prefix){
    	prefix = " "+prefix+'="';
    	itemsStack[0].push(function(context,result){
	        var buf = data(context);
	        if(buf!=null){
	        	buf = String(buf);
	        	if(buf.length){
	        	    result.push(prefix,buf.replace(/[<>&"]/g,xmlReplacer)+'"');
	        	}
	        }
	    });
    }else{
	    itemsStack[0].push(function(context,result){
	        var buf = data(context);
	    	result.push(String(buf).replace(/[<>&'"]/g,xmlReplacer));
	    });
    }
}

/**
 * 构建If处理
 * if             [IF_TYPE,expression]                  //
 * @internal
 */
function buildIf(data,itemsStack){
    var data = createExpression(data[1]);
    var children = [];
    itemsStack[0].push(function(context,result){
        var test = data(context);
        //alert(buf)
        if(test){
            renderList(context,children,result);
        }
        context[2] = test;//if passed(一定要放下来，确保覆盖)
    })
    itemsStack.unshift(children);
}

/**
 * 构建Else If处理
 * else if        [ELSE_TYPE,expression]                  //
 * @internal
 */
function buildElse(data,itemsStack){
    itemsStack.shift();
    var data = data[1] == null ? null:createExpression(data[1]);
    var children = [];
    itemsStack[0].push(function(context,result){
        if(!context[2]){
            if(!data || data(context)){//if key
                renderList(context,children,result);
                context[2] = true;//if passed(不用要放下去，另一分支已正常)
            }
        }
    })
    itemsStack.unshift(children);
}

/**
 * 构建循环处理
 * @internal
 * for:[FOR_TYPE ,var,itemExpression,status]
 */
function buildFor(data,itemsStack){
    var varName = data[1];    var itemExpression = createExpression(data[2]);
    var statusName = data[3];
    var children = [];
    itemsStack[0].push(function(context,result){
        var data = itemExpression(context);
        //alert(data.constructor)
        if(!(data instanceof Array)){
            //hack $for as buf
            forStatus = [];
            //hack len as key
            for(var len in data){
                forStatus.push(len);
            }
            data = forStatus;
        }
        var preiousStatus = context[4];
        var i = 0;
        var len = data.length;
        var forStatus = context[4] = {lastIndex:len-1,depth:preiousStatus?preiousStatus.depth+1:0};
        //prepareFor(this);
        if(statusName){
        	context[statusName] = forStatus;
        }
        for(;i<len;i++){
            forStatus.index = i;
            context[varName] = data[i];
            renderList(context,children,result);
        }
        if(statusName){
            context[statusName] = preiousStatus;
        }
        context[4] = preiousStatus;//for key
        context[2] = len;//if key
    });
    itemsStack.unshift(children);
}

/**
 * 构建申明处理
 * var            [VAR_TYPE,name,expression]             //设置某个变量（el||string）
 * @internal
 */
function buildVar(data,itemsStack){
    var name = data[1];
    var data = data[2];
    if(data!=null){
        data = createExpression(data);
        itemsStack[0].push(function(context,result){
            context[name] = data(context);
        })
    }else{
        //hack reuse data for hack
        data = [];
        itemsStack[0].push(function(context,result){
            result = [];
            renderList(context,data,result);
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
function createExpression(el){
	switch(el.constructor){
    case String:
        el = el.split('.')
	    return function(_){
            try{
    	        var i = el.length-1;
    	        var v = el[i];
    	        if(v!='this'){
    	            _=(v in _ ? _:this)[v];
    	        }
    	        while(i--){
    	            _ = _[el[i]]
    	        }
    	        return _;
	        }catch(e){
               $log.trace(e);
            }
	    }
	case Function:
		return function(_){
	         try{
	             return el.call(_);
	         }catch(e){
	             $log.trace(e);
	         }
	     };
	case Array:
	    //el.length>0...预留吧，用于以后解释执行
	    el = el[0];
	}
	return function(_){
         return el;
    };
}
