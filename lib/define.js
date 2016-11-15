try{
	var jstoken = require('./js-token');
	var lite = require('lite');
}catch(e){
}

/**
 * @param loader
 * @param path
 * @param callback(path,realPath) this is the define
 */
function Define(loader,path,callback,source){
	this.loader = loader;
	this.path = path;
	var thiz = this;
	var path = path
	//console.warn('define load:',path);
	if(source){
		onload(source,path,'')
	}else{
		loader.load(path,onload);
	}
	function onload(source,realPath,file){
		//console.log(realPath,file)
		//console.warn('source:',source)
		thiz.source = source;
		thiz.file = file;
		thiz.realPath = realPath;
		thiz.parseDependence();
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
				//console.log('^^^',translatedPath,requireItem,realPath,file)
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
	var result = ["$JSI.define('",this.realPath,"',["];
	if(deps.length){
		result.push('"',deps.join('","'),'"')
	}
	result.push('],function(exports,require');
	var realPath = this.realPath;
	var hitfilename = false;
	if(/\b__(?:file|dir)name\b/.test(source)){
		result.push(',module,__filename');
		hitfilename = true;
	}else if(/\bmodule\b/.test(source)){
		result.push(',module');
	}
	result.push('){');
	//if(id!=realId){
	//	console.log("$$$$",id,realId,hitfilename,idIndex)
	//	}
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
	result.push(source,'\n});');
	//console.log('##complete',id)
	return result.join('');
}


Define.prototype.parseDependence = function(){
	var rtv = parseDependence(this.source,this.loader.bases[0].base,this.file);
	this.requireIndexes = rtv[0];
	this.tokens = rtv[1];
}
Define.parseDependence = function(source,root,file){
	var indexAndToken = parseDependence(source,root,file);
	var requireIndexes = indexAndToken[0];
	var tokens = indexAndToken[1];
	var depMap = {};
	var deps = []
	for(var i=0;i<requireIndexes.length;i++){
		var idx = requireIndexes[i];
		var requireItem = evalString(tokens[idx]);
		var translatedPath = loader.translatePath(requireItem,realPath,file)
		//console.log('^^^',translatedPath,requireItem,realPath,file)
		if(translatedPath in depMap){
			depMap[translatedPath] = true;
			deps.push(translatedPath)
		}
	}
	return deps;
}
function parseDependence(content,root,file){
	//var moduleName = id.replace(/\/.*/,'')
	//console.log("###",id,realId)
	var tokens = jstoken.partitionJavaScript(content);
	var requireIndexes = [];
	//console.log("###",id,realId)
	for(var i =0;i<tokens.length;i++){
		var item = tokens[i];
		switch(item.charAt()){
		case '\'':
		case '\"':
			//console.log(item);
			var prev = tokens[i-1];
			var next = tokens[i+1];
			if(prev && /^\s*[\)\+]/.test(next)){
				//var tpl1 = new XML("<div></div>");
				//function tpl2(a,b){return new XML("<div style='display:${a}'>${b}</div>");}
				var match = prev.match(/\b(?:(require)|\b(?:new\s+XML|liteXML))\s*\(\s*$/)
				if(match){
					var isRequire = match[1] == 'require'
					if(isRequire){
						requireIndexes.push(i)
						//console.log(item,deps)
					}else if(lite){//xml
						if(/^\s*\)/.test(tokens[i+1])){
							parseLite(item,prev);
						}
					}
				}
			}
		}
	}
	function parseLite(item,prev){
		var match = prev.match(/(?:(\([\w\s,]*\))\{\s*(?:return\s*)?)?(new\s+XML|liteXML)\s*\(\s*$/);
		var params = match[1];
		//console.log("###",match)
		prev = prev.slice(0,prev.length-match[0].length);
		var args = params&&params.replace(/[\s()]/g,'').split(',')||[];
		var fn = parseTemplate(root,file,evalString(item),{
			params:args,
		})
		tokens[i-1] = prev+String(fn).replace(/^\s+|\s+$/g,'');
		tokens[i] = '';
		tokens[i+1] = tokens[i].replace(/^\s*\)/,'')
	}
	return [requireIndexes,tokens]
}

function parseTemplate(root,file,xml,args){
	//console.log("parseTemplate:",root,file,xml)
	if(typeof xml == 'string'){
		var m = xml.match(/^([\w\-\/\.]+)(#.*)?$/)
		if(m){
			var attr = m[2];
			var buf =["<c:include path='",m[1],"' "];
			if(attr && attr.length>1){
				buf.push('selector="',attr.substr(1).replace(/["]/g,'&#34;'),'"/>')
			}else{
				buf.push('/>')
			}
			xml = buf.join('')
		}
		//console.log(xml)
		var parser = new (require('xmldom').DOMParser)({
				locator:{systemId:file||root},
				xmlns:{
					c:'http://www.xidea.org/lite/core',
					h:'http://www.xidea.org/lite/html-ext'
				}
			});
		xml = parser.parseFromString(xml,'text/html');
		//xml.root = root;
	}
	//console.log(xml.documentElement.attributes)
	return lite.parseLite(xml,args);
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