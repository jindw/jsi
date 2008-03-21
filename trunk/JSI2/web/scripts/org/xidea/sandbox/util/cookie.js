/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: cookie.js,v 1.2 2008/02/19 13:30:13 jindw Exp $
 */

function Cookie(path,expires,domain,secure)
{
  this.path = path;
  this.expires = expires;
  this.domain = domain;
  this.secure = secure;
}

Cookie.prototype.getSubfix = function (){
}
/**
 * Sets cookie
 * @param {String} name the name of cookie item
 * @param {String} value the value of cookie item
 */
Cookie.setValue = function (name,value,path,expires,domain,secure)
{
  document.cookie = encodeURIComponent(name) + "=" + encodeURIComponent(value) 
    + this.getSubfix()+";"
    + (domain ? "; domain=" + domain :"")
	+ (path ? "; path=" + path : "" )
	+ (expires ? "; expires=" + expires.toGMTString()
		: "" )
	+ (secure?"; secure":'');
}
/**
 * Sets cookie
 * @param {String} name the name of cookie item
 * @param {String} value the value of cookie item
 */
Cookie.prototype.setValue = function (name,value,path,expires,domain,secure)
{
  Cookie.setValue(name,value,path || this.path,expires || this.expires,domain || this.domain,secure || this.secure);
}
/**
 * Returns cookie value with name.
 * @return  a string
 * @type String
 */
Cookie.getValue = Cookie.prototype.getValue = function (name){
  name = encodeURIComponent(name);
  name = new RegExp("^.*[^| ;]" + name + "=([^;]*).*");
  name = document.cookie.replace(name,'$1');
  return name?decodeURIComponent(name): null;
}
