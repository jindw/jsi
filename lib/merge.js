/**
 * 文件合并工具，支持异步化的文件合并
 * @param onLoad(id,function(file,id,text))
 */
function IncludeBuilder(path,text,onLoad,onComplete){
	this.path = path;
	var indexes = this.indexes = [];
	var files = this.files = [];
	var contents = this.contents = {};
	var trimLen = 0;
	this.template = text.replace(/^\s*(?:\/\/\s*)?#include\b\s*[('"]+([\/\w\.\-]+?)[)"']+/mg,function(a,inc,index){
		inc = normalizePath(inc,path);
		files.push(inc);
		indexes.push(index-trimLen);
		trimLen += a.length;
		return '';
	})
	scheduleLoad(this,onLoad,onComplete);
}
IncludeBuilder.mergeSync = function(path,text,loader){
	return text.replace(/^\/\/\s*#include\b\s*[('"]+([\/\w\.\-]+?)[)"']+/mg,function(a,inc,index){
		inc = normalizePath(inc,path);
		var content = loader(inc);
		return IncludeBuilder.mergeSync(inc,content,loader);
	})
}
function scheduleLoad(builder,onLoad,onComplete){
	function loadComplete(file,id,content){
		file = normalizePath(id,builder.path);
		new IncludeBuilder(file,content,onLoad,function(file,content){
			builder.contents[file] = content;
			if(--len ==0){
				onComplete(file,builder.path,builder.toString());
			}
		})
	}
	var files = builder.files;
	var len = files.length;
	var i = len;
	if(i){
		while(i--){
			onLoad(files[i],loadComplete)
		}
	}else{
		onComplete(builder.path,builder.toString());
	}
}
IncludeBuilder.prototype.toString = function(){
	var indexes = this.indexes;
	var len = indexes.length;
	var buf = [];
	var tmp = this.template;
	var begin = 0;
	for(var i=0;i<len;i++){
		var p = indexes[i];
		buf.push(tmp.substring(begin,begin = p));
		buf.push(this.contents[this.files[i]]);
	}
	buf.push(tmp.substring(begin));
	return buf.join('');
}



/**
 * TODO:.....
 */
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
				data = "$JSI.define('"+path+"',[],function(){/* file not found: "+file+"*/})"
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

function normalizePath(url,base){
    var url = url.replace(/\\/g,'/');
    if(url.charAt(0) == '.'){
    	url = base.replace(/[^\/]+$/,'')+url
    	while(url != (url =url.replace( /[^\/]+\/\.\.\/|(\/)?\.\//,'$1')));
    }
    return url;
}


exports.merge = merge;
exports.IncludeBuilder = IncludeBuilder;