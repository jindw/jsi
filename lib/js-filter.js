var FS = require('fs');
var syntaxTestPostfix = '/*\'"*/';
function setupJSRequire(resourceManager, prefix){
	prefix = prefix.replace(/[\\\/]?$/,'/');
	var pattern = new RegExp('^'+toRegSource(prefix)+'(.+)__define__\.js$');
	resourceManager.addBinaryBuilder(pattern,function(resource,data){
		resource.sourcePath = prefix + resource.path.replace(pattern,'$1')+'.js';
	});
	resourceManager.addTextFilter(pattern,function(resource,text){
		var sourcePath = resource.sourcePath;
		var path = sourcePath.substr(prefix.length).replace(/\.js$/,'');
		if(text == null){
			try{
				var file = require.resolve(path);
			}catch(e){}
			if(file ){
				text = resource.getExternalAsBinary(file).toString('utf-8');
				file = file.replace(/\\/g,'/');
				if(path.indexOf('/')<0){
					var p = file.indexOf('/node_modules/'+path+'/')+14;
					var realpath = file.substr(p);
					return buildDefine(path,"$JSI.copy(require('"+realpath+"'),exports)",realpath);
				}
			}else{
				return buildDefine("/* not found module "+path+"*/");
			}
		}
		return buildDefine(path,text);
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
	test = buildDefineFromTokens(test,deps,sourceToken,targetTokens)

	result.push(deps.join(','))
	result.push('],function(require,exports){',test,'\n});');
	return result.join('');
}
function buildDefineFromTokens(test,deps,sourceTokens,targetTokens){
	try{
		new Function(test);
		test = targetTokens.join('');
	}catch(e){
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
			: s.replace(/([^w_-])/g,toRegSource);
}
exports.setupJSRequire = setupJSRequire;