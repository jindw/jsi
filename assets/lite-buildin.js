/**
 * 内置lite 模板工具函数
 */
function __x__(source,exp){//not ignoreQute?
	return String(source).replace(exp||/&(?!#\d+;|#x[\da-f]+;|[a-z]+;)|[<"]/ig,function(c){
		return '&#'+c.charCodeAt()+';';
	});
}
/*
var  __x__ = function(defExp){
	function r(c){return '&#'+c.charCodeAt()+';';}
	function dl(date,f){return f.length > 1? ('000'+date).slice(-f.length):date;}
	function tz(offset){return offset?(offset>0?'-':offset*=-1,'+')+dl(offset/60,'00')+':'+dl(offset%60,'00'):'Z'}
	return function(source,exp){
		if(source instanceof Date){
			return exp.replace(/([YMDhms])\1*|\.s|TZD$|'[^']+'|"[^"]+"/g,function(format){
				switch(format.charAt()){
				case 'Y' :return dl(date.getFullYear(),format);
				case 'M' :return dl(date.getMonth()+1,format);
				case 'D' :return dl(date.getDate(),format);
				case 'h' :return dl(date.getHours(),format);
				case 'm' :return dl(date.getMinutes(),format);
				case 's' :return dl(date.getSeconds(),format);
				case '.' :return '.'+dl(date.getMilliseconds(),'000');
				case 'T':return tz(date.getTimezoneOffset());
				case '\'':case '\"':return format.slice(1,-1);
				default :return format;
				}
			})
		}else{
			return String(source).replace(exp||/&(?!#\d+;|#x[\da-f]+;|[a-z]+;)|[<"]/ig,r);
		}
	}
}();*/
//__x__(date,pattern)
function __df__(pattern,date){
	function dl(date,f){return f.length > 1? ('000'+date).slice(-f.length):date;}
	function tz(offset){return offset?(offset>0?'-':offset*=-1,'+')+dl(offset/60,'00')+':'+dl(offset%60,'00'):'Z'}
	date = new Date(date);
	return pattern.replace(/([YMDhms])\1*|\.s|TZD$|'[^']+'|"[^"]+"/g,function(format){
		switch(format.charAt()){
		case 'Y' :return dl(date.getFullYear(),format);
		case 'M' :return dl(date.getMonth()+1,format);
		case 'D' :return dl(date.getDate(),format);
		case 'h' :return dl(date.getHours(),format);
		case 'm' :return dl(date.getMinutes(),format);
		case 's' :return dl(date.getSeconds(),format);
		case '.' :return '.'+dl(date.getMilliseconds(),'000');
		case 'T':return tz(date.getTimezoneOffset());
		case '\'':case '\"':return format.slice(1,-1);
		default :return format;
		}
	})
}
if(!Object.keys){
	Object.keys = function(o){
		var result = [];
		for (p in o) {
			if (o.hasOwnProperty(p)) {
				result.push(prop);
			}
		}
		return result;
	}
}