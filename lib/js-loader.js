var buildDefine = require('./define').buildDefine
var Define = require('./define').Define
var IncludeBuilder = require('./include').IncludeBuilder
var loadBuildInSource = require('./include').loadBuildInSource
var fs = require('fs');
var path = require('path');


var compressJS = require('./js-process.js').compressJS
var formatJS = require('./js-process.js').formatJS


function transformCode(source,path,format){
	try{
	switch(format){
	case 'f':
	case 'format':
		return formatJS(compressJS(source,path),path);
	case 'compress':
	case 'c':
	case 'compressed':
		return compressJS(source,path);
	case 'r':
	case 'raw':
		return source;
	default:
		if(format){
			console.warn('unsupport format type:'+format+':'+format+'; only support:format,compressed,raw')
		}
		return source;
	}
	}catch(e){
		//console.log(source)
		throw new Error(e);
	}
	
}

/**
 * 为了不让模块路文件混淆， 不允许 root 目录下的js文件当模块引入（不能带__define__.js后缀）。
 * @params root 最近一个跟目录
 * @param loader
 */
function ScriptLoader(base,async,loader){
	this.async = async;
	var base =fs.realpathSync(base||'./').replace(/\\|\/?$/g,'/');
	//console.error('base:',mainModule)
	this.loader = loader || buildLoader(this,async);
	this.base = base;
	var dir = base.replace(/\/$/,'');
	var bases = this.bases = [];
	
	do{
		var baseName = path.basename(dir);
		var modulesDir = path.join(dir,'node_modules/')
		var config = {'base':dir+'/',moduleMap:{}};
		//console.log(dir,modulesDir)
		if(previousName == null){
			loadDir(config,base,true)
		}
		if(fs.existsSync(modulesDir)){
			loadDir(config,modulesDir)
		}else if(previousName){
			if(baseName != 'node_modules'){
				var pkg = path.join(dir,previousName,'package.json');
				//bug support
				if(fs.existsSync(pkg)){
					var main = require(pkg).main;
					var realFile = path.join(dir,previousName,main);
					if(!/\.js/i.test(realFile) &&!fs.existsSync(realFile)&&fs.existsSync(realFile+'.js')){
						//console.log('realFile redirect:',realFile)
						realFile = realFile+'.js'
					}
					config.moduleMap[previousName] = realFile;
				}
			}
		}else{
			//console.log('load base file:',baseName,dir)
			//if(baseName == 'assets' ){
			//	loadDir(config,dir.replace(/\/?$/,'/'))
			//}
		}
		for(var n in config.moduleMap){
			bases.push(config)
			break;
		}
		var previousName = baseName
	}while((dir!=(dir = path.dirname(dir))))
	//console.log(dir,bases)
}
function loadDir(config,modulesDir,isBasePath){
	var files = fs.readdirSync(modulesDir).filter(function(moduleName){
		var mainPath = null;
		var realFile = path.join(modulesDir,moduleName);
		var stat = fs.statSync(realFile);
		if(isBasePath){
			if(stat.isDirectory()){
				try{
					var main = require(modulesDir+moduleName+'/package.json').main;
					var realFile = path.join(modulesDir,moduleName,main);
					mainPath = realFile;
				}catch(e){
					//no default
					mainPath= path.join(modulesDir,moduleName,'undefined');
				}
				
			}else if(stat.isFile()){
				moduleName = moduleName.replace(/\.js$/,'');
				mainPath = realFile;
			}
		}else if(stat.isDirectory()){
			try{
				var main = require(modulesDir+moduleName+'/package.json').main;
				var realFile = path.join(modulesDir,moduleName,main);
				mainPath = realFile;
			}catch(e){
				//console.error(modulesDir,moduleName,e)
			}
			//console.log(realFile)
			
		}
		
		if(mainPath && !(moduleName in config.moduleMap)){
			if(!/\.js/i.test(mainPath) &&!fs.existsSync(mainPath)&&fs.existsSync(mainPath+'.js')){
				//console.log('realFile redirect:',mainPath)
				mainPath = mainPath+'.js'
			}
			config.moduleMap[moduleName] = mainPath;
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
		//console.log(requirePath,currentPath)
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
	
	var bases = this.bases;
	//console.warn('findModuleAndPath:',id,baseFile,file)
	if(id.charAt() == '.'){
		var file = absoluteModule(id,baseFile);
		for(var i = 0;i<bases.length;i++){
			var config = bases[i];
			//console.log('ID:'+i,id,dir,config.base)
			if(file.indexOf(config.base)==0){
				baseFile = config.base;
				id = file.substr(baseFile.length).replace(/^\/?node_modules\//,'');
				//console.log(id,baseFile)
				break;
			}
		}
	}
	//console.warn('findModuleAndPath2:',id,baseFile,file)
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
				if(i ==0 && main == null){
					var fileModule = moduleName.replace(/\.js$/,'')
					if(fileModule!=moduleName && fileModule in config.moduleMap){
						main = config.moduleMap[moduleName = fileModule];
					}
				}
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
							if(file.match(/\.\w+\.js$/)){
								files.push(file.slice(0,-3));
							}
							
						}
						//console.error(files)
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
ScriptLoader.prototype.load = function(path,callback,format){
	//console.log(path)
	path = path.replace(/^\//,'');
	var match = path.match(/(.+?)__(\w+)__\.js$/)//clean: __define__
	var exportType = match && match[2];
	var sourcePath = match?match[1]+'.js':path;
	//console.warn('load:::',path,sourcePath,format)
	var loader = this;
	this.loadFile(sourcePath,function(text,path,file,deps){
		if(exportType == 'define'){
			//var moduleAndPath = loader.findModuleAndPath(path);
			//console.warn('load#:::',path,moduleAndPath,loader.bases[0].base,loader.bases[loader.bases.length-1].base)
			new Define(loader,path,function(){
				text = this.toString();
				text = transformCode(text,file,format);
				callback(text,path,file);
			},text,file);
		}else if(exportType == 'export'){
			var moduleAndPath = loader.findModuleAndPath(path);
			//console.log(moduleAndPath)
			require('./exports').exportScript(moduleAndPath[0],[moduleAndPath[1]],function(text){
				callback(text);
			},format)
		}else{
			new Define(loader,path,function(){
				text = this.toString().replace(/^[^{]+\{|\}\s*\)\s*;?\s*$/g,'');
				text = transformCode(text,file,format);
				callback(text,path,file);
			},text,file);
			//callback(transformCode(text,file,format),path,file);
		}
	})
}
ScriptLoader.prototype.loadFile = function(path,callback){
	var async = this.async;
	this.loader(path,function(text,path,file){
		text = text.replace(/^\ufeff/,'')
		//console.log('load file:',path)
		function onLoad(text){
			callback(text,path,file,builder.deps);
		}
		if(async){
			var builder = new IncludeBuilder(text,file,onLoad)
		}else{
			var builder = new IncludeBuilder(text,file)
			onLoad(builder.toString());
		}
	})
}

function buildLoader(loader,async){
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
			function fileLoaded(err,data){
				if(err){
					//console.error('load file err:',file,err);
					callback('console.log('+JSON.stringify("read module err!!!"+path+'\n'+err)+')',path,file)
				}else{
					//console.log("@@@",file,path,data.toString())
					callback(data.toString(),path,file)
				}
			}
			if(async){
				fs.readFile(file,fileLoaded);
			}else{
				try{
					var data = fs.readFileSync(file);
				}catch(e){
					var err = e;
				}
				fileLoaded(err,data)
			}
			
		}else if(!loadBuildInSource(path,callback)){
			//console.error('load inline file err:',path,moduleAndPath);
			//throw new Error();
			callback('console.log('+JSON.stringify("read module err!!!"+path+'\n')+')',path,file)
		}
	}
			
}


function absoluteModule(url,parentModule){
	//console.log('absModule:',url,'|',parentModule)
	if(url.charAt(0) == '.'){
		url = url.replace(/\.js$/i,'');
		url = parentModule.replace(/([^\/]*)?$/,'')+url
		while(url != (url =url.replace( /(\/)([^\/]+\/\.)?\.\//,'$1')));
	}
	//console.log(url)
	return url;
}

exports.transformCode = transformCode;
exports.ScriptLoader = ScriptLoader;
