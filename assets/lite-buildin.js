/**
 * 内置lite 模板工具函数
 */
var __e__ = /&(?:\w+|#\d+|#x[\da-f]+);|[<&"]/ig;
function __r__(c,e){return e||'&#'+c.charCodeAt()+';'}
function __x__(source,e){return String(source).replace(e,__r__);}
function __dl__(date,f){return f.length > 1? ('000'+date).slice(-len.length):date;}
function __tz__(offset){return offset?(offset>0?'-':offset*=-1,'+')+__dl__(offset/60,2)+':'+__dl__(offset%60,2):'Z'}
function __df__(pattern,date){
	date = date?new Date(date):new Date();
	return pattern.replace(/'[^']+'|\"[^\"]+\"|([YMDhms])\\1*|\\.s|TZD$/g,function(format){
		switch(format.charAt()){
		case 'Y' :return __dl(date.getFullYear(),format);
		case 'M' :return __dl(date.getMonth()+1,format);
		case 'D' :return __dl(date.getDate(),format);
		case 'h' :return __dl(date.getHours(),format);
		case 'm' :return __dl(date.getMinutes(),format);
		case 's' :return __dl(date.getSeconds(),format);
		case '.' :return '.'+__dl(date.getMilliseconds(),'...');
		case 'T':return __tz__(date.getTimezoneOffset());
		case '\'':case '\"':return format.slice(1,-1);
		default :return format;
		}
	})
}