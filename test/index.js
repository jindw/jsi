var http = require('http');
var https = require('https');
var ScriptLoader = require('../lib/js-loader.js').ScriptLoader;
var loaderMap = {};
var writeFile = require('./server-file').writeFile
var writeSource = require('./server-file').writeSource;
var compressJS = require('../lib/js-process').compressJS
var maxTimeout = 0;//1000;
function getLoader(base,md){

	var loader = loaderMap[base];
	if(!loader){
		var mds = md && md.split(/[,;]/);
		console.log('getLoader:',base)
		loader = loaderMap[base] = new ScriptLoader(base,{md:mds});
	}
	return loader;
}
const printRequestLog = false;

function buildGenForm(mode,format,rawurl){
	return "<form style='text-align:center'><input name='export' value='ui' type='hidden'>" +
	"<label title='该文件在全局域运行，变量全部是全局变量。&#32; 依赖模块可用 require 函数获得。'>" +
	"	<input type=radio name=mode value=1 "+(mode==1?'checked':'')+
	" 		onclick='this.form.submit()' autocomplete='off'>" +
	"全导出</label>" +
	"<label title='不自动运行，需要require，可用 require 函数获得任意模块, 该方式不自动合并其他模块的内容（只合并模块内文件）。'>" +
	"	<input type=radio name=mode value=2 "+(mode==2?'checked':'')+
		" onclick='this.form.submit()' autocomplete='off'>" +
	"库导出</label> " +
	"&#160;&#160;&#160;" +
	"<label>格式化代码" +
	"<input type='checkbox' name='format' value='format' "+(format &&'checked')+" onclick='this.form.submit()' autocomplete='off'>"+
	"</label>" +
	//"<input style='margin-left:40px' type='button' value='查看原始格式' onclick=\"this.form.export.value='raw';this.form.submit()\"/>" +
	"<input style='margin-left:40px' type='button' value='查看原始格式' onclick=\"location='"+rawurl+"'\"/>" +
	"<hr></form>"
}
function startServer(opt){
	var root = opt.root || opt.r || require('path').resolve('./');
	var port = opt.port || opt.p || 8080;
	var md = opt.md;
	var mapping = [];
 	var mappingName = opt.mapping || opt.m || 'mapping.js';
 	var mappingPath = root.replace(/[\\\/]?$/,'')+'/'+mappingName;
	try{
		var mappingSource = require('fs').readFileSync(mappingPath);
		//var routes = require(root.replace(/[\\\/]?$/,'/mapping.js')).routes;
	}catch(e){
		//console.log('mapping file not found!!',mappingPath)
	}
	if(mappingSource){
		//routes = require(mappingPath).routes || [];
		try{
			mapping = new Function('require','var exports = {};'+mappingSource+";\nreturn exports.mapping||exports.routes;").call(0,require)||[]
		}catch(e){
			console.log('invalid mapping file!!',e)
		}
	}
	var server = http.createServer(function (request, response) {
		var url = request.url.replace(/[?#].*$/,'');
		if(printRequestLog)console.log('start:'+url,request.headers,request.socket.remoteAddress);

		response.on('finish',function(){
			if(printRequestLog)console.log('finish:'+url)
		})
		
		//if(url.match(/^\/-shorter.js/)){}else 
		if(require('fs').existsSync(root+url)){
			setTimeout(function(){
				writeFile(root,request,response)
				if(printRequestLog)console.log('\tloaded:'+url)
			},Math.random()*(maxTimeout*3));
		}else if(url.match(/\.(css|js)$/)){

			var path = url.replace(/^\/(?:static|assets|scripts?)(?:\/js)?\//,'/');
			var base = root + url.slice(0,1-path.length)
			//console.log(base)
			var loader = getLoader(base,md);
			var mode = request.url.replace(/(?:.*?[&?]mode=(\w+))?.*/,'$1') || 1;
			var format = request.url.replace(/(?:.*?[&?]format=(\w+))?.*/,'$1')||'raw';
			
			//console.log(format)
			//var jsRaw = request.url.match(/__export__.js/)
			var jsExportAs = request.url.match(/\.js\?export=(raw|ui)/);
			path = path.replace(/^\//,'')
			if(jsExportAs){
				//console.log(JSON.stringify(type))
				path = (mode == 1?path:path.replace(/\.js$/i,''));
				require('../lib/exports').exportScript(base,[path],onComplete,format);
				function onComplete(source,externals){
					if(jsExportAs[1] == 'ui'){
						var cmd = "$ cd "+base+"\n$ jsi export -o tmp.js -f "+format+' '+path;
						var rawurl = url.replace(/\.js$/,'__export__.js')+'?format='+format
						if(mode == 2){
							rawurl+='&mode=2';
						}
						var header = buildGenForm(mode,format == 'format',rawurl);
						header+='<code><pre style="margin:12px">'+cmd+'</pre></code><p><pre style="background:#ddd;color:#339;padding-left:24px">size:'+source.length+'</pre></p>'
						if(externals instanceof Array && externals.length){
							header += '<p><pre style="background:yellow;color:red;padding-left:24px"><strong>使用时别忘了和未导入的外部依赖配合工作：</strong><br>'+externals.join('\n').replace(/.+/g,'require("$&")')+'</pre></p>'
						}
						writeSource(request,response,root+url,header,source);
					}else{
						writeContent(request,response,source,'text/javascript;charset=utf-8');
					}
				}
			}else {
				//console.log('preload!',path.replace(/__define__\.js$/,'.js'),loader.base)
				loader.load(path,function(content){
					writeContent(request,response,content,'text/'+(url.match(/\.css$/)?'css':'javascript')+';charset=utf-8');
				},format)
				return true;
			}
		}else if(url.match(/\.css$/)){
			setTimeout(function(){
				writeFile(root,request,response)
				if(printRequestLog)console.log('\tloaded:'+url)
			},Math.random()*(maxTimeout*3));
		}else if(request.url.match(/\.html\?optimized=merge$/)){
			var exportHTML = require('../lib/export-html').exportHTML;
			exportHTML(root,url,function(content){
				writeContent(request,response,content,"text/html;charset=utf-8");
				if(printRequestLog)console.log('\tloaded:'+url)
			})
		}else if(request.url.match(/^\/\?proxy=/)){
			doProxy(request,response,decodeURIComponent(request.url.replace(/^.*=/,'')));
		}else{
			//console.log(routes)
			for(var i =0;i<mapping.length;i++){
				var route = mapping[i];
				var pattern = route.path;
				var match = false;
				if((typeof pattern) == 'string'){
 					route.path = pattern = new RegExp('^'+
 						pattern.split('').map(c=>c == '*'?'.*':(c.charCodeAt()+0x10000).toString(16).replace('1','\\u')).join('')
 						+'$','');
 				}

				if(pattern instanceof RegExp){
					match = url.match(pattern);
				}
				//console.log(url,pattern,match)
				if(match){
					if(route.action){
						match.unshift(request,response);
						if(route.action.apply(this,match)!== false){
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
						writeContent(request,response,"<h3>action || file || remote || data is requied!</h3>")
					}
					return;
				}
			}
			writeFile(root,request,response)
		}
	});
	var tryinc = 10;
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
	var oldMd5 = request.headers && request.headers['if-none-match'];
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
			if(printRequestLog)console.log('\t304 loaded:'+url)
		}else{
			response.writeHead(200, headers);
			response.end(content+'');
			//console.log('\tloaded:'+url)
		}
	},Math.random()*(md5 == oldMd5?maxTimeout/10:maxTimeout));
}

function doProxy(request,response,proxyPath){
	var path = request.url;
	var match = path.match(/^([^?]*?.*)(\?|\?.*&)__proxy__=([^&]+)(.*)$/);
	//console.log(proxyPath,match)
	if(proxyPath || match){
		path = proxyPath ? path : match[1]+match[2]+match[4];
		var realpath = proxyPath || match[3];
		match = realpath.match(/^(https?)\:\/\/([^\\\/:]+)(\:\d+)?(.*)$/) ;
		if(match){
			var impl = match[1] == 'https'?https:http;
			var host = match[2];
			var port = match[3]||(impl == https?443:80);
			var path = match[4] ;
			var impl = realpath.match(/^https/)?https:http;
			//var hostname = 
			request.headers.host = host;
			var options = {
				port:port,hostname:host,
				method:request.method, path:path, headers:request.headers
			}
			//console.dir(request.headers)
			var proxy_request = impl.request(options);
			proxy_request.addListener('response', function (proxy_response) {
				proxy_response.addListener('data', function(chunk) {
					response.write(chunk, 'binary');
				});
				proxy_response.addListener('end', function() {
					response.end();
				});
				
				proxy_response.headers['Access-Control-Allow-Origin']='*'
				response.writeHead(proxy_response.statusCode, proxy_response.headers);
			});
			proxy_request.on('error',function (e){
				console.error(e);
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
