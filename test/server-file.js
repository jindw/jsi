var fs = require('fs');
var path = require('path');

exports.writeFile = writeFile;

function writeFile(root,request,response,realPath){
	var url = request.url.replace(/[?#].*$/,'');
	var filepath = path.join(root,realPath||url);
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
	    		return writeContent(filepath,request,response);
	    	}
	    }else{
	    	response.writeHead(404, {"Content-Type": "text/plain"});    
     		response.end("404 Not Found \n filepath:"+filepath);    
	    }
	});
}
function writeContent(filepath,request,response){
	var crypto = require('crypto');
	fs.readFile(filepath, "binary", function(err, file) {    
        if(err) {
            response.writeHead(500, {"Content-Type": "text/plain"});   
            response.end(err + "\n");    
            return;    
        }
        var contentType = "text/html"
        if(/.css$/.test(filepath)){
        	contentType = "text/css";
        }else if(/.js$/.test(filepath)){
        	contentType = "text/javascript";
        }else if(/\.(jpge?|png|gif)$/.test(filepath)){
        	contentType = filepath.replace(/.*\.(\w+)$/,'image/$1');
        }
        
		var crypto = require('crypto');
		var md5 = '"'+crypto.createHash('md5').update(file).digest('base64')+'"';
		var oldMd5 = request.headers['if-none-match'];
		var headers = {"Content-Type": contentType.replace(/^text\/.*/,'$&;charset=utf8'),"ETag":md5};
		//console.log(request.headers,"\n",headers);
		
		if(md5 == oldMd5){
        	response.writeHead(304, headers); 
			console.log('304 response: '+filepath)
        	response.end();  
		}else{
			if(/^image\//.test(contentType)){
				headers.Expires = new Date(1000 * 60 * 1 + +new Date() ).toGMTString()
			}
        	response.writeHead(200, headers); 
        	//If-None-Match: "686897696a7c876b7e"
        	//response.writeHeader("ETag","crypto")
        	response.end(file, "binary");  
		}
    });
}
var exportExample = fs.readFileSync(require.resolve('./index.html')).toString()
function writeIndex(filepath,response){
	//console.log('index:'+filepath)
	fs.readdir(filepath, function(err, files) { 
		files.sort(); 
		var buf = [];
		for(var i=0;i<files.length;i++){
			var filename= files[i];
			if(!/^\./.test(filename)){
				buf.push("<div class='file-row'><a href='",filename,"'>",filename,'</a></div>\n');
			}
		}
		var html = exportExample.replace('$!{dir}',filepath).replace('$!{content}',buf.join(''));
		
		response.writeHead(200,  {"Content-Type":'text/html;charset=utf8'}); 
		response.write(html,'utf-8');
		response.end();
	});
	
}
