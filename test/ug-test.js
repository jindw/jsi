var fs = require('fs')
var exportRequireExample = fs.readFileSync(require.resolve('./ug-example.js')).toString();


try{
	var UglifyJS = require("uglify-js");
}catch(e){
	try{
		var UglifyJS = require("uglifyjs");
	}catch(e){
	}
}

if(UglifyJS){
	//var compressJS0 = compressJS;
	compressJS = function(source){
		var ast = UglifyJS.parse(source);
		ast.figure_out_scope();
		compressor = UglifyJS.Compressor({});
		ast = ast.transform(compressor);
		return ast.print_to_string({beautify:true},true); 
	}
}
console.log(compressJS(exportRequireExample))