var FS = require('fs');
var syntaxTestPostfix = '/*\'"*/';

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
	var isDefine = /__define__\.js$/.test(path);
	text = text.replace(/^\/\/\s*#include\b\s*[('"]+([\/\w\.\-]+?)[)"']+/mg,function(a,inc){
		var source = resource.load(inc).toString('utf-8');
		
		if(!isDefine && inc == '/static/require.js'){
			//build in source;
			return exportSource(rbs,path.substr(prefix.length),prefix,source)
		}
		return source;
	})
	if(isDefine){
		var sourcePath = resource.sourcePath || path;
		var path = sourcePath.substr(prefix.length).replace(/\.js$/,'');
		return buildDefine(path,text);
	}else{
		return text;
	}
}

function normalizeModule(url,base){
    var url = url.replace(/\\/g,'/');
    if(url.charAt(0) == '.'){
    	url = base.replace(/[^\/]+$/,'')+url
    	while(url != (url =url.replace( /[^\/]+\/\.\.\/|(\/)?\.\//,'$1')));
    }
    return url;
}

function exportSource(rbs,path,prefix,bootSource){
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
			var file = prefix+path.replace(/.js$/,'__define__.js');
			var data = rbs.getContentAsBinary(file).toString('utf-8');
			var deps = JSON.parse(data.substring(data.indexOf('['),data.indexOf(']')+1));
			var impl = data.substring(data.indexOf('function('),data.lastIndexOf('}')+1);
			if(path in sourceMap){
				sourceMap[path] = true;
				buf.push('"',path.replace(/\.js$/,'')+'":',impl,',')
			}
//			console.log('deps',path,deps,data)
			for(var i = 0;i<deps.length;i++){
				var path2 = normalizeModule(deps[i],path)+'.js';
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
/**
 * 
 * require("csshelper").css("'+cssPath +'");'
			cssPath = file.replace(/.js$/,'.css');
			if(!resource.getExternalAsBinary(cssPath)){
				cssPath = null;
			}
			cssPath = cssPath && cssPath.substr(p)
			var cssPath = sourcePath.replace(/\.js$/i,'.css');
			var cssContent = resourceManager.getContent(cssPath,resource.prefix,resource.postfix);
			console.log(cssContent)
			cssPath = cssContent && cssPath;
 */

function buildDefine(path,text){
	text = text.replace(/\r\n?/g,'\n');
	var result = ["$JSI.define('",path,"',["];
	var deps = [];
	text  = buildDependence(path,text,deps)
	if(deps.length){
		result.push('"',deps.join('","'),'"')
	}
	
	result.push('],function(require,exports){',text,'\n});');
	return result.join('');
}
function buildDependence(path,text,deps){
	var sourceToken = [];
//	var targetTokens = [];
	var end=0;
	var test = text.replace(/\brequire\(\s*([^)\s]+)\s*\)/g,function(a,dep,start){
		if(dep == (dep = dep.replace(/^(['"])([^'"]+)\1$/,'$2'))){
			return a
		}
		end = text.substring(end,start);
		sourceToken.push(end);
//		targetTokens.push(end);
		
		end = start+a.length;
		deps.push(dep);
		
		sourceToken.push(a);
//		targetTokens.push( a = 'require("'+dep+'")');//e,
		return a+syntaxTestPostfix;
	});
	//targetTokens.push(text.substr(end));
	sourceToken.push(text.substr(end));
	test = buildDefineFromTokens(test,deps,sourceToken);
	return test;
}
function buildDefineFromTokens(test,deps,sourceTokens){
	try{
		new Function(test);
		test = sourceTokens.join('');
	}catch(e){
		//TODO,先对源码监察一边
		//console.log("Error",e)
		//var dest = sourceTokens.concat();
		var end = sourceTokens.length-2;
		var i=end;
		while(i>0){
			try{
				var token = sourceTokens[i];
				sourceTokens[i] += syntaxTestPostfix;
				new Function(sourceTokens.join(''))
			}catch(e){
				deps.splice((i-1)/2,1)
			}
			sourceTokens[i] = token;
			i-=2;
		}
		test = sourceTokens.join('');
	}
	var end = deps.length-1;
	while(end>0){
		var dep = deps[end];
		var i = end--;
		while(i--){
			if(dep == deps[i]){
				deps.splice(end,1)
				break;
			}
		}
		
	}
	return test;
}
function toRegSource(s,c){
	return c? '\\u'+(0x10000+c.charCodeAt()).toString(16).substr(1)
			: s.replace(/([^\w_-])/g,toRegSource);
}
exports.setupJSRequire = setupJSRequire;
exports.setupJSRequire = setupJSRequire;