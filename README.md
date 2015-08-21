#JSI Introduction
a simple module  loader , you can use npm installed modules in web browser such as it work in nodejs!

##Functions
 * pure nodejs style syntax.
   * without browser script wrapper.
   * npm module support  automatic.
 * CommonJS1.0 compatible JavaScript loader [ require.js]
   * Rewriting of the original [JSI2](http://www.xidea.org/project/jsi) boot.js.
   * give up the original specification.
   
##Install:
	npm install jsi
	
##Example:
 * start test server:
 
		node -e "require('jsi/test')";
		
 * html example:
 
		<!DOCTYPE html><html>
		<head>
			<title>test wait</title>
			<script src="/static/boot.js">
				//blocked sync loading example
				//lite will be loaded from next script node 
				var xmldom = require('xmldom');
			</script>
		</head>
		<body>
			<h3>Blocked Sync Load on boot script</h3>
			<script>
				document.write("<h3>DOMParser</h3><p>"+xmldom.DOMParser+"</p>")
			</script>
			
			<h3>Blocked Sync Load </h3>
			<script>
			//xml can be used on the next script node.
			var xml = require('lite/parse/xml');
			</script>
			<script>
			document.write("<h3>loadLiteXML</h3><pre><code>"+xml.loadLiteXML+"</code></pre>")
			</script>
			
			<h3>Async Load Example</h3>
			<div id="asyncLoadInfo">Async Loading....</div>
			<script>
			$JSI.require(function(xmldom,lite){
				var c = document.getElementById('asyncLoadInfo');
				c.innerHTML = "<h4>LiteEngine</h4><pre><code>"+lite.LiteEngine+"</code></pre>"+
							  "<h4>xmldom</h4><pre><code>"+xmldom.DOMParser+"</code></pre>";
			},'xmldom','lite')
			</script>
			<hr>
		</body>
		</html>

 * extends server

		var fs = require('fs');
		var path = require('path');
		var http = require('http');
		
		var ScriptLoader = require('../lib/js-loader.js').ScriptLoader;
		var loaderMap = {};
		var webRoot = require('path').resolve('./')
		http.createServer(function (req, res) {
			var url = req.url.replace(/[?#].*$/,'');
			if(url.match('\.js$')){
				var path = url.replace(/^\/(?:static|assets|scripts?)(?:\/js)?\//,'/');
				var base = webRoot + url.slice(0,1-path.length)
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
				writeFile(webRoot,url,res)
			}
		}).listen(8080);

