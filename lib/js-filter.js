var FS = require('fs');
var buildDefine = require('./define').buildDefine
var merge = require('./merge').merge

function setupJSRequire(rbs, prefix, htmlPattern){
	prefix = prefix.replace(/[\\\/]?$/,'/');
	//add system resouce filter
	var pattern = new RegExp('^'+toRegSource(prefix)+'.+\.js$');
	//add define filter
	if(htmlPattern){
		rbs.addTextFilter(htmlPattern,function(resource,text){
			return replaceTemplate(prefix,text);
		})
	}
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
			case 'wait.js':
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
			file = file.replace(/\\/g,'/');
			if(file.substr(file.lastIndexOf(path)+path.length).indexOf('/')>=0){
				var p = file.indexOf('/node_modules/'+path+'/')+14;
				var realpath = file.substr(p).replace(/\.js$/i,'');
				text = "$JSI.copy(require('"+realpath+"'),exports)";
			}else{
				text = resource.getExternalAsBinary(file).toString('utf-8');
			}
		}else{
			text = "/* not found module "+path+"*/";
		}
	}
	return text;
}
function replaceTemplate(prefix,text){
	return text.replace(/<script\b[^>]*\/>|(<script\b[^>]*>)([\s\S]*?)<\/script>/g,function(a,attrs,content){
		if(!/\s+src\s*=/.test(attrs) && /\brequire\s*\(/.test(content)){
			//console.log(attrs,content.substr(0,10))
			if(content){
				var data = buildDefine('',content);
				var deps = data.substring(data.indexOf('[')+1,data.indexOf(']'));
				if(deps){
					if(/\s+(async|defer)\s*?[=\s>]/.test(attrs)){
						var impl = data.substring(data.indexOf('{'),data.lastIndexOf('}'));
						//TODO... get globals
						var globals = "";
						if(globals){
							globals = '\n'+globals.replace(/[^,]+/,"this.$& = $&");
						}
						return attrs+"$JSI.load("+deps+",function()"+impl+globals+"},false)</script>";
					}else{
						return "<script>$JSI.load("+deps+",{},true)</script>"+a;
					}
				}
			}
		}
		return a;
	})
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
