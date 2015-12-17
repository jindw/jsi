a = [function(exports,require){/**
 * 内置lite 模板工具函数
 */
function __x__(source,exp){//not ignoreQute?
//(?!\w+;)
	return String(source).replace(exp||/&(?!#\d+;|#x[\da-f]+;|[a-z]+;)|[<"]/ig,function(c){
		return '&#'+c.charCodeAt()+';';
	});
}
function __df__(pattern,date){
	function __dl__(date,f){return f.length > 1? ('000'+date).slice(-len.length):date;}
	function __tz__(offset){return offset?(offset>0?'-':offset*=-1,'+')+__dl__(offset/60,2)+':'+__dl__(offset%60,2):'Z'}
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
}
]