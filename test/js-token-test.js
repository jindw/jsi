var jstoken = require('lite/parse/js-token');
function s1(){
	return a+b  /c/2;
}
function s7(){
	a = /a/i+b;
	a = /a/i+'a'
}
function s8(){
	a = /a/i+b;
	a =
/*d*//a/i+'a'
}
var partitionJavaScript = jstoken.partitionJavaScript;
function assertLength(source, length, changed){
	source = String(source).replace(/\r\n?/g,'\n');
	var result = partitionJavaScript(source);
	var s = source;
	var d = result.join('');
	
	//console.log(d)
	//console.log(s)
	console.assert(result.length==length,[result.length,length,'\n']+result);
	console.assert(changed ^ (s.replace(/\s/g,'') == d.replace(/\s/g,'')),d)
}
assertLength(s1,1)
assertLength(s7,7)
assertLength(s8,8)