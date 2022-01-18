var partitionJavaScript = require('./js-token').partitionJavaScript;

/**
 * @param loader
 * @param path
 * @param callback(path,realPath) this is the define
 */
function Define(loader,path,callback,source,file){
	this.loader = loader;
	this.path = path;
	var thiz = this;
	var path = path
	//console.warn('define load:',path);
	if(source){
		onload(source,path,file||'')
	}else{
		loader.loadFile(path,onload);
	}
	function onload(source,realPath,file){
		//console.log(realPath,file)
		//console.warn('load file:',file)
		thiz.source = source;
		thiz.file = file;
		thiz.realPath = realPath;
		var root = thiz.loader.bases[0].base
		// if(source.match(/^<[\s\S]+>\s*$/)){
		// 	//xml
		// 	source = parseTemplate(root,file,source,{});
		// 	thiz.requireIndexes = [];
		// 	thiz.tokens = ['module.exports=',source];
		// }else{
			thiz.parseDependence(root);
		//}
		var depMap = {};
		var tokens = thiz.tokens;
		var requireIndexes = thiz.requireIndexes;
		for(var i=0;i<requireIndexes.length;i++){
			var idx = requireIndexes[i];
			//console.log('idx:'+tokens[idx],realPath,file)
			var requireItem = evalString(tokens[idx]);
			var translatedPath = depMap[requireItem]
			if(!translatedPath){
				//console.warn('deps:',requireItem,realPath,file)
				translatedPath = loader.translatePath(requireItem,realPath,file)
				console.log('Define onload1',translatedPath,requireItem,realPath,file)
				translatedPath = translatedPath.replace(/(\/.*)\.js$/,'$1')
				depMap[requireItem] = translatedPath
			}
			tokens[idx] = '"'+translatedPath+'"'
		}
		thiz.depMap = depMap;
		var deps = this.deps = [];
		for(var n in depMap){
			var absDep = depMap[n];
			deps.indexOf(absDep)<0 && deps.push(absDep);
		}
		callback.call(thiz,path,realPath);
	}
}
Define.prototype.toString = function (replaceMap){
	if(!replaceMap){
		replaceMap = {};
	}
	var deps = [];
	var tokens = this.tokens.concat();
	var requireIndexes = this.requireIndexes;
	for(var i=0;i<requireIndexes.length;i++){
		var idx = requireIndexes[i];
		var oldPath = tokens[idx].replace(/['"]/g,'')
		var dep = oldPath in replaceMap?replaceMap[oldPath]:oldPath;
		tokens[idx] = JSON.stringify(dep);
		if(deps.indexOf(dep) ==-1 && typeof dep == 'string'){
			deps.push(dep)
		}
	}
	var source =tokens.join('');
	var i = deps.length;
	var result = ["$JSI.define('",this.realPath.replace(/\.js$/,''),"',["];
	if(deps.length){
		result.push('"',deps
			//.map(a=>a.replace(/\.js$/,''))//子文件夹都是直接带扩展名的。
			.join('","'),'"')
	}
	result.push('],function(exports,require');
	
	var realPath = this.realPath;
	var hitfilename = false;
	if(/\b__(?:file|dir)name\b/.test(source)){
		result.push(',module,__filename');
		hitfilename = true;
	}else if(/\bmodule\b/.test(source) || this.defaultName){
		result.push(',module');
	}
	result.push('){');
	
	
	
	
	if(hitfilename && realPath && realPath !=this.path){
		var p = realPath.indexOf('/');
		if(p >0){
			var pathname = realPath.substring(p)+'.js';
		}else{
			var pathname = '/';
		}
		var rel = relative(realPath,this.path).replace(/^\.\//,'');
		var postfix = rel.replace(/^(\.+\/)+/,'');
		var perfix = rel.substr(0,rel.length-postfix.length);
		var c  = perfix.length/3 ;
		var reg = c ? "/[^\\/]+(?:[^\\/]+\\/){"+c+"}$/":"/[^\\/]+$/"
		
		result.push('__filename = __filename.replace('+ reg+',"'+postfix+'");')
	}
	if(/\b__dirname\b/.test(source)){
		result.push('var __dirname= __filename.replace(/[^\\/]+$/,"");');
	}
	
	
	
	result.push(source,'\n');
	if(this.defaultName){
 		result.push('module.exports = exports = '+this.defaultName+';\n');
 	}
 	result.push(this.exportNames.join(';').replace(/[\w\$]+/g,'exports.$&=$&'));
 	result.push('\n});');
	return result.join('');
}

Define.prototype.parseDependence = function(root){
	var rtv = parseDependence(this.source,root,this.file);
	this.requireIndexes = rtv[0];
	this.tokens = rtv[1];

	this.exportNames = rtv[2];
 	this.defaultName = rtv[3];
}
// Define.parseDependence = function(source,root,file){
// 	var indexAndToken = parseDependence(source,root,file);
// 	var requireIndexes = indexAndToken[0];
// 	var tokens = indexAndToken[1];
// 	var depMap = {};
// 	var deps = []
// 	for(var i=0;i<requireIndexes.length;i++){
// 		var idx = requireIndexes[i];
// 		var requireItem = evalString(tokens[idx]);
//		//loader? realPath?
// 		var translatedPath = loader.translatePath(requireItem,realPath,file)
// 		//console.log('^^^',translatedPath,requireItem,realPath,file)
// 		if(translatedPath in depMap){
// 			depMap[translatedPath] = true;
// 			deps.push(translatedPath)
// 		}
// 	}
// 	return deps;
// }
function parseDependence(content,root,file){
	//var moduleName = id.replace(/\/.*/,'')
	//console.log("###",id,realId)
	var tokens = partitionJavaScript(content,file,root);
	var requireIndexes = [];
	var exportNames = [];
 	var defaultName ;
	//console.log("###",id,realId)
	for(var i =0;i<tokens.length;i++){
		var item = tokens[i];
		switch(item.charAt()){
		case '\'':
		case '\"':
			//console.log(item);
			var prev = tokens[i-1];
			var next = tokens[i+1];
 			if(prev){
 				var m = prev.match(/import\s+(\{[^\{\}]+\}|[\$\w]+)\s+from\s*\(?\s*$/);
 
 				if(m){
 					prev = tokens[i-1] = prev.replace(/import\s+(?:\{[^\{\}]+\}|[\$\w]+)\s+from\s*\(?\s*$/,'var '+m[1].replace(/\s+as\s+/g,':')+' = require(');
 					if(!m[0].match(/\(\s*$/)){
 						next = tokens[i+1] = ')'+next;
 					}
 					
 				}
 				if(/^\s*[\)\+]/.test(next)){

					//var tpl1 = new XML("<div></div>");
					//function tpl2(a,b){return new XML("<div style='display:${a}'>${b}</div>");}
					var match = prev.match(/\b(?:(require))\s*\(\s*$/)
					if(match){
						var isRequire = match[1] == 'require'
						if(isRequire){
							requireIndexes.push(i)
						}
					}
				}
			}
 			break;
 		case '/': // regexp
 			break;
 		default:
 			var exp = /\bexport\s+(default\s+)?(?:(function|class|const|var|let)\s+)?([\$\w]+)/g;
 			var m = item.match(exp);
 			if(m){
 				tokens[i] = item.replace(exp,function(a,d,t,n){
 					if(d){
 						defaultName = n;
 					}else{
 						exportNames.push(n);
 					}
 					
 					return (t ||'')+' '+ n;
 				})
 			}
 			

		}
	}
	return [requireIndexes,tokens,exportNames,defaultName]
}

function evalString(s){
	return Function('return '+s)()
}


function absoluteModule(url,parentModule){
	//console.log('absModule:',url,'|',parentModule)
	if(url.charAt(0) == '.'){
		url = url.replace(/\.js$/i,'');
		url = parentModule.replace(/([^\/]*)?$/,'')+url
		while(url != (url =url.replace( /[^\/]+\/\.\.\/|(\/)?\.\//,'$1')));
	}
	//console.log(url)
	return url;
}

function relative(item,base){
	var sp1 = base.split('/');
	var sp2 = item.split('/');
	var leaf = sp2.pop();
	sp1.pop();
	for(var i=0;i<sp1.length;i++){
		if(sp1[i] == sp2[i]){
			sp1.shift();
			sp2.shift();
			i--;
		}else{
			break;
		}
	}
	return ((sp1.join('/').replace(/[^\/]+/g,'..') || '.')+'/'+sp2.join('/').replace(/.+/,'$&/')).replace(/^\.\/\./,'')+leaf
}


exports.Define = Define;