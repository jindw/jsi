var buildDefine = require('./define').buildDefine
var Define = require('./define').Define
var IncludeBuilder = require('./include').IncludeBuilder
var loadBuildInSource = require('./include').loadBuildInSource
var fs = require('fs');
var path = require('path');

/**
 * 为了不让模块路文件混淆， 不允许 root 目录下的js文件当模块引入（不能带__define__.js后缀）。
 * @params root 最近一个跟目录
 * @param loader
 */
function ScriptLoader(base,loader){
	console.error('base:',base)
	var mainModule =fs.realpathSync(base||'./').replace(/\\/g,'/');
	this.loader = loader || buildLoader(this);
	var dir = mainModule;
	var bases = this.bases = [];
	do{
		var baseName = path.basename(dir);
		var modulesDir = path.join(dir,'node_modules/')
		var config = {'base':dir+'/',moduleMap:{}};
		if(fs.existsSync(modulesDir)){
			loadDir(config,modulesDir)
		}else if(previousName){
			if(baseName != 'node_modules'){
				var pkg = path.join(dir,previousName,'package.json');
				//bug support
				if(fs.existsSync(pkg)){
					var main = require(pkg).main;
					var realFile = path.join(dir,previousName,main);
					config.moduleMap[previousName] = realFile;
				}
			}
		}else{
			console.log('load base file:',baseName,dir)
			if(baseName == 'assets' ){
				loadDir(config,dir.replace(/\/?$/,'/'))
			}
		}
		for(var n in config.moduleMap){
			bases.push(config)
			break;
		}
		var previousName = baseName
	}while((dir!=(dir = path.dirname(dir))))
}
function loadDir(config,modulesDir){
	var files = fs.readdirSync(modulesDir).filter(function(moduleName){
		try{
			var main = require(modulesDir+moduleName+'/package.json').main;
			var realFile = path.join(modulesDir,moduleName,main);
			//console.log(realFile)
			config.moduleMap[moduleName] = realFile;
		}catch(e){
		}
	});
}
/**
 * modulename/path.js
 * modulename2/node_modules/modulename3/path.js
 * modulename2/dir2/node_modules/modulename4/path.js
 */
ScriptLoader.prototype.translatePath = function (requirePath,currentPath,currentFile){
	//console.log('%%???%%',requirePath,currentPath,currentFile)
	if(/^[\.]/.test(requirePath)){//相对路径,当前模块
		var requirePath = absoluteModule(requirePath,currentPath);
		var moduleAndPath = this.findModuleAndPath(requirePath,currentFile);
		var modulePath = moduleAndPath[1]
		return modulePath;
	}else{//绝对路径，需要判断  requirePath 中是否有node_modules!
		var requireModuleAndPath =  this.findModuleAndPath(requirePath,currentFile);
		//console.log('@####'+requirePath,currentFile,requireModuleAndPath)
		if(!requireModuleAndPath){
			return requirePath;
		}
		var requireNodeModuleDir = requireModuleAndPath[0];
		var currentNodeModulesDir = currentFile.slice(0,-currentPath.length);
		
		var requireModulePath = requireModuleAndPath[1];
		var moduleName = requireModulePath.replace(/\/.*/,'');
		
		//node_modules 的父目录，base dir
		var requireModuleBaseDir = requireNodeModuleDir.replace(/\/node_modules\/$/,'/');
		var currentModuleBaseDir = currentNodeModulesDir.replace(/\/node_modules\/$/,'/');
		
		
		//console.error(moduleName,requireModuleDir,requirePath+currentModuleParentDir,[currentFile,currentPath])
		if(currentModuleBaseDir.indexOf(requireModuleBaseDir) ==0){//与当前模块同目录或者跳出了当前模块
			return requirePath;
		}else if(requireModuleBaseDir.indexOf(currentModuleBaseDir) == 0){//是当前模块的子模块
			//console.warn(requireModuleDir.substr(currentNodeModulesDir.length)+requirePath)
			return requireNodeModuleDir.substr(currentNodeModulesDir.length)+requireModulePath;
		}else{
			throw new Error('invalid require path:'+requireNodeModuleDir+'!='+currentNodeModuleDir);
		}
	}
}
/**
 * [/Users/..../node_modules/xpath.js/ xpath.js]
 */
