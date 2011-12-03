/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: browser-info.js,v 1.3 2008/02/19 14:43:47 jindw Exp $
 */

function isBrowser(bn,n){
	switch(bn.toUpperCase()){
	case 'IE':
	case 'MSIE':
		return isIE(n);
	case 'OPERA':
		return isOpera(n);
	case 'GECKO':
		return isGecko(n);
	case 'NETSCAPE':
		return isNetscape(n);
	case 'MOZILLA':
		return isMozilla(n);
	case 'FIREFOX':
		return isFirefox(n);
	case 'KHTML':
		return isKhtml(n);
	case 'SAFARI':
		return isSafari(n);
	case 'KONQUEROR':
		return isKonqueror(n);
	}
	return true;
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
var isIE = falseChecker;
var isOpera = falseChecker;
var isGecko = falseChecker;
var isNetscape = falseChecker;
var isMozilla = falseChecker;
var isFirefox = falseChecker;
var isKhtml = falseChecker;
var isSafari = falseChecker;
var isKonqueror = falseChecker//;

function buildBrowserChecker(version){
    return function(min){
        if(min == null){
            return version;
        }else{
            return version<=min;
        }
    }
}
var ua = window && window.navigator && window.navigator.userAgent || '';
if(ua.indexOf("Opera") > 0){//Opera
    var version = ua.replace(/.*Opera\s+([^; ]+).*/,'$1');
    isOpera = buildBrowserChecker(version);
}else if(ua.indexOf(" MSIE ") > 0){//MSIE
    var version = ua.replace(/.*MSIE\s+([^; ]+).*/,'$1');
    isIE = buildBrowserChecker(version);
    if(isIE(null,5.9)){
        var isQuirks = function(){return true;};
    }
}else if(ua.indexOf("Gecko/") > 0){//mozilla netscape firefox ...
    var version = ua.replace(/.*Gecko[\s\/]*([^;\/\) ]+).*/,'$1');
    isGecko = buildBrowserChecker(version);
    if(ua.indexOf("Firefox") > 0){
        var version = ua.replace(/.*Firefox[\s\/]*([^;\/\) ]+).*/,'$1');
        isFirefox = buildBrowserChecker(version);
    }else if(ua.indexOf("Netscape") > 0){
        var version = ua.replace(/.*Netscape[\s\/]*([^;\/\) ]+).*/,'$1');
        isNetscape = buildBrowserChecker(version);
    }else if(ua.indexOf("Mozilla") > 0){
        var version = ua.replace(/.*rv:([^;\/\) ]+).*/,'$1');
        isMozilla = buildBrowserChecker(version);
    }
}else if(ua.indexOf("KHTML") > 0){//khtml 
    if(ua.indexOf("Konqueror") > 0){//Konqueror 糟糕的浏览器
        //var version = ua.replace(/.*AppleWebKit\/([^;\/ ]+).*/,'$1');
        //isKhtml = buildBrowserChecker(version);
        isKhtml = function(){return true;};
        version = ua.replace(/.*Konqueror\/([^;\/ ]+).*/,'$1');
        isKonqueror = buildBrowserChecker(version);
    }
}else if(ua.indexOf("Safari") > 0){//Safari khtml 
    //var version = ua.replace(/.*AppleWebKit\/([^;\/ ]+).*/,'$1');
    //isKhtml = buildBrowserChecker(version);
    isKhtml = function(){return true;};
    version = ua.replace(/.*Safari\/([^;\/ ]+).*/,'$1');
    isSafari = buildBrowserChecker(version);
}else{
    //UNKNOW 
}
