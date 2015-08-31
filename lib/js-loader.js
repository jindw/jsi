var buildDefine = require('./define').buildDefine
var IncludeBuilder = require('./merge').IncludeBuilder
var fs = require('fs');
var path = require('path');


function ScriptLoader(root,loader){
	root =(root||'./').replace(/\\/g,'/').replace(/\/?$/,'/');
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
				case 'config/main.js':
					callback(path,'$JSI.init({})')
					return;
				}
				var id = path.replace(/(.\/.+)\.js$/,'$1');
				var matchs = id.match(/^[\w\.\-]+(\/.*)?$/);//		module, module/files
				try{
					var file = require('path').join(root,id)
					
					//console.log("loader:"+file)
					if(!(/\.css(?:[#?].*)?$/.test(id) && fs.existsSync(file))){
						var file = loader.findPath(id)
					}
				}catch(e){
					console.log('file not found!!',id,e)
				}
				if(matchs){
					//load from node module system!
					
					if(matchs[1]){//path
						fs.readFile(file,function(err,data){
							if(err){
								console.error(err)
								callback(path,JSON.stringify(String(err)))
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
									console.error(err)
									callback(path2,JSON.stringify(String(err)))
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
