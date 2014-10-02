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


function writeFile(root,url,response){
	var filepath = path.join(root,url);
	fs.stat(filepath,function(error,stats){
		if(stats){
	    	if(stats.isDirectory()){
	    		if(/[\\\/]$/.test(url)){
	    			writeIndex(filepath,response);
	    		}else{
	    			response.writeHead(301, {"Location" : url+'/'});    
	            	response.end();    
	    		}
	    	}else{
	    		return writeContent(filepath,response);
	    	}
	    }else{
	    	response.writeHead(404, {"Content-Type": "text/plain"});    
     		response.end("404 Not Found \n filepath:"+filepath);    
	    }
	});
}
function writeContent(filepath,response,prefix,postfix){
	fs.readFile(filepath, "binary", function(err, file) {    
        if(err) {
            response.writeHead(500, {"Content-Type": "text/plain"});   
            response.end(err + "\n");    
            return;    
        }
        var contentType = "text/html"
        if(/.css$/.test(filepath)){
        	contentType = "text/css";
        }else  if(/.js$/.test(filepath)){
        	contentType = "text/javascript";
        }
        response.writeHead(200, {"Content-Type": contentType+';charset=utf8'}); 
        if(prefix||postfix){
         	prefix && response.write(prefix);
         	response.write(file, "binary"); 
         	postfix && response.write(postfix); 
        	response.end();
        }else{
        	response.end(file, "binary");  
        }    
    });
}
function writeIndex(filepath,response){
	fs.readdir(filepath, function(err, files) { 
		files.sort(); 
		for(var i=0;i<files.length;i++){
			response.write("<a href='"+files[i]+"'>"+files[i]+'</a><hr/>','utf8');
		}
		response.end();
	});
	
}
