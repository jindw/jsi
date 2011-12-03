/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */
if(typeof window.confirm !='function'){
	function output(title,bindLevel,msg){
		return $JSI.impl.log(title,bindLevel,msg);
	}
}else{
	output = function (title,bindLevel,msg){
    	var outputLevelName = logLevelNameMap[bindLevel];
		return confirm(outputLevelName+':' + msg+"\n\n继续弹出 ["+title+"]"+outputLevelName+" 日志?\r\n");
	}
}
/*
 * @param bindLevel 绑定函数的输出级别，只有该级别大于等于输出级别时，才可输出日志
 */
function buildLevelLog(bindLevel){
    return function(){
	    if(bindLevel>this.level){
            var msg = this.format.apply(this,arguments);
            if(bindLevel>this.userLevel){
		        if(output(this.title,bindLevel,msg)===false){
		        	this.userLevel = bindLevel;
		        }
		    }
		    if(":debug"){
				if(typeof (window.console && window.console.log) == 'function'){
		    		window.console.log(msg)
				}
			}
        }
        return msg;
    }
}
function JSILog(title,level){
	/**
	 * 基本级别(不输出，只有大于才输出)
	 */
	this.title = title;
	this.level = level;
}
JSILog.prototype = {
	/**
	 * 用户许可级别（最终用户使用过程中禁止某些级别输出）
	 */
	userLevel : 1,
	filters:[],
	clone:function(title){
		var c = new JSILog(title,this.level);
		return c;
	},
	addFilter:function(f){
		this.filters.push(f);
	},
	dir:function(o){
		var buf = [];
		for(o in o){
			buf.push(o);
		}
		this.info(buf.join('\n'))
	},
	format: function(msg){
	    for(var buf = [],i = 0;i<arguments.length;i++){
	    	msg = arguments[i];
	        if(msg instanceof Array){
	        	buf.push('[',msg,']\n');
	        }else if(msg instanceof Object){
	            buf.push(msg,"{");
	            for(var n in msg){
	                buf.push(n,":",msg[n],",");
	            }
	            buf.push("}\n");
	        }else{
	            buf.push(msg,"\n");
	        }
	    }
	    buf =  buf.join('');
	    for(var i=0;i<this.filters.length;i++){
	    	buf = this.filters[i].call(this,buf)
	    }
	    return buf;
	}
}
//var confirm = window.confirm || function(arg){
//	(this.print||java.lang.System.out.print)(String(arg))
//	return true;
//}
		
var $log = new JSILog('',1);
var logLevelNameMap = "trace,debug,info,warn,error,fatal".split(',');
/* 
 * 允许输出的级别最小 
 * @hack 先当作一个零时变量用了
 */
var logLevelIndex = logLevelNameMap.length;

while(logLevelIndex--){
    var logName = logLevelNameMap[logLevelIndex];
    JSILog.prototype[logName] = buildLevelLog(logLevelIndex);
};
var console= $log;
