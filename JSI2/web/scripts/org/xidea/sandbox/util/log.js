/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/jsi/
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General 
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) 
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 * details.
 *
 * @author jindw
 * @version $Id: boot-log.js,v 1.4 2008/02/20 15:42:03 jindw Exp $
 */
 
/** 
 * 输出<b>[$1]</b>级别的日志信息
 * <ul><pre>
 * trace : 输出跟踪级别的日志信息（比debug级别更低）
 * debug : 输出调试级别的日志信息
 * info  : 输出信息级别的日志信息
 * warn  : 输出警告级别的日志信息
 * error : 输出错误级别的日志信息
 * fatal : 输出致命错误级别的日志信息
 * </pre></ul>
 * @public 
 * @param <object> arg1 第一个参数
 * @param <object>arg2 第一个参数
 * @arguments <object>arg... 第N个参数
 * 
 * @id log.*
 */
/**
 * 全局日志
 * <p>JSI 可选功能,你也可以使用JSA将代码中的日志处理信息清除。<p>
 * <p>自JSI2.1之后，只有全局日志，没有装在单元日志了。<p>
 * @typeof object
 * @public
 */
var logLevelNameMap = "trace,debug,info,warn,error,fatal".split(',');
var consoleLevel = 1;
function log(){
    var i = 0;
    var temp = [];
    if(this == log){
        var bindLevel = arguments[i++];
        temp.push(arguments[i++],":\n\n");
    }
    while(i<arguments.length){
        var msg = arguments[i++]
        if(msg instanceof Object){
            temp.push(msg,"{");
            for(var n in msg){
                temp.push(n,":",msg[n],";");
            }
            temp.push("}\n");
        }else{
            temp.push(msg,"\n");
        }
    }
    if(bindLevel >= 0){
        temp.push("\n\n继续弹出 "+temp[0]+" 日志?");
        if(!confirm(temp.join(''))){
            consoleLevel = bindLevel+1;
        }
    }else{
        alert(temp.join(''));
    }
}
/*
 * @param bindLevel 绑定函数的输出级别，只有该级别大于等于输出级别时，才可输出日志
 */
function buildLevelLog(bindLevel,bindName){
    return function(){
        if(bindLevel>=consoleLevel){
            var msg = [bindLevel,bindName];
            msg.push.apply(msg,arguments);
            log.apply(log,msg);
        }
    }
}
/* 
 * 允许输出的级别最小 
 * @hack 先当作一个零时变量用了
 */
var i = logLevelNameMap.length;
while(i--){
    var logName = logLevelNameMap[i];
    log[logName] = buildLevelLog(i,logName);
};
/**
 * 设置日志级别
 * 默认级别为debug
 * @id log.setLevel
 * @protected
 */
log.setLevel = function(level){
    if(logLevelNameMap[level]){
        consoleLevel = level;
    }else{
        var i = logLevelNameMap.length;
        level = level.toLowerCase();
        while(i--){
            if(logLevelNameMap[i] == level){
                consoleLevel = i;
                return;
            }
        }
        log("unknow logLevel:"+level);
    }
};
