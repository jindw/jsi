/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */

if(window.$JSI && $JSI.impl){
	function output(title,bindLevel,msg){
		return $JSI.impl.log(title,bindLevel,msg);
	}
}else{
	function output(title,bindLevel,msg){
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
				if(typeof (window.console && console.log) == 'function'){
		    		console.log(msg)
				}
			}
        }
        return msg;
    }
}
function JSILog(plog){
	for(var n in plog){
		this[n] = plog[n];
	}
}
JSILog.prototype = {
	/**
	 * 基本级别(不输出，只有大于才输出)
	 */
	level : 1,
	/**
	 * 用户许可级别（最终用户使用过程中禁止某些级别输出）
	 */
	userLevel : 1,
	title:'',
	clone:function(title){
		var c = new JSILog(this);
		c.title = title;
		return c;
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
	    return buf.join('');
	}
}
//var confirm = window.confirm || function(arg){
//	(this.print||java.lang.System.out.print)(String(arg))
//	return true;
//}
		
var $log = new JSILog();
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

