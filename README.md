#JSI Introduction
a simple module  loader , you can use npm installed modules in web browser such as it work in nodejs!

Functions
=====
 * base on npm server and tools
 	* install from : loacal file,npm server,github ect...
 	* dependences install automatic
 * auto merge,compress,export(browserify) and debug support
 	* merge module scripts as a signle script module
 	* merge module scripts as a signle browser support script
 	* build a compressed script for release and a uncompressed script for debug 
 	* if the cookie: JSI_DEBUG=true ,load a uncompressed one, otherwise load the compressed one.
 * pure nodejs style syntax.
	* without browser script wrapper.
 	* npm module support  automatic.
 * CommonJS1.0 compatible JavaScript loader [ require.js]
 	* Rewriting of the original [JSI2](http://www.xidea.org/project/jsi) boot.js.
 	* give up the original specification.
   
Install:
=====
	npm install jsi -g
	
Example:
=====
 * start test server:
 
		node -e "require('jsi')";
		
 * html example:

		<html> 
		<head>
		<title>Hello jQuery</title> 
		<script src="../scripts/boot.js" type="text/javascript">
		$=require('jquery');
		</script> 
		<script type="text/javascript"> 
		$(document).ready(function(){ 
			alert("Hello World!"); 
		}); 
		</script> 
		</head> 
		<body> 
		</body> 
		</html> 
		
* command line

		$ cd ./scripts/
		$ jsi example 									--deplay hello world example
		$ jsi start										--start debug server
		$ jsi install jquery							--install a package from npm
		$ jsi install ./workspace/xmldom				--install a package from local filesystem
		$ jsi export -o temp.js main.js					--export main.js as a single javascript file
		$ jsi export -o exported.js xmldom jquery		--export package: xmldom and jquery as a single script can work on the browser.
		$ jsi browserify -o exported.js xmldom lite		--alias command of export


* lite template example

		var tpl = <div>
			<p> this is a xml style templete ; 
				default syntax support for mostly javascript editor (E4X standard) </p>
		</div>
		
		
		var tpm = liteXML("/path.tpl#body>*")
		
* javascript debug and compress

	* enable debug									-- javascript:document.cookie="JSI_DEBUG=true"
	* disable debug									-- javascript:document.cookie="JSI_DEBUG=false"

* test route 
		<root>/route.js
		
		content:
		exports = [
			{path:"/service/login.do",data:{"auth-token":"sdeee23734ru3hfbvncm"}},
			{path:"/service/user.do",remote:"http://test.com/user.do"},
			{path:"/service/user.do",file:"./mock/user.json"},
			{
				path:	/\/service\/user\/(\d+)/,
				action:	function(request,response,path,uid){
					var headers = {"Content-Type":"text/json;charset=utf-8"};
					response.writeHead(200, headers); 
					response.end(JSON.stringify({"uid":uid}));
					return true;
				}
			}
		]
