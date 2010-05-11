/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: browser-info.js,v 1.3 2008/02/19 14:43:47 jindw Exp $
 */

function isBrowser(bn,n){
	
}

/** 
 * 检测是否是指定版本范围内的<b>[$1]</b>浏览器
 * @public 
 * @param <string|double> minVersion 最小版本
 * @param <string|double> maxVersion 最大版本
 * @id BrowserInfo.is*
 */
var falseChecker = function(minVersion,maxVersion){return false};
/**
 * BrowserInfo 对象，用于判断浏览器的相关信息，如浏览器类型、客户端语言（简体中文？英语..未实现）、操作系统（未实现）等等。
 * @public
 */
var BrowserInfo = {
    isIE : falseChecker,
    isOpera : falseChecker,
    isGecko : falseChecker,
    isNetscape : falseChecker,
    isMozilla : falseChecker,
    isFirefox : falseChecker,
    isKhtml : falseChecker,
    isSafari : falseChecker,
    isKonqueror : falseChecker//,
};

//这段打算用函数式风格重写
function Version(version){
    var vs = version.match(/([\d\.]*)(.*)?/);
    this.flag = vs[2]?vs[2].replace(/^\s+(.*[^\s])\s+$/,'$1')||'':'';
    vs = vs[1].match(/(\d+(\.\d+)?)/g); 
    for(var i = vs.length-1;i>=0;i--){
        vs[i] = parseFloat(vs[i]);
    }
    this.version = version;
    this.value = vs[0];
    this.values = vs;
}

function buildBrowserChecker(version){
    return function(min){
        if(min == null){
            return version;
        }else{
            return version<=min;
        }
    }
}
var ua = window.navigator.userAgent;
if(ua.indexOf("Opera") > 0){//Opera
    var version = ua.replace(/.*Opera\s+([^; ]+).*/,'$1');
    BrowserInfo.isOpera = buildBrowserChecker(version);
}else if(ua.indexOf(" MSIE ") > 0){//MSIE
    var version = ua.replace(/.*MSIE\s+([^; ]+).*/,'$1');
    BrowserInfo.isIE = buildBrowserChecker(version);
    if(BrowserInfo.isIE(null,5.9)){
        BrowserInfo.isQuirks = function(){return true;};
    }
}else if(ua.indexOf("Gecko/") > 0){//mozilla netscape firefox ...
    var version = ua.replace(/.*Gecko[\s\/]*([^;\/\) ]+).*/,'$1');
    BrowserInfo.isGecko = buildBrowserChecker(version);
    if(ua.indexOf("Firefox") > 0){
        var version = ua.replace(/.*Firefox[\s\/]*([^;\/\) ]+).*/,'$1');
        BrowserInfo.isFirefox = buildBrowserChecker(version);
    }else if(ua.indexOf("Netscape") > 0){
        var version = ua.replace(/.*Netscape[\s\/]*([^;\/\) ]+).*/,'$1');
        BrowserInfo.isNetscape = buildBrowserChecker(version);
    }else if(ua.indexOf("Mozilla") > 0){
        var version = ua.replace(/.*rv:([^;\/\) ]+).*/,'$1');
        BrowserInfo.isMozilla = buildBrowserChecker(version);
    }
}else if(ua.indexOf("KHTML") > 0){//khtml 
    if(ua.indexOf("Konqueror") > 0){//Konqueror 糟糕的浏览器
        //var version = ua.replace(/.*AppleWebKit\/([^;\/ ]+).*/,'$1');
        //BrowserInfo.isKhtml = buildBrowserChecker(version);
        BrowserInfo.isKhtml = function(){return true;};
        version = ua.replace(/.*Konqueror\/([^;\/ ]+).*/,'$1');
        BrowserInfo.isKonqueror = buildBrowserChecker(version);
    }
}else if(ua.indexOf("Safari") > 0){//Safari khtml 
    //var version = ua.replace(/.*AppleWebKit\/([^;\/ ]+).*/,'$1');
    //BrowserInfo.isKhtml = buildBrowserChecker(version);
    BrowserInfo.isKhtml = function(){return true;};
    version = ua.replace(/.*Safari\/([^;\/ ]+).*/,'$1');
    BrowserInfo.isSafari = buildBrowserChecker(version);
}else{
    //UNKNOW 
}
//@Deprecated ???
//for(var n in BrowserInfo){
//    window.navigator[n] = BrowserInfo[n];
//}