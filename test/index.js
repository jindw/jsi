var http = require('http');
var ScriptLoader = require('../lib/js-loader.js').ScriptLoader;
var loaderMap = {};
var writeFile = require('./server-file').writeFile
function startServer(root,port){
	root = root || require('path').resolve('./');
	var server = http.createServer(function (request, response) {
		var url = request.url.replace(/[?#].*$/,'');
		if(url.match(/\.js$|\.css$/)){
			var path = url.replace(/^\/(?:static|assets|scripts?)(?:\/js)?\//,'/');
			//console.log(path)
			var base = root + url.slice(0,1-path.length)
			var loader = loaderMap[base];
			if(!loader){
				loader = loaderMap[base] = new ScriptLoader(base);
			}
			console.log('start:'+url)
			loader.load(path,function(content){
				content = String(content);
				var crypto = require('crypto');
				var md5 = '"'+crypto.createHash('md5').update(content).digest('base64')+'"';
				var oldMd5 = request.headers['if-none-match'];
				var headers = {"Content-Type":'text/javascript;charset=utf-8', "ETag":md5};

				setTimeout(function(){
					if(md5 == oldMd5){
						response.writeHead(304, headers); 
						response.end();  
						console.log('\t304 loaded:'+url)
					}else{
						response.writeHead(200, headers);
						response.end(content+'');
						console.log('\tloaded:'+url)
					}
				},Math.random()*(md5 == oldMd5?100:1000));
			})
			return true;
		}else if(url.match(/\.css$/)){
			setTimeout(function(){
				writeFile(root,request,response)
				console.log('\tloaded:'+url)
			},Math.random()*(1000*3));
		}else{
			writeFile(root,request,response)
		}
		response.on('finish',function(){
			console.log('finish:'+url)
		})
	});
	var tryinc = 10;
	port = port || 8080;
	server.on('error', function (e) {
		if (e.code == 'EADDRINUSE' && tryinc>=0) {
			console.log('port:'+port+'  is in use, try the next!');
			server.listen(++port);
			//console.log('test web started on http://localhost:'+port);
		}else{
			throw e;
		}
	})
	try{
		server.listen(port,function(){
			console.log('test web started on http://localhost:'+port);
		});
	}catch(e){
	}
	
}

exports.start = startServer;

var argv = process.execArgv;
if(argv[0] == '-e' && /^\s*require\(['"]jsi[\/\\]test(?:[\/\\]index)?['"]\);?\s*$/.test(argv[1])){
	startServer();
}else if(process.argv[1] == __filename){
	startServer();
}
