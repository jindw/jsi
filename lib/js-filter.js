var FS = require('fs');
var buildDefine = require('./define').buildDefine
var merge = require('./merge').merge

function setupJSRequire(rbs, prefix){
	prefix = prefix.replace(/[\\\/]?$/,'/');
	//add system resouce filter
	var pattern = new RegExp('^'+toRegSource(prefix)+'.+\.js$');
	//add define filter
	
	rbs.addBinaryBuilder(pattern,function(resource,data){
		return buildSourceBinnary(resource,data,prefix);
	});
	rbs.addTextFilter(pattern,function(resource,text){
		return buildSource(resource,text,prefix)
	})
	
	rbs.addTextFilter(pattern,function(resource,text){
		return buildRequire(rbs,resource,text,prefix)
	})
}
function buildSourceBinnary(resource,data,prefix){
	if(data == null){
		var path = resource.path;
		var sourcePath = path.replace(/__define__\.js$/,'.js');
		if(sourcePath != path){
//			console.log("!!!!!!!!!!!!!")
			resource.sourcePath = sourcePath;
		}else if(!data && /\.js$/.test(path)){
			path = path.substr(prefix.length);
			switch(path){
			case 'require.js':
			case 'console.js':
				try{
					var file = require.resolve('../static/'+path);
					return resource.getExternalAsBinary(file).toString('utf-8');
				}catch(e){}
			}
		}
	}
	return data;
}
function buildSource(resource,text,prefix){
	if(text == null){
		var path = resource.path;
		var sourcePath = resource.sourcePath || path;
		var path = sourcePath.substr(prefix.length).replace(/\.js$/,'');
		try{
			var file = require.resolve(path);
		}catch(e){}
		if(file ){
			//console.log(file)
			text = resource.getExternalAsBinary(file).toString('utf-8');
			file = file.replace(/\\/g,'/');
			if(path.indexOf('/')<0){
				var p = file.indexOf('/node_modules/'+path+'/')+14;
				var realpath = file.substr(p);
				text = "$JSI.copy(require('"+realpath+"'),exports)";
			}
		}else{
			text = "/* not found module "+path+"*/";
		}
	}
	return text;
}
function buildRequire(rbs,resource,text,prefix){
	var path = resource.path;
	var sourcePath = path.replace(/__define__(?=\.js$)/,'');
	var text = merge(rbs,resource,sourcePath,text,prefix)
	if(sourcePath != path){
		var path = sourcePath.substr(prefix.length).replace(/\.js$/,'');
		return buildDefine(path,text);
	}else{
		return text;
	}
}


function toRegSource(s,c){
	return c? '\\u'+(0x10000+c.charCodeAt()).toString(16).substr(1)
			: s.replace(/([^\w_-])/g,toRegSource);
}
exports.setupJSRequire = setupJSRequire;
