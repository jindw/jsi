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
				//html 中require 函数必须放在最前面的单独的script标签中！
				//因为html script 中的 require 只能保证在下一个script 出现之前被装载完成！
				var xmldom = require('xmldom');
			</script>
			<script>
				document.write("<h3>DOMParser</h3><p>"+xmldom.DOMParser+"</p>")
			</script>
			<hr>
			<script>
				var lite = require('lite');</script>
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
