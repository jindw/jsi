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

var tokenCompress = require('lite/parse/js-token').compressJS
function formatJS(js){
	if(UglifyJS){
		var ast = UglifyJS.parse(js);
		ast.figure_out_scope();
		//compressor = UglifyJS.Compressor({});
		//ast = ast.transform(compressor);
		return ast.print_to_string({beautify:true}); 
	}
	return String(new Function(js)).replace(/^[^{]+\{([\s\S]*)\}/,'$1')
}

function compressJS(source,id,root){
	if(!UglifyJS){
		return tokenCompress(source,id,root)
	}
	var old_warn = UglifyJS.AST_Node.warn_function
	var logs = [];
	UglifyJS.AST_Node.warn_function = function(args){
		logs.push(args);
	}
	/*
	
	var ast = UglifyJS.parse(source);
	ast.figure_out_scope();
	compressor = UglifyJS.Compressor({});
	ast = ast.transform(compressor);
	var code = ast.print_to_string(); // get compressed code */
	
	var result = UglifyJS.minify(String(source), {fromString: true});
	var code = result.code;
	
	UglifyJS.AST_Node.warn_function = old_warn;
	if(!code){
		//console.dir(result)
		//console.log(source)
		throw new Error('invalid compress result!\n'+logs+'\n\n'+source)
	}
	if(logs.length){
		console.warn('compress warn '+id,"\n"+logs.join('\n'))
	}
	//console.log(source,'!=',code)
	return code;
}