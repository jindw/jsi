var buildDefine = require('./define').buildDefine
var IncludeBuilder = require('./merge').IncludeBuilder
var fs = require('fs');
var path = require('path');


function ScriptLoader(root,loader){
	root =fs.realpathSync(root||'./').replace(/\\/g,'/').replace(/\/?$/,'/');
	this.root = root;
	this.loader = loader || buildLoader(this);
	this.mainMap = {};
	
	var paths = [root+'node_modules',root];
	var dir = root;
	
	while((dir!=(dir = path.dirname(dir))) && fs.existsSync(dir)){
		if(fs.existsSync(path.join(dir,'node_modules'))){
			if(paths.indexOf(dir)>=0){
				break;
			}
			paths.push(dir);
		}
	}
	this.paths = paths.concat(module.paths)
}
ScriptLoader.prototype.findPath = function(id){
	var findPath = require('module')._findPath;
	if(findPath){
		file = findPath(id,this.paths)
	}
	//console.log(this.paths)
	var file = file || require.resolve(id).replace(/\\/g,'/');
	return file;
}
ScriptLoader.prototype.load = function(path,callback,idIndex){
	//console.log(path)
	
	path = path.replace(/^\//,'');
	var sourcePath = path.replace(/(\/[^\/]+)__define__\.js$/,'$1.js')//replace module file
		.replace(/([^\/]+)__define__\.js$/,'$1.js');;//replace module name
	//console.log(path,sourcePath)
	var isDefine = sourcePath != path;
	var loader = this.loader;
	var mainMap = this.mainMap;
	var root = this.root;
	var t = (new Date()*1 & 0xFF).toString(16)
	//console.log("@@@@",t,sourcePath)
	loader(sourcePath,function(file,realPath,text){
		//console.log('script loader:',t,sourcePath,realPath,file)
		
		//console.log(sourcePath,realPath)
		var rawId = sourcePath.replace(/\.js$/,'');
		var realId = realPath.replace(/\.js$/,'');
		//console.log(sourcePath,realPath,rawId,realId)
		if(text instanceof Array){
			if(isDefine){
				if(rawId!=realId){
						mainMap[rawId] = realId;
					};
					callback( buildDefine(root,file,rawId,realId,text,idIndex,mainMap));
				}else{
					callback( text);
				}
			}else{
			text = text.replace(/^\ufeff/,'')
			if(isDefine){//预定义里面就不需要包含require.js了
				text = text.replace(/^\/\/#include\(['"][\.\/]*require\.js['"]\)/m,'//')
			}
			var text = new IncludeBuilder(realPath,text,loader,function(){
				if(isDefine){
					if(rawId!=realId){
						mainMap[rawId] = realId;
					};
					callback( buildDefine(root,file,rawId,realId,text,idIndex,mainMap));
				}else{
					callback( text);
				}
			});
		}
	},idIndex)	
}


function buildLoader(loader){
	return function(path,callback,idIndex){
		var file = require('path').join(loader.root,path)
		//console.log("###",path,file)
		fs.readFile(file,function(err,data){
			if(err){
				//console.log('load file err:',file,err)
				restoreFromErr(loader,file,path,callback,idIndex);
			}else{
				//console.log("@@@",file,path)
				callback(file,path,data.toString())
			}
		});
	}
}
var jquery_1_9_1;
function restoreFromErr(loader,file,path,callback,idIndex){
	var root = loader.root;
	if(loadBuildInSource(file,path,callback)){
		return;
	}
	//var id = path.replace(/(.\/.+)\.js$/,'$1');
	
	//if((/\.css(?:[#?].*)?$/.test(id) )
	var subFileMatchs = path.match(/^([\w\.\-]+?)(?:\.js|(\/.*))?$/);//		module, module/files
	if(subFileMatchs){
		try{
			var module = loader.findPath(subFileMatchs[1]);
		}catch(e){
			//console.log(subFileMatchs)
			if(/^jquery$/i.test(subFileMatchs[1])){
				if(!jquery_1_9_1){
					jquery_1_9_1 = fs.readFileSync(require.resolve('../assets/test/jquery_1_9_1.js')).toString()
					console.log('jquery module not found!!',path,
						"\n\t\tuse jquery1.9.1 instead!")
				}
				callback(file,path,jquery_1_9_1)
			}else{
				console.error('module not found!!',subFileMatchs[1])
				callback(file,path,"module not found!!"+path);
			}
			return;
		}
		if(module){
			module = module.replace(/\\/g,'/');
			if(module.substr(0,root.length) == root){
				module = module.substr(root.length)
				var m = module.match(/^([\w\.\-]+)(\/.*)$/);
				//console.log(module,path)
				
				
				if(module != path && m && m[2] && m[1] != 'node_modules' && 
					(
						(subFileMatchs[2] == '.js' || !subFileMatchs[2]  )//index module
						||(subFileMatchs[2] == m[2])//sub module
					 )
					){
					//inline module export!
					var exports = require('./exports');
					//console.log(module)
					exports.export4web(loader.root,[module],function(impls,idIndex,internalDeps,externalDeps){
						var source = impls;//.join(',')
						//console.log(2)
						callback(file,module,source)
					})
					
					return;
				}
			}
			var dir = module.replace(/((?:^|.*\/)node_modules\/[\w\.\-]+)\/?.*$/,'$1');
			var moduleName = subFileMatchs[1];
			var modulePath = subFileMatchs[2];
			var realPath = module.substr(dir.length);
			var file = dir + (modulePath ||  module.substr(dir.length))
			loader.mainMap[moduleName] =moduleName+ realPath.replace(/\.js$/,'');
			
			
			//console.log(moduleName,modulePath,path,dir,'$$$',realPath)
			fs.readFile(file,function(err,data){
				if(err){
					console.error(err)
					callback(file,moduleName+realPath,JSON.stringify(String(err)))
				}else{
					callback(file,moduleName+realPath,data.toString('utf-8'))
				}
			});
		}else{
			callback(file,path,"console.error('file not found:"+path+"')");
		}
	}else{
		//console.log("@@@",file,path)
		callback(file,path,"console.error('illage path:"+path+"')");
	}
}

function loadBuildInSource(file,path,callback){
	switch(path){
	case 'lite-buildin.js':
	case 'require.js':
	case 'console.js':
	case 'boot.js':
	case 'block.js':
		try{
			var file = require.resolve('../assets/'+path);
			fs.readFile(file,function(err,data){
				if(err){
					callback(file,path,err)
				}else{
					callback(file,path,data.toString())
				}
			})
		}catch(e){
			callback(file,path,e)
		}
		return true;
	case 'config/main.js':
		callback(file,path,'$JSI.init({})')
		return true;
	}
}
exports.ScriptLoader = ScriptLoader;
