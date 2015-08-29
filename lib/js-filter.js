var buildDefine = require('./define').buildDefine
var mergeInclude = require('./merge').mergeInclude

// function setupJSRequire(path, scriptBase, htmlPattern){
// 	scriptBase = scriptBase.replace(/[\\\/]?$/,'/');
// 	//add system resouce filter
// 	var pattern = new RegExp('^'+toRegSource(scriptBase)+'.+\.js$');
// 	//add define filter
// 	if(htmlPattern){
// 		rbs.addTextFilter(htmlPattern,function(resource,text){
// 			return replaceTemplate(scriptBase,text);
// 		})
// 	}
// 	rbs.addBinaryBuilder(pattern,function(resource,data){
// 		return buildSourceBinnary(resource,data,scriptBase);
// 	});
// 	rbs.addTextFilter(pattern,function(resource,text){
// 		return buildSource(resource,text,scriptBase)
// 	})
	
// 	rbs.addTextFilter(pattern,function(resource,text){
// 		return buildRequire(rbs,resource,text,scriptBase)
// 	})
// }

function buildRequire(path,text,loader){
	var sourcePath = path.replace(/__define__(?=\.js$)/,'');
	var isDefine = sourcePath != path;
	var text = new IncludeBuilder(path,text,buildLoader('~'),function(){
		if(isDefine){
			var id = path.replace(/\.js$/,'');
			return buildDefine(id,text);
		}else{
			return text;
		}
	});
}


function buildLoader(root){
	var fs = require('fs');
	root = root.replace(/\/?$/,'/');
	return function(path,callback){
		path = path.replace(/^\//,'');
		fs.readFile(root+path,function(err,data){
			if(err){
				var file = require.resolve(path);
				// var p = file.lastIndexOf('/node_modules/');
				// var path2 = '~'+file.substr(file.indexOf(p+2,'/')+1);
				fs.readFile(root+path,function(err,data){
					if(err){
						callback(path,data.toString())
					}else{
						callback(path,data.toString())
					}
				})
			}else{
				callback(path,data.toString())
			}
		});
	}
	
}

function buildSourceBinnary(resource,data,scriptBase){
	if(data == null){
		var path = resource.path;
		var sourcePath = path.replace(/__define__\.js$/,'.js');
		if(sourcePath != path){
//			console.log("!!!!!!!!!!!!!")
			resource.sourcePath = sourcePath;
		}else if(!data && /\.js$/.test(path)){
			path = path.substr(scriptBase.length);
			switch(path){
			case 'require.js':
			case 'console.js':
			case 'wait.js':
				try{
					var file = require.resolve('../assets/'+path);
					return resource.getExternalAsBinary(file).toString('utf-8');
				}catch(e){}
			}
		}
	}
	return data;
}

function buildSource(resource,text,scriptBase){
	if(text == null){
		var path = resource.path;
		var sourcePath = resource.sourcePath || path;
		var path = sourcePath.substr(scriptBase.length).replace(/\.js$/,'');
		try{
			var file = require.resolve(path);
		}catch(e){}

		if(file ){
			//console.log(file)
			file = file.replace(/\\/g,'/');
			if(file.substr(file.lastIndexOf(path)+path.length).indexOf('/')>=0){
				var p = file.indexOf('/node_modules/'+path+'/')+14;
				var realpath = file.substr(p).replace(/\.js$/i,'');
				text = "var o = require('"+realpath+"');for(var n in o){exports[n] = o[n]}";
			}else{
				text = resource.getExternalAsBinary(file).toString('utf-8');
			}
		}else{
			text = "/* not found module "+path+"*/";
		}
	}
	return text;
}



/**
 * 静态html 中模板，script标签内的模板替换
 */
function replaceTemplate(scriptBase,text){
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

function toRegSource(s,c){
	return c? '\\u'+(0x10000+c.charCodeAt()).toString(16).substr(1)
			: s.replace(/([^\w_-])/g,toRegSource);
}
exports.setupJSRequire = setupJSRequire;
