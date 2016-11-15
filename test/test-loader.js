var ScriptLoader = require('../lib/js-loader.js').ScriptLoader;
var loader = new ScriptLoader('../');
loader.loadFile('test/deps.js',function(text,path,file){
	//console.log(text,path,file)
})
loader.loadFile('boot.js',function(text,path,file){
	//console.log(text,path,file)
})


//console.log(module)