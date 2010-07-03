/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */

/**
 */
function output(outputLevel,msg){
    var temp = [];
    var outputLevelName = logLevelNameMap[outputLevel];
    var i = 2;
    temp.push(outputLevelName,":\n");
    while(msg || i<arguments.length){
        if(msg instanceof Object){
            temp.push(msg,"{");
            for(var n in msg){
                temp.push(n,":",msg[n],";");
            }
            temp.push("}\n");
        }else{
            temp.push(msg,"\n");
        }
        msg = arguments[i++];
    }
    temp = temp.join('');
    if(outputLevel>this.userLevel){
    	var n =window.$JSI && $JSI.impl;
    	n =n?n.log(outputLevel,temp)!==false:confirm(temp+"\n\n继续弹出 ["+this.title+"]"+outputLevelName+" 日志?\r\n");
        if(n){
        	this.userLevel = outputLevel+1;
        }
        return temp;
    }else if(":debug"){
    	if((typeof console == 'object') && (typeof console.log == 'function')){
        	console.log(msg)
    	}
    }
    return temp;
}

/*
 * @param bindLevel 绑定函数的输出级别，只有该级别大于等于输出级别时，才可输出日志
 */
function buildLevelLog(bindLevel){
    return function(){
	    if(bindLevel>this.level){
            var msg = [bindLevel];
            msg.push.apply(msg,arguments);
            msg = output.apply(this,msg);
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

