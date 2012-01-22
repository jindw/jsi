var FS = require('fs');
var syntaxTestPostfix = '/*\'"*/';
function setupJSI(env, prefix){
	prefix = prefix.replace(/[\\\/]?$/,'/');
	var pattern = new RegExp('^'+toRegSource(prefix)+'(.+)__define__\.js$');
	env.addBinaryBuilder(pattern,function(resource,data){
		resource.sourcePath = prefix + resource.path.replace(pattern,'$1')+'.js';
	});
	env.addTextFilter(pattern,function(resource,text){
		var path = resource.sourcePath.substr(prefix.length).replace(/\.js$/,'');
		if(!text){
			var file = require.resolve(path);
			if(file ){
				file = env.vfs.getFile(file,resource.instance.key);
				text = String(FS.readFileSync(file));
				if(path.indexOf('/')<0){
					return buildDefine(path,text,path+"/");
				}
			}
		}
		return buildDefine(path,text);
	})
}

function normalizeModule(url,base){
    var url = url.replace(/\\/g,'/');
    if(url.charAt(0) == '.'){
    	url = base.replace(/[^\/]+$/,'')+url
    	while(url != (url =url.replace( /[^\/]+\/\.\.\/|(\/)?\.\//,'$1')));
    }
    return url;
}
function buildDefine(path,text,realpath){
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
		if(realpath ){
			dep = normalizeModule(dep,realpath)
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
exports.setupJSI = setupJSI;