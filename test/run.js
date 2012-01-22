var Path  = require('path');
var root = Path.resolve(__dirname,'../web/');


var ENV = require('../lib/env').ENV;
var setupJSI = require('../lib/jsi-filter').setupJSI;
var env = new ENV(root);
setupJSI(env,'/static/');

try{
	console.log('xmldom is install at:',require.resolve('xmldom'))
}catch(e){
	console.error("xmldom for test is not install !! please npm install xmldom");
}

console.log(env.getContentAsBinary('/static/xmldom/dom-parser__define__.js')+'')

var FS = require('fs');//
require('http').createServer(function (req, response) {
	var url = req.url.replace(/[?#][\s\S]*/,'');
	var filepath = Path.join(root,url);
	FS.stat(filepath,function(error,stats){
	    if(stats){
	    	if(stats.isDirectory()){
	    		return writeDir(url,filepath,response);
	    	}
	    }
	    var text = env.getContentAsBinary(url);
	    if(text){
	    	response.writeHead(200, {'Content-Type' : 'text/html;charset=utf-8'});
	    	response.write(text);
	    	response.end();
	    	return;
	    }
	    if(stats){
	    	writeNotFound(filepath,response,"compile failed"); 
	    }else{
	    	writeNotFound(filepath,response); 
	    }
	});
}).listen(1985,'127.0.0.1');
console.log('lite test server is started: http://'+('127.0.0.1')+':' + (1985) );

function writeNotFound(filepath,response,msg){
     response.writeHead(404, {"Content-Type": "text/plain"});    
     response.write("404 Not Found \n filepath:"+filepath+'\n'+(msg||''));    
     response.end();    
}

function writeDir(url,filepath,response){
	if(/\/$/.test(url)){
		FS.readdir(filepath, function(err, files) {  
			for(var i=0;i<files.length;i++){
				response.write("<a href='"+files[i]+"'>"+files[i]+'</a><hr/>','utf8');
			}
			response.end();
		});
	}else{
		response.writeHead(301, {"Location" : url+'/'});    
	            	response.end();    
	}
}