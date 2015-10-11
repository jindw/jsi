var http = require('http');
var ScriptLoader = require('../lib/js-loader.js').ScriptLoader;
var loaderMap = {};
var writeFile = require('./server-file').writeFile
var writeSource = require('./server-file').writeSource;
var compressJS = require('../lib/js-token').compressJS
var formatJS = require('../lib/js-token').formatJS
function getLoader(base){
	var loader = loaderMap[base];
	if(!loader){
		loader = loaderMap[base] = new ScriptLoader(base);
	}
	return loader;
}
function startServer(root,port){
	root = root || require('path').resolve('./');
	var mapping = [];
	var mappingPath = root.replace(/[\\\/]?$/,'/mapping.js');
	try{
		var mappingSource = require('fs').readFileSync(mappingPath);
		//var routes = require(root.replace(/[\\\/]?$/,'/mapping.js')).routes;
	}catch(e){
		console.log('mapping file not found!!',mappingPath)
	}
	if(mappingSource){
		//routes = require(mappingPath).routes || [];
		try{
			mapping = new Function('var exports = {};'+mappingSource+";\nreturn exports.mapping||exports.routes;").call()||[]
		}catch(e){
			console.log('invalid mapping file!!')
		}
	}
	var server = http.createServer(function (request, response) {
		var url = request.url.replace(/[?#].*$/,'');
		//console.log('start:'+url);
		response.on('finish',function(){
			//console.log('finish:'+url)
		})
		
		if(url.match(/\.js$|\.css$/)){
			var path = url.replace(/^\/(?:static|assets|scripts?)(?:\/js)?\//,'/');
			var base = root + url.slice(0,1-path.length)
			var loader = getLoader(base);
			if(request.url.match(/\.js\?export=ui/)){
				var type = request.url.replace(/.*&type=(\w+).*/,'$1') || 1;
				var format = request.url.replace(/.*&format=(\w+).*/,'$1').match(/^(true|on|1)$/);
				//console.log(format)
				path = path.replace(/^\//,'')
				require('../lib/exports').exportSingleFile(base,path,type,function(source){
					var cmd = "$ cd "+base+"\n$ jsi export -o tmp.js -type "+type+" "+path
					var header = "<form style='text-align:center'><input name='export' value='ui' type='hidden'>" +
							"<label title='可以用 require 函数获得任意依赖的模块，但是当前文件自身直接在全局域执行，当前模块的变量全部是全局变量。'>" +
							"	<input type=radio name=type value=1 "+(type==1?'checked':'')+" onclick='this.form.submit()' autocomplete='off'>" +
							"依赖兼容导出</label>" +
							"<label title='可以用 require 函数获得任意模块'>" +
							"	<input type=radio name=type value=2 "+(type==2?'checked':'')+" onclick='this.form.submit()' autocomplete='off'>" +
							"全兼容导出</label> " +
							"<label title='当前文件在全局域执行，子模块作为匿名闭包载入，不能再用require(module)方式获得'>" +
							"	<input type=radio name=type value=3 "+(type==3?'checked':'')+" onclick='this.form.submit()' autocomplete='off'>" +
							"匿名导出</label>" +
							"&#160;&#160;&#160;" +
							"<label>格式化代码" +
							"<input type='checkbox' name='format' value='true' "+(format &&'checked')+" onclick='this.form.submit()' autocomplete='off'>"+
							"</label><hr>" +
							"<code><pre>"+cmd+"</pre></code>"+
							"</form>"
					if(format){
						source = formatJS(source)
					}
					writeSource(request,response,root+url,header,source);
				})
			}else {
				loader.load(path,function(content){
					var cookie = request.headers.cookie || '';
					var debug = cookie.replace(/^.*\bJSI_DEBUG=(\w+).*$/,'$1')
					//console.log(debug)
					if(url.match(/\.js$/) && !url.match(/\/o\//) && /^(false|0)$/.test(debug)){
						content = compressJS(content);
					}
					writeContent(request,response,content,'text/'+(url.match(/\.css$/)?'css':'javascript')+';charset=utf-8');
				})
				return true;
			}
		}else if(url.match(/\.(css)$/)){
			setTimeout(function(){
				writeFile(root,request,response)
				console.log('\tloaded:'+url)
			},Math.random()*(1000*3));
		}else{
			//console.log(routes)
			for(var i =0;i<mapping.length;i++){
				var route = mapping[i];
				var pattern = route.path;
				var match = false;
				if(pattern instanceof RegExp){
					match = url.match(pattern);
				}else{
					match = url == pattern && [];
				}
				//console.log(url,pattern,match)
				if(match){
					if(route.action){
						match.unshift(request,response);
						if(route.action.apply(this,match)){
							return;
						}
					}
					if(route.file){
						writeFile(root,request,response,route.file)
					}else if(route.remote){
						var remote = route.remote;
						if(pattern instanceof RegExp && remote.indexOf('$')>=0){
							remote = url.replace(pattern,remote.replace('$0','$&'));
						}
						//console.log(route.remote,route.remote.match(/^https?\:/))
						if(!remote.match(/^https?\:/)){
							var url = require('path').resolve(url.replace(/(https?\:\/\/[^\/]+)?[^\/\\]*$/,''),remote);
							//route.remote = 'http://'+ request.headers.host+url;
							request.url = url;
							//console.log(route.remote,url)
							arguments.callee.apply(this,arguments);
							return;
								
						}
						
						doProxy(request,response,remote)
					}else if(route.data){
						writeContent(request,response,JSON.stringify(route.data));
					}else{
						writeContent("<h3>action || file || remote || data is requied!</h3>")
					}
					return;
				}
			}
			writeFile(root,request,response)
		}
	});
	var tryinc = 10;
	port = port || 8080;
	server.on('error', function (e) {
		if (e.code == 'EADDRINUSE' && tryinc>=0) {
			console.log('port:'+port+'	is in use, try the next!');
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
function writeContent(request,response,content,contentType){
	content = String(content);
	var url = request.url;
	var crypto = require('crypto');
	var md5 = '"'+crypto.createHash('md5').update(content).digest('base64')+'"';
	var oldMd5 = request.headers['if-none-match'];
	if(contentType == null){
		if(/^\s*[\[\{][\s\S]*[\[\}]\s*$/.test(content)){
			contentType = 'text/json;charset=utf-8';
		}else	if(/^\s*<[\s\S]*>\s*$/.test(content)){
			contentType = 'text/html;charset=utf-8';
		}
	}
	var headers = {"Content-Type":contentType, "ETag":md5};
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
}

function doProxy(request,response,proxyPath){
	var path = request.url;
	var match = path.match(/^([^?]*?.*)(\?|\?.*&)__proxy__=([^&]+)(.*)$/);
	//console.log(proxyPath,match)
	if(proxyPath || match){
		path = proxyPath ? path : match[1]+match[2]+match[4];
		var realpath = proxyPath || match[3];
		match = realpath.match(/^http\:\/\/([^\\\/:]+)(\:\d+)?(.*)$/) ;
		if(match){
			var host = match[1];
			var port = match[2]||80;
			var path = match[3] ;
			//var hostname = 
			request.headers.host = host;
			var options = {
				port:port,host:host,
				method:request.method, path:path, headers:request.headers
			}
			//console.log(options)
			var proxy_request = http.request(options);
			proxy_request.addListener('response', function (proxy_response) {
				proxy_response.addListener('data', function(chunk) {
					response.write(chunk, 'binary');
				});
				proxy_response.addListener('end', function() {
					response.end();
				});
				response.writeHead(proxy_response.statusCode, proxy_response.headers);
			});
			request.addListener('data', function(chunk) {
				proxy_request.write(chunk, 'binary');
			});
			if(request.method == 'POST'){
				request.addListener('end', function() {
					proxy_request.end();
				});
			}else{
				proxy_request.end();
			}
			return true;
		}
	}
	return false;
}



exports.start = startServer;

var argv = process.execArgv;
if(argv[0] == '-e' && /^\s*require\(['"]jsi[\/\\]test(?:[\/\\]index)?['"]\);?\s*$/.test(argv[1])){
	startServer();
}else if(process.argv[1] == __filename){
	startServer();
}
