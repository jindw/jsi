#JSI Introduction

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
 
		require('jsi/test');
		
 * html example:
 
		<!DOCTYPE html><html>
		<head>
		<title>test wait</title>
		<script src="/static/boot.js"></script>
		</head>
		<body>
		<hr>
		<script>
		var xmldom = require('xmldom');
		</script>
		<script async="async">
		document.write("<h3>DOMParser</h3><p>"+xmldom.DOMParser+"</p>")
		</script>
		<hr>
		<script>var lite = require('lite');</script>
		<script>
		var LiteEngine = lite.LiteEngine;
		document.write("<h3>LiteEngine</h3><p>"+LiteEngine+"</p>")
		</script>
		<hr>
		</body>
		</html>

 * extends server
 
		var ScriptLoader = require('../lib/js-loader.js').ScriptLoader;
		//setup resource loader
		var loader = new ScriptLoader('./');
		
		createServer(function(req,res){
			var url = req.url;
			if(url.match('\.js$')){
				console.log('start:'+url)
				loader.load(url.replace(/^\/static\/|\/assets\//,'/'),function(content){
					setTimeout(function(){
						res.writeHead(200, {'Content-Type': 'text/javascript;charset=utf-8'});
						res.end(content+'');
						console.log('\tend:'+url)
					},Math.random()*100);
				})
				return true;
			}
		},'./').listen(8080);
