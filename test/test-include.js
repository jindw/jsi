var IncludeBuilder = require('../lib/include.js').IncludeBuilder;
var ib = new IncludeBuilder("#include 'lite-buildin.js'",function(text,file){
	console.log('result:',text,file)
},'test.js');