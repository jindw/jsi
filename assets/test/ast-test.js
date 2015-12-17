var UglifyJS = require('uglify-js');
var TreeWalker = UglifyJS.TreeWalker;
var defaultGlobals = "document,window,arguments,setTimeout,clearTimeout,setInterval,clearInterval," +
		"Math,JSON,Number,Date".split(/[^\w]+/);
		
var code = String(function(){
	function aaa(xxx,yyy){
		return xxx - yyy+aaa;
	}
	function bbb(xxx2,yyy2){
		return xxx2 - yyy2+aaa;
	}
	
	
}).replace(/^[^{]+\{([\S\s]+)\}\s*$/,'$1')
var topScope = UglifyJS.parse(code);
topScope.figure_out_scope();
//console.log(require('util').inspect(topScope,true))
// this is the transformer
topScope.transform(new UglifyJS.TreeTransformer(null,function(node){
	//console.log('transform',node.TYPE);
	//descend(node,this);
	return node;
}));
//require 变量记数，如果使用者不全是 property， 就不能处理
topScope.walk(new TreeWalker(function(node, descend){
	if(node instanceof UglifyJS.AST_SymbolRef){
		
	}
	console.log(node.TYPE);//,node,'\n-----\n')
}));
topScope.mangle_names();
console.log(topScope.print_to_string())