ScriptLoader.prototype.findModuleAndPath = function(id,baseFile){
	if(/^(fs|path|process|child_process|util)$/.test(id)){return ['',id];}
	if(id.match(/^node_modules/)){
		throw new Error('')
	}
	//console.warn('findModuleAndPath:',id,baseFile)
	var bases = this.bases;
	var dir = baseFile?baseFile:bases[0].base;
	var fileMatchs = id.match(/^(\w[\w\.\-]*?)(\/.*)?$/);
	var moduleName = fileMatchs[1];//require('xpath.js'),require('xpath.js/ddd')
	var moduleSubPath =fileMatchs[2];
	//console.log(dir,baseFile)
	do{
		for(var i=0;i<bases.length;i++){
			var config = bases[i];
			//console.log('ID:'+i,id,dir,config.base)
			if(dir.indexOf(config.base)==0){
				var main = config.moduleMap[moduleName];
				//console.error('###',config.base,moduleName,main,moduleSubPath)
				if(main){
					var mainPath = main.substr(config.base.length).replace(/^node_modules\//,'');
					if(!moduleSubPath){
						//console.log('@#@#11##', [main.slice(0,-mainPath.length),mainPath])
						return [main.slice(0,-mainPath.length),mainPath];
					}else{
						var m = mainPath.match(/^([\w\-\.]+\/)(.*)/)
						//console.log(mainPath,m)
						var nodeModuleDir = main.slice(0,-mainPath.length);
						
						var file = nodeModuleDir+id
						var files = [file,file+'.js'];
						if(/\.js/.test(file)){
							files.reverse();
						}
						//console.log(fs.existsSync(files[0]),fs.existsSync(files[1]),files)
						for(var i=0;i<files.length;i++){
							file = files[i];
							if(fs.existsSync(file)){
								//console.log('@#@#22##',config.base,[config.base,file.substr(config.base.length)])
								return [nodeModuleDir,file.substr(nodeModuleDir.length)]
							}
						}
						throw new Error('module file not found:'+moduleSubPath+'@'+moduleName+' '+files)
					}
					
				}
				
			}
		}
	}while((dir!=(dir = dir.replace(/[\w\-.]+\/?$/,''))))

}
/**
 * @param path node module 对应文件路径（带扩展名(.js || __define__.js)，无前置'/'）
 */
ScriptLoader.prototype.load = function(path,callback){
	//console.log(path)
	path = path.replace(/^\//,'');
	var sourcePath = path.replace(/([^\/]+)__define__\.js$/,'$1.js')//clean: __define__
	var isDefine = sourcePath != path;
	//console.warn('load:::',path,sourcePath)
	var loader = this;
	this.loadFile(sourcePath,function(text,path,file){
		if(isDefine){
			var moduleAndPath = loader.findModuleAndPath(path);
			console.warn('load#:::',path,moduleAndPath,loader.bases[0].base,loader.bases[loader.bases.length-1].base)
			text = new Define(loader,path,function(){
				callback(this.toString(),path,file);
			},text);
		}else{
			callback(text,path,file);
		}
	})
}
ScriptLoader.prototype.loadFile = function(path,callback){
	this.loader(path,function(text,path,file){
		text = text.replace(/^\ufeff/,'')
		new IncludeBuilder(text,function(text,file2){
			callback(text,path,file);
		},file);
	})	
}

function buildLoader(loader){
	/**
	 * @param path node module 对应文件路径（带扩展名(.js),无__define__段，无前置'/'）
	 * @param callback(data,path,file)
	 */
	return function(path,callback){
		//console.log("###",path)
		if(!path || path == 'undefined.js'){
			throw new Error();
		}
		var moduleAndPath = loader.findModuleAndPath(path);
		//console.log('@@%@',path,moduleAndPath,loader.bases[0].base)
		if(moduleAndPath){
			path = moduleAndPath[1];
			var file = moduleAndPath.join('');
			fs.readFile(file,function(err,data){
				if(err){
					//console.error('load file err:',file,err);
					callback('console.log('+JSON.stringify("read module err!!!"+path+'\n'+err)+')',path,file)
				}else{
					//console.log("@@@",file,path,data.toString())
					callback(data.toString(),path,file)
				}
			});
		}else if(!loadBuildInSource(path,callback)){
			//console.error('load inline file err:',path,moduleAndPath);
			//throw new Error();
			callback('console.log('+JSON.stringify("read module err!!!"+path+'\n')+')',path,file)
		}
	}
				//console.log('load file err:',file,err)
			
}

function xx(){
	if(modulePath){
		if(modulePath.substr(0,root.length) == root){
			var relativeRootPath = modulePath.substr(root.length)
			var m = relativeRootPath.match(/^([\w\.\-]+)(\/.*)$/);
			//console.log(module,path)
			var relativeRootId = relativeRootPath.replace(/\.js$/,'');
			if( relativeRootPath != path
				 && m && m[2] && m[1] != 'node_modules'){//子模块引用，如果是子模块，需要一次全导出处理？
				
				return;	
					
				if(idIndex){//merge outer
					//continue!!
				}else if(moduleSubPath == '.js' || !moduleSubPath  ){//index module
					//inline module export!
					var exports = require('./exports');
					exports.export4web(loader.root,[relativeRootId],function(impls,idIndex,internalDeps,externalDeps){
						var source = impls;//.join(',')
						//console.log('export:',module2,impls.length)
						callback(file,relativeRootPath,source)
					})
					
					return;
				}else if(moduleSubPath == m[2]){//sub module
					//return;
				}
			}
			var dir = root + relativeRootId.replace(/^((?:node_modules\/)?[\w\.\-]+)\/?.*$/,'$1');
		}else{
			var dir = modulePath.replace(/((?:^|.*\/)node_modules\/[\w\.\-]+)\/?.*$/,'$1');
		}
		var realPath = moduleSubPath.substr(dir.length);
		var file = dir + (moduleSubPath ||  moduleSubPath.substr(dir.length)).replace(/^\/?(?=.)/,'/')
		loader.mainMap[moduleName] =moduleName+ realPath.replace(/\.js$/,'');
		//console.log('^^^realid:',root,',',module,',',dir,'|',moduleName,',',realPath)
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

exports.ScriptLoader = ScriptLoader;
