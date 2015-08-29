var buildDefine = require('./define').buildDefine
var IncludeBuilder = require('./merge').IncludeBuilder
var fs = require('fs');


function ScriptLoader(root,loader){
	root =(root||'./').replace(/\\/g,'/').replace(/\/?$/,'/');
	this.root = root;
	this.loader = loader || buildLoader(this);
	this.mainMap = {};
	
	var paths = [];
	var path = root+'node_modules';
	while(path != (path =path.replace(/([^\:])\/[^\/]+\/node_modules$/,'$1/node_modules'))){
		if(module.paths.indexOf(path)>=0){
			break;
		}
		paths.push(path);
	}
	//console.log(paths);
	this.paths = paths.concat(module.paths)
}
ScriptLoader.prototype.findPath = function(id){
	var findPath = require('module')._findPath;
	if(findPath){
		var file = findPath(id,this.paths)
	}
	var file = file || require.resolve(id).replace(/\\/g,'/');
	return file;
}
ScriptLoader.prototype.load = function(path,callback,idIndex){
	//console.log(path)
	
	path = path.replace(/^\//,'');
	var sourcePath = path.replace(/^([^\/]+)__define__\.js$/,'$1').replace(/(\/[^\/]+)__define__\.js$/,'$1.js');;//.replace(/^([^\/\\]+)\.js$/,'$1');
	//console.log(path,sourcePath)
	var isDefine = sourcePath != path;
	var loader = this.loader;
	var mainMap = this.mainMap;
	
	loader(sourcePath,function(refPath,text){
		text = text.replace(/^\ufeff/,'')
		var text = new IncludeBuilder(refPath,text,loader,function(){
			if(isDefine){
				var rawId = sourcePath.replace(/(.\/.+)\.js$/,'$1');
				var refId = refPath.replace(/(.\/.+)\.js$/,'$1');
				if(rawId!=refId){
					mainMap[rawId] = refId;
				};
				callback( buildDefine(rawId,text,idIndex,mainMap));
			}else{
				callback( text);
			}
		});
	},idIndex)	
}


function buildLoader(loader){
	var root = loader.root;
	return function(path,callback,idIndex){
		fs.readFile(root+path,function(err,data){
			if(err){
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
								callback(path,err)
							}else{
								callback(path,data.toString())
							}
						})
						return;
					}catch(e){
						callback(path,e)
						return ;
					}
				}
				var id = path.replace(/(.\/.+)\.js$/,'$1');
				var matchs = id.match(/^[\w\.\-]+(\/.*)?$/);//		module, module/files
				try{
					var file = loader.findPath(id)
				}catch(e){
					matchs = null;
					console.log('file not found!!',file)
				}
				if(matchs){
					//load from node module system!
					
					if(matchs[1]){//path
						fs.readFile(file,function(err,data){
							if(err){
								callback(path,err)
							}else{
								callback(path,data.toString('utf-8'))
							}
						})
					}else{
						//compute relative path
						var p = file.lastIndexOf('/node_modules/');
						var path2 = file.substr(file.indexOf('/',p+2)+1);
						if(idIndex){
							fs.readFile(file,function(err,data){
								if(err){
									callback(path2,err)
								}else{
									callback(path2,data.toString('utf-8'))
								}
							})
						}else{
							var refId = path2.replace(/(.\/.+)\.js$/,'$1');
							callback(path,"var o = require('"+refId+"');for(var n in o){exports[n] = o[n]};module.exports=o;")
						}
					}
				}else{
					callback(path,"console.error('illage path:"+path+"')");
				}
				
			}else{
				callback(path,data.toString())
			}
		});
	}
}
exports.ScriptLoader = ScriptLoader;
