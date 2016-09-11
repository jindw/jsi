exports.ScriptLoader = require('../lib/js-loader').ScriptLoader;
//exports.compressJS = require('../lib/js-token').compressJS;
//exports.format = 'raw';//raw;compressed;format
var argv = process.execArgv;
if(argv[0] == '-e' && /^\s*require\(['"]jsi['"]\);?\s*$/.test(argv[1])){
	//start server
	require('../test').start();
}
