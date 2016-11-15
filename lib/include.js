var fs = require('fs')
/**
 * 文件合并工具，支持异步化的文件合并
 * @param onComplete(text,file)
 */
function IncludeBuilder(text,onComplete,file){
	this.file = file;
	this.text = text;
	var indexes = this.indexes = [];
	var paths = this.paths = [];
	var contents = this.contents = {};
	var trimLen = 0;
	this.template = text.replace(/^\s*(?:\/\/\s*)?#include\b\s*[('"]+([\/\w\.\-]+?)[)"']+/mg,function(a,includePath,index){
		//console.log('!!!!!',includePath)
		paths.push(includePath);
		indexes.push(index-trimLen);
		trimLen += a.length;
		return '';
	})
	scheduleLoad(this,function(text,file){
		onComplete(text,file);
	});
}

function scheduleLoad(builder,onComplete){
	var len = builder.paths.length;
	var i = len;
	if(i){
		function loadComplete(content,path,file){
			//console.log(path,content)
			new IncludeBuilder(content,function(content,file){
				//console.log(path,content.length)
				builder.contents[path] = content;
				if(--len ==0){
					builder.complete = true;
					onComplete.apply(builder,[builder.toString(),this.file]);
				}
			},file)
		}
		while(i--){
			var path = builder.paths[i];
			var absFile = normalizePath(path,builder.file);
			asyncLoadFile(absFile,path,loadComplete)
		}
	}else{
		builder.complete = true;
		onComplete(builder.toString(),builder.path);
	}
}

IncludeBuilder.prototype.toString = function(){
	if(this.complete){
		var indexes = this.indexes;
		var len = indexes.length;
		var buf = [];
		var tmp = this.template;
		var begin = 0;
		for(var i=0;i<len;i++){
			var p = indexes[i];
			buf.push(tmp.substring(begin,begin = p));
			buf.push(this.contents[this.paths[i]]);
		}
		buf.push(tmp.substring(begin));
		return buf.join('');
	}else{
		return mergeSync(this.file,this.text)
	}
}
function mergeSync(file,text){
	return text.replace(/^\/\/\s*#include\b\s*[('"]+([\/\w\.\-]+?)[)"']+/mg,function(a,includeFile,index){
		includeFile = normalizePath(includeFile,file);
		var content = loader(includeFile);
		return mergeSync(includeFile,content);
	})
}
function syncLoadFile(file,onComplete){
	try{
		return fs.readFileSync(file)
	}catch(e){
		return loadBuildInSource(file);
	}
}
function asyncLoadFile(file,path,onComplete){
	fs.readFile(file,function(err,data){
		if(err){
			loadBuildInSource(path,onComplete)
		}else{
			onComplete(data.toString(),path,file)
		}
	})
}
function loadBuildInSource(path,callback){
	if(callback instanceof Function){
		var readFile = function (file){
			//console.log('###222',file)
			fs.readFile(file,readFileCallback);
			return true;
		}
	}else{
		var readFile = function (file){
			//console.log('###',file)
			return fs.readFileSync(file).toString();
		}
	}
	function readFileCallback(err,data){
		//console.log('###',err,data)
		if(err){
			callback(err,path,file)
		}else{
			callback(data.toString(),path,file)
		}
	}
	try{
		switch(path){
		case 'jquery.js':
			var file = require.resolve('../assets/test/jquery_1_9_1.js');
			return readFile(file);
		case 'lite-buildin.js':
		case 'require.js':
		case 'console.js':
		case 'boot.js':
		case 'block.js':
			var file = require.resolve('../assets/'+path);
			return readFile(file)
		case 'config/main.js':
			var source = '$JSI.init({})';
			callback(source,path,file);
			return source;
		case 'path':
		case 'fs':
		case 'process':
		case 'child_process':
		case 'util':
			return '';
		}
	}catch(e){
		callback(e,path,file)
		return e;
	}
}

/**

IncludeBuilder.mergeSync = function(path,text,loader){
	return text.replace(/^\/\/\s*#include\b\s*[('"]+([\/\w\.\-]+?)[)"']+/mg,function(a,inc,index){
		inc = normalizePath(inc,path);
		var content = loader(inc);
		return IncludeBuilder.mergeSync(inc,content,loader);
	})
}
function merge(path,text,loader){
	return text.replace(/^\/\/\s*#include\b\s*[('"]+([\/\w\.\-]+?)[)"']+/mg,function(a,inc){
		inc = normalizePath(inc,path);
		if(inc == '/assets/require.js'){
			//build in source;
			return mergeLoader(path,text,loader)
		}
		var content = loader(inc);
		var source = mergeInclude(inc,content,loader);
		return source;
	})
}
function mergeLoader(path,bootSource,loader){
	var sourceMap = {};
	var buf = [bootSource.replace(/\}(\);?\s*)$/,''),''];
	while(true){
		for(var n in sourceMap){
			if(!sourceMap[n]){
				path = n;
				break;
			}
		}
		if(path){
			try{
				var file = path.replace(/.js$/,'__define__.js');
				var data = loader(file);
			}catch(e){
				console.error('merge file not found:',file)
				data = "$JSI.define('"+path+"',[],function(){// file not found: "+file+"\n})"
			}
			var deps = JSON.parse(data.substring(data.indexOf('['),data.indexOf(']')+1));
			var impl = data.substring(data.indexOf('function('),data.lastIndexOf('}')+1);
			if(path in sourceMap){
				sourceMap[path] = true;
				buf.push('"',path.replace(/\.js$/,'')+'":[',impl,']',',')
			}
			for(var i = 0;i<deps.length;i++){
				var path2 = normalizePath(deps[i],path)+'.js';
				if(!(path2 in sourceMap)){
					sourceMap[path2] = null;
				}
			}
			var path = null;
		}else{
			break;
		}
	}
	buf.pop();
	buf.push('});')
	return buf.join('');
}
*/

function normalizePath(url,base){
    var url = url.replace(/\\/g,'/');
    if(url.charAt(0) == '.'){
    	url = base.replace(/[^\/]+$/,'')+url
    	while(url != (url =url.replace( /[^\/]+\/\.\.\/|(\/)?\.\//,'$1')));
    }
    return url;
}

exports.loadBuildInSource = loadBuildInSource;
exports.IncludeBuilder = IncludeBuilder;