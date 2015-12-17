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
			var jsExportAs = request.url.match(/\.js\?export=(raw|ui)/);
			if(jsExportAs){
				var mode = request.url.replace(/.*&mode=(\w+).*|.*/,'$1') || 1;
				var format = request.url.replace(/.*&format=(\w+).*/,'$1').match(/^(true|on|1)$/);
				//console.log(JSON.stringify(type))
				path = path.replace(/^\//,'')
				require('../lib/exports').exportSingleFile(base,path,mode,function(source,externals){
					if(jsExportAs[1] != 'ui'){
						writeContent(request,response,source,'text/javascript;charset=utf-8');
						return;
					}
					
					var cmd = "$ cd "+base+"\n$ jsi export -o tmp.js -mode "+mode+" "+path
					var header = "<form style='text-align:center'><input name='export' value='ui' type='hidden'>" +
							"<label title='该文件在全局域运行，变量全部是全局变量。&#32; 依赖模块可用 require 函数获得。'>" +
							"	<input type=radio name=mode value=1 "+(mode==1?'checked':'')+" onclick='this.form.submit()' autocomplete='off'>" +
							"全导出</label>" +
							"<label title='不自动运行，需要require，可用 require 函数获得任意模块, 该方式不自动合并其他模块的内容（只合并模块内文件）。'>" +
							"	<input type=radio name=mode value=2 "+(mode==2?'checked':'')+" onclick='this.form.submit()' autocomplete='off'>" +
							"库导出</label> " +
							//"<label title='当前文件在全局域执行，子模块作为匿名闭包载入，不能再用require(module)方式获得'>" +
							//"	<input type=radio name=mode value=3 "+(mode==3?'checked':'')+" onclick='this.form.submit()' autocomplete='off'>" +
							//"匿名导出</label>" +
							"&#160;&#160;&#160;" +
							"<label>格式化代码" +
							"<input type='checkbox' name='format' value='true' "+(format &&'checked')+" onclick='this.form.submit()' autocomplete='off'>"+
							"</label>" +
							"<input style='margin-left:40px' type='button' value='查看原始格式' onclick='this.form.export.value=\"raw\";this.form.submit()'/><hr>" +
							"<code><pre>"+cmd+"</pre></code>"+
							"</form>"
					if(format){
						source = formatJS(source)
					}
					if(externals instanceof Array && externals.length){
						header += '<p><pre style="background:yellow;color:red;padding-left:24px"><strong>使用时别忘了和未导入的外部依赖配合工作：</strong><br>'+externals.join('\n').replace(/.+/g,'require("$&")')+'</pre></p>'
					}
					writeSource(request,response,root+url,header,source);
				})
			}else {
				//console.log('preload!',path)
				loader.load(path,function(content){
					var cookie = request.headers.cookie || '';
					var debug = cookie.replace(/^.*\bJSI_DEBUG=(\w+).*$/,'$1')
					//console.log('afterload:',debug,path)
					if(url.match(/\.js$/) && !url.match(/\/o\//) && /^(false|0)$/.test(debug)){
						content = compressJS(content,path);
					}
					writeContent(request,response,content,'text/'+(url.match(/\.css$/)?'css':'javascript')+';charset=utf-8');
				})
				return true;
			}
		}else if(url.match(/\.css$/)){
			setTimeout(function(){
				writeFile(root,request,response)
				console.log('\tloaded:'+url)
			},Math.random()*(1000*3));
		}else if(request.url.match(/\.html\?optimized=merge$/)){
			var exportHTML = require('../lib/export-html').exportHTML;
			exportHTML(root,url,function(content){
				writeContent(request,response,content,"text/html;charset=utf-8");
				console.log('\tloaded:'+url)
			})
		}else if(request.url.match(/^\/\?proxy=/)){
			doProxy(request,response,decodeURIComponent(request.url.replace(/^.*=/,'')));
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
