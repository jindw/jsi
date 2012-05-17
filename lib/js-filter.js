var FS = require('fs');
var syntaxTestPostfix = '/*\'"*/';
function setupJSRequire(rbs, prefix){
	prefix = prefix.replace(/[\\\/]?$/,'/');
	//add system resouce filter
	var pattern = new RegExp('^'+toRegSource(prefix)+'.+\.js$');
	//add define filter
	
	rbs.addBinaryBuilder(pattern,function(resource,data){
		var path = resource.path;
		var sourcePath = path.replace(/__define__\.js$/,'.js');
		if(sourcePath != path){
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
	});
	rbs.addTextFilter(pattern,function(resource,text){
		var path = resource.path;
		var sourcePath = resource.sourcePath || path;
		var path = sourcePath.substr(prefix.length).replace(/\.js$/,'');
		if(text == null){
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
		
	})
	
	rbs.addTextFilter(pattern,function(resource,text){
		var path = resource.path;
		var isDefine = /__define__\.js$/.test(path);
		text = text.replace(/^\/\/\s*#include\b\s*[('"]+([\/\w\.\-]+?)[)"']+/mg,function(a,path){
			return resource.load(path).toString('utf-8');
		})
		if(isDefine){
			var sourcePath = resource.sourcePath || path;
			var path = sourcePath.substr(prefix.length).replace(/\.js$/,'');
			return buildDefine(path,text);
		}else{
			return text;
		}
	})
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
	var sourceToken = [];
	var targetTokens = [];
	var end=0;
	var test = text.replace(/\brequire\(\s*([^)\s]+)\s*\)/g,function(a,dep,start){
		if(dep == (dep = dep.replace(/^\s*(['"])([^'"]+)\1\s*$/,'$2'))){
			return a
		}
		end = text.substring(end,start);
		sourceToken.push(end);
		targetTokens.push(end);
		sourceToken.push(a);
		
		end = start+a.length;
		deps.push(dep = '"'+dep+'"');
		targetTokens.push( a = 'require('+dep+')');//e,
		return a+syntaxTestPostfix;
	});
	targetTokens.push(text.substr(end));
	test = buildDefineFromTokens(test,deps,sourceToken,targetTokens);
	result.push(deps.join(','))
	result.push('],function(require,exports){',test,'\n});');
	return result.join('');
}
function buildDefineFromTokens(test,deps,sourceTokens,targetTokens){
	try{
		new Function(test);
		test = targetTokens.join('');
	}catch(e){
		//TODO,先对源码监察一边
		console.log("Error",e)
		var dest = sourceTokens.concat();
		var end = sourceTokens.length-2;
		var i=end;
		while(i>0){
			try{
				dest[i] = targetTokens[i]+syntaxTestPostfix;
				new Function(dest.join(''))
				dest[i] = targetTokens[i];
			}catch(e){
				dest[i] = sourceTokens[i];
				deps.splice((i-1)/2,1)
			}
			i-=2;
		}
		test = dest.join('');
	}
	deps.sort();
	var end = deps.length-1;
	while(end>0){
		if(deps[end--] == deps[end]){
			deps.splice(end,1)
		}
	}
	return test;
}
function toRegSource(s,c){
	return c? '\\u'+(0x10000+c.charCodeAt()).toString(16).substr(1)
			: s.replace(/([^\w_-])/g,toRegSource);
}
exports.setupJSRequire = setupJSRequire;