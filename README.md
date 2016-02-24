:q
#JSI Introduction
a simple module  loader , you can use npm installed modules in web browser such as it work in nodejs!


Install:
=====
	npm install jsi -g
	
Start:
=====
 * install and run example:

 		$ cd <webroot>
 		$ jsi example
 		$ open http://localhost:8080
		
* command line
	* on [webroot]

			$ jsi example 									--deplay hello world example
			$ jsi start										--start debug server

	* on [webroot]/[scriptroot]

			$ jsi export -o temp.js main.js					--export main.js as a single javascript file
																(main.js variables on global namespace;
																	buf dependence modules is hidden, you can use require('modulename') to get it )
			$ jsi export -o exported.js -ns xmldom xmldom	--export package: export xmldom to a single script and exports variables on the namespace xmldom.



			//advance useage:  install a optimized external package on package system.
$ jsi install jquery							--install a exter:qnal optimized package from npm
			$ jsi install ./workspace/xmldom				--install a external optimized package from local filesystem


* lite template example

		//inline style
		var tpl = <div>
			<p> this is a xml style templete ; 
				default syntax support for mostly javascript editor (E4X standard) </p>
		</div>
		
		//single template file
		var tpl = liteXML("/path.tpl")

		//partly template file（css3 selector）
		var tpl = liteXML("/path.tpl#header")
		
* javascript debug and compress

	* enable debug

		javascript:document.cookie="JSI_DEBUG=true"
	* disable debug	

		javascript:document.cookie="JSI_DEBUG=false"

* test route 
		<root>/route.js
		
		content:
		exports = [

			//mock with json data
			{path:"/service/login.do",data:{"auth-token":"sdeee23734ru3hfbvncm"}},

			//mock from remote url 
			{path:"/service/user.do",remote:"http://test.com/user.do"},

			//mock from local json file
			{path:"/service/user.do",file:"./mock/user.json"},


			//mock with nodejs request handle
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

   