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
		$ jsi install jquery							--install a package from npm
		$ jsi install ./workspace/xmldom				--install a package from local filesystem
		$ jsi export -o exported.js xmldom jquery		--export package: xmldom and jquery as a single script can work on the browser.
		$ jsi browserify -o exported.js xmldom lite		--alias command of export

