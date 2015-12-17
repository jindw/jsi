var fs = require('fs')
var content = fs.readFileSync(require.resolve('./test.js'))+'';
//console.log(content+'')

var UglifyJS = require('uglifyjs');

	var compressor = UglifyJS.Compressor();
	var compressed = UglifyJS.parse(content,{filename:'.js'});
	compressed.figure_out_scope();
	var compressed = compressed.transform(compressor);