exports.compressJS = compressJS;
exports.formatJS = formatJS;

try{
	var UglifyJS = require("uglify-js");
}catch(e){
	try{
		var UglifyJS = require("uglifyjs");
	}catch(e){
	}
}

var tokenCompress = require('./js-token').compressJS
function formatJS(js){
	if(UglifyJS){
		var ast = UglifyJS.parse(js);
		ast.figure_out_scope();
		return ast.print_to_string({beautify:true}); 
	}
	return String(new Function(js)).replace(/^[^{]+\{([\s\S]*)\}/,'$1')
}
var uncompressed = /^[\t ]{2}|^\s*\/\/|\/\**[\r\n]/m;
var compressedCache = []
function compressJS(source,file,root){
	source = source.replace(/^\/\*[\s\S]*?\*\/\s*/g,'')
	var i = compressedCache.indexOf(source);
	if(i>=0){
		if(i%2 == 0){
			var kv = compressedCache.splice(i,2)
			compressedCache.push(kv[0],kv[1]);
			return kv[1];
		}else{
			return source;
		}
	};
	var result = source;
	var sample = source.slice(source.length/10,source.length/1.1);
	//console.log(sample.length )
	if(sample.length < 200 || sample.match(uncompressed)){

		if(UglifyJS){
			result = uglifyJSCompress(source,file);
		}else{
			try{
				result= tokenCompress(source,file,root);
			}catch(e){
				result= tokenCompress('this.x='+source,file,root).replace(/^this.x=|(});$/g,'$1') ;
			}
		}
	}
	compressedCache.push(source,result);
	if(compressedCache.length>64){
		compressedCache.shift();
		compressedCache.shift();
	}
	return result;
}

function tokenCompress(source,file,root){
	source = String(source).replace(/\r\n?/g,'\n');
	if(source.search(uncompressed)<0){
		return source;
	}
	var ps = partitionJavaScript(source,file,root);
	var result = [];
	for(var i =0;i<ps.length;i++){
		var item = ps[i];
		switch(item.charAt()){
		case '\'':
		case '\"':
		case '`':
			result.push(item);//string
			break;
		case '/':
			//skip comment && reserve condition comment and regexp
			var stat = item.match(/^\/(?:(\*\s*@)|\/|\*)/);
			if(!stat || stat[1]){
				result.push(item);//regexp or condition comment
			}
			break;
			
		default:
			//result.push(item.replace(/^[ \t]+/gm,''));//被切开的语法块，前置换行，可能上上一个语法的结束语法，不能删除
			//console.log('%%<',item.replace(/^(\r\n?|\n)+|(\s)+/gm,'$1$2'),">%%")
			result.push(item.replace(/(\n)+|([^\S\r\n])+/gm,'$1$2').replace(/(?:([\r\n])+\s*)+/g,'$1'));
		}
	}
	return result.join('');
}
function uglifyJSCompress(source,id,root){
	var old_warn = UglifyJS.AST_Node.warn_function
	var logs = [];
	UglifyJS.AST_Node.warn_function = function(args){
		logs.push(args);
	}
	var result = UglifyJS.minify(String(source), {fromString: true});
	var code = result.code;
	
	UglifyJS.AST_Node.warn_function = old_warn;
	if(!code){
		throw new Error('invalid compress result!\n'+logs+'\n\n'+source)
	}
	if(logs.length){
		console.warn('compress warn '+id,"\n"+logs.join('\n'))
	}
	return code;
}