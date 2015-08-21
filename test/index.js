var http = require('http');
var ScriptLoader = require('../lib/js-loader.js').ScriptLoader;
var loaderMap = {};
var writeFile = require('./server-file').writeFile

function startServer(root,port){
	root = root || require('path').resolve('./');
	http.createServer(function (req, res) {
		var url = req.url.replace(/[?#].*$/,'');
		if(url.match('\.js$')){
			var path = url.replace(/^\/(?:static|assets|scripts?)(?:\/js)?\//,'/');
			var base = root + url.slice(0,1-path.length)
			var loader = loaderMap[base];
			if(!loader){
				loader = loaderMap[base] = new ScriptLoader(base);
			}
			console.log('start:'+url)
			loader.load(path,function(content){
				setTimeout(function(){
					res.writeHead(200, {'Content-Type': 'text/javascript;charset=utf-8'});
					res.end(content+'');
					console.log('\tend:'+url)
				},Math.random()*1000);
			})
			return true;
		}else{
			writeFile(root,req,res)
		}
	}).listen(port || 8080);
	
	console.log('test web started on http://localhost:8080/',
		'\n test module xmldom && lite template engine:',
		'\n  1. npm install xmldom',
		'\n  2. npm install lite',
		'\n open http://localhost:8080/test/index.html');
}
var argv = process.execArgv;
if(argv[0] == '-e' && /^\s*require\(['"]jsi[\/\\]test(?:[\/\\]index)?['"]\);?\s*$/.test(argv[1])){
	startServer();
}else if(process.argv[1] == __filename){
	startServer();
}
exports.start = startServer;
