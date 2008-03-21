/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: commons.js,v 1.2 2008/02/19 13:30:13 jindw Exp $
 */

//常用方法
/**
 * 生成页面范围内唯一id.
 * (Pagescope Unique Identifier)
 * @public
 * @return <String> puid
 */
var puid = 1;
//compute scriptBase
var rootMatcher = /(^\w+:((\/\/\/\w\:)|(\/\/[^\/]*))?)/;
var homeFormater = /(^\w+:\/\/[^\/#\?]*$)/;
//var homeFormater = /^\w+:\/\/[^\/#\?]*$/;
var urlTrimer = /[#\?].*$/;
var dirTrimer = /[^\/\\]*([#\?].*)?$/;
var forwardTrimer = /[^\/]+\/\.\.\//;
var base = window.location.href.
    replace(homeFormater,"$1/").
    replace(dirTrimer,"");
//处理HTML BASE 标记
function computeURL(url){
  var purl = url.replace(urlTrimer,'').replace(/\\/g,'/');
  var surl = url.substr(purl.length);
  if(rootMatcher.test(purl)){
    return purl + surl;
  }else if(purl.charAt(0) == '/'){
    return rootMatcher.exec(base)[0]+purl + surl;
  }
  purl = base + purl;
  while(purl.length >(purl = purl.replace(forwardTrimer,'')).length){
    //alert(purl)
  }
  return purl + surl;
}
var bases = document.getElementsByTagName("base");
if(bases){
  for(var i=bases.length-1;i>=0;i--){
    var href = bases[i].href;
    if(href){
      base = computeURL(href.replace(homeFormater,"$1/").replace(dirTrimer,""));
      break;
    }
  }
}


CommonUtil = {
  puid : function(){
    return "$puid$_";+(puid++);
  },
  /**
   */
  computeURL: computeURL,
  /**
   * Utility to set up the prototype, constructor properties to
   * support an inheritance strategy that can chain constructors and methods.
   *
   * @public 
   * @member CommonUtil
   * @static
   * @param {Function} subclass   the object to modify
   * @param {Function} superclass the object to inherit
   */
  extend:function(subclass, superclass) {
    function f(){};
    f.prototype = superclass.prototype;
    var npt = new f();
    npt.constructor = subclass;
    var opt = subclass.prototype;
    for(var n in opt){
      npt[n] = opt[n];
    }
    subclass.prototype = npt;
  },
  /**
   * 绑定this变量和参数列表
   * @public
   * @arguments 第一个参数为method,需要绑定的函数
   * @arguments 第二个参数为thisArg
   * @arguments 3..n 其他参数为原函数绑定的参数
   * @return 绑定的函数
   */
  bind: function(method){
    var args = Array.prototype.slice.call(arguments,1);
    var object = args.shift();
    return function() {
      return method.apply(object, args);
    }
  },
  /**
   * 判断数组中是否包含某元素
   * @public
   * @param arg 
   * @see java.util.Collection#contains(Object arg);
   */
  contains : function(array,arg) {
    for(var i = array.length-1;i>=0;i--){
      if(array[i] == arg){
        return true;
      }
    }
    return false;
  },
  /**
   * @public
   * @see java.lang.String#trim()
   * @return 去除前后空格的新字符串
   */
  trim : function(source){
    return source.replace(/^\s*|\s*$/g,'');
  },
  /**
   * @public
   * @see java.lang.String#equalsIgnoreCase()
   * @return boolean 忽视大小写后是否等价
   */
  equalsIgnoreCase : function(source,target){
    //TODO:有待优化
    return source == target || (source!=null && target!=null)
    if(target!=null && target.length == this.length && target.toLowerCase){
      return source.toLowerCase() == target.toLowerCase();
    }else{
      return false;
    }
  }
}
