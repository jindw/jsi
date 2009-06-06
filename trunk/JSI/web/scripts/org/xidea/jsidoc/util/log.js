/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */

function $log(){
    var i = 0;
    var temp = [];
    if(this == $log){
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
        temp.push("\n\n继续弹出 ",temp[0]," 日志?\r\n");
        if(!confirm(temp.join(''))){
            consoleLevel = bindLevel+1;
        }
    }else{
        confirm(temp.join(''));
    }
}
/**
 * 设置日志级别
 * 默认级别为debug
 * @protected
 */
$log.setLevel = function(level){
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
        $log("unknow logLevel:"+level);
    }
};
/*
 * @param bindLevel 绑定函数的输出级别，只有该级别大于等于输出级别时，才可输出日志
 */
function buildLevelLog(bindLevel,bindName){
	if(!this.confirm){
		this.confirm = print;
	}
    return function(){
        if(bindLevel>=consoleLevel){
            var msg = [bindLevel,bindName];
            msg.push.apply(msg,arguments);
            $log.apply($log,msg);
        }
        if(":debug"){
            if((typeof console == 'object') && (typeof console.log == 'function')){
                var msg = [bindLevel,bindName];
                msg.push.apply(msg,arguments);
                console.log(msg.join(';'))
            }
        }
    }
}
var logLevelNameMap = "trace,debug,info,warn,error,fatal".split(',');
var consoleLevel = 1;
/* 
 * 允许输出的级别最小 
 * @hack 先当作一个零时变量用了
 */
var logLevelIndex = logLevelNameMap.length;

while(logLevelIndex--){
    var logName = logLevelNameMap[logLevelIndex];
    $log[logName] = buildLevelLog(logLevelIndex,logName);
};
