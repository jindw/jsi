var buildDefine = require('./define').buildDefine
var Define = require('./define').Define
var IncludeBuilder = require('./include').IncludeBuilder
var loadBuildInSource = require('./include').loadBuildInSource
var fs = require('fs');
var path = require('path');


var compressJS = require('./js-process').compressJS
var formatJS = require('./js-process').formatJS


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
function ScriptLoader(base,opt){
	var async = this.async = !! opt.async;
	var loader = opt.loader;
	var sources = opt.sources;


	if(!fs.existsSync(base)){
		base =  path.dirname(base);
	}
	var base =fs.realpathSync(base||'./').replace(/\\|\/?$/g,'/');
	
	this.loader = loader || buildLoader(this,async);
	this.base = base;
	var dir = base.replace(/\/$/,'');
	var bases = this.bases = [];
	for(var s in sources){

	}
	
	do{
		var baseName = path.basename(dir);
		var modulesDir = path.join(dir,'node_modules/')
		var config = {'base':dir+'/',moduleMap:{}};
		//console.log(dir,modulesDir)
		if(dir && dir.length>1){//previousName == null){
			loadDirSync(config,dir,previousName == null )
		}
		if(fs.existsSync(modulesDir)){
			loadDirSync(config,modulesDir)
		}else if(previousName){
			if(baseName != 'node_modules'){
				var pkg = path.join(dir,previousName,'package.json');
				//bug support
				if(fs.existsSync(pkg)){
					var main = require(pkg).main;
					var realFile = path.join(dir,previousName,main);
					if(!/\.[jt]s/i.test(realFile) &&!fs.existsSync(realFile)){
						if(fs.existsSync(realFile+'.js')){
							realFile = realFile+'.js'
						}else if(fs.existsSync(realFile+'.ts')){
							realFile = realFile+'.ts'
						}
						//console.log('realFile redirect:',realFile)
						
					}
					config.moduleMap[previousName] = realFile;
				}
			}
		}else{
			//console.log('load base file:',baseName,dir)
			// if(baseName == 'assets' ){
			// 	loadDirSync(config,dir.replace(/\/?$/,'/'))
			// }
		}
		for(var n in config.moduleMap){
			bases.push(config)
			break;
		}
		var previousName = baseName
		// console.log(dir,bases)
	}while((dir!=(dir = path.dirname(dir))))

}
function firstExistsFile(){
	for(var i=0;i<arguments.length;i++){
		if(fs.existsSync(arguments[i])){
			return arguments[i];
		}
	}
}
// var inc = 0;
function loadDirSync(config,modulesDir,isBasePath){
	// if(inc++>11){
	// 	throw new Error();
	// }
	//console.log('^^',modulesDir)
	var files = fs.readdirSync(modulesDir).filter(function(moduleName){
		var mainPath = null;
		var moduleDir = path.join(modulesDir,moduleName);
		var stat = fs.statSync(moduleDir);
		if(isBasePath ){
			if(stat.isDirectory()){
				try{
					var pkg = require(moduleDir+'/package.json');
					var main = pkg.main;
				}catch(e){
				}
				if(!main){
					main = firstExistsFile(['js','ts'].map(function(ext){return moduleDir+'/index.'+ext}));
				}
				if(main){
					mainPath = path.join(modulesDir,moduleName,main);
				}else{
					//no default, give flag, report later
					mainPath= path.join(modulesDir,moduleName,'undefined');
				}

				if(!pkg ){
					return;
				}else{
					//console.log(modulesDir,moduleName,pkg.name)
					moduleName = pkg.name;
				}
			}else if(stat.isFile()){
				// mainPath = path.join(modulesDir,moduleName);
				// moduleName = moduleName.replace(/\.js$/,'');
			}
		}else if(stat.isDirectory()){
			try{
				var pkg = require(modulesDir+'/'+moduleName+'/package.json');
				var main = pkg.main;
				mainPath = path.join(modulesDir,moduleName,main);
				moduleName = pkg.name;
			}catch(e){
				//console.error(modulesDir,moduleName,e)
			}

			if(!pkg ){
				return;
			}else{
				// console.log('##22#',modulesDir,moduleName,pkg.name)
			}
			// console.error(moduleName)
			//console.log(realFile)
			
		}
		
		if(mainPath && !(moduleName in config.moduleMap)){
			if(!/\.js/i.test(mainPath) &&!fs.existsSync(mainPath)){
				if(fs.existsSync(mainPath+'.js')){
					mainPath = mainPath+'.js'
				}else if(fs.existsSync(mainPath+'.ts')){
					mainPath = mainPath+'.ts'
				}
			}
			config.moduleMap[moduleName] = mainPath.replace(/\\/g,'/');
		}
	});
	//console.log('$$',config.moduleMap)
}
/**
 * modulename/path.js
 * modulename2/node_modules/modulename3/path.js
 * modulename2/dir2/node_modules/modulename4/path.js
 */
ScriptLoader.prototype.translatePath = function (requirePath,currentPath,currentFile){
	requirePath = requirePath.replace(/\\/g,'/')
	currentPath = currentPath.replace(/\\/g,'/')
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
			throw new Error('invalid require path:'+requireNodeModuleDir+'!='+currentModuleBaseDir);
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
	//console.warn('findModuleAndPath:',id,baseFile)
	//WHY?
	if(id.charAt() == '.'){
		var absModuleId = absoluteModule(id,baseFile);
		for(var i = 0;i<bases.length;i++){
			var config = bases[i];
			//console.log('ID:'+i,id,dir,config.base)
			if(absModuleId.indexOf(config.base)==0){
				baseFile = config.base;
				id = absModuleId.substr(baseFile.length).replace(/^\/?node_modules\//,'');
				//console.log(id,baseFile)
				break;
			}
		}
	}
	//console.warn('findModuleAndPath2:',id,baseFile)
	var dir = baseFile?baseFile:bases[0].base;
	var fileMatchs = id.match(/^(\w[\w\.\-]*?)(\/.*)?$/);
	var moduleName = fileMatchs[1];//require('xpath.js'),require('xpath.js/ddd')
	var moduleSubPath =fileMatchs[2];
	//console.log(moduleName,dir)
	do{
		for(var i=0;i<bases.length;i++){
			var config = bases[i];
			//console.log('ID:'+i,id,dir,config.base)
			if(dir.indexOf(config.base)==0){
				//console.log(config)
				var main = config.moduleMap[moduleName];
				if( main == null){
					var fileModule = moduleName.replace(/\.js$/,'')
					if(fileModule!=moduleName && fileModule in config.moduleMap){
						main = config.moduleMap[moduleName = fileModule];
					}
				}
				//console.error('###',config.base,moduleName,main,moduleSubPath)
				if(main){
					//console.log(main)
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
		try{
			if(fs.existsSync(dir)){
				break;
			}
		}catch(e){}
		
	}while((dir!=(dir = dir.replace(/[\w\-.]+\/?$/,''))))
	

}
/**
 * @param path node module 对应文件路径（带扩展名(.js || __define__.js)，无前置'/'）
 */
ScriptLoader.prototype.load = function(path,callback,format){
	//console.log('load::',path)
	path = path.replace(/^\//,'');
	var match = path.match(/(.+?)__(\w+)__\.js$/)//clean: __define__
	var exportType = match && match[2];
	var sourcePath = match?match[1]+'.js':path;
	//console.warn('load:::',path,sourcePath,format)
	var loader = this;
	this.loadFile(sourcePath,function(text,path2,file,deps){
		if(exportType == 'define'){
			//var moduleAndPath = loader.findModuleAndPath(path);
			//console.warn('load#:::',path,path2,file,loader.bases[0].base,loader.bases[loader.bases.length-1].base)
			new Define(loader,path2,function(){
				text = this.toString();
				if(path.indexOf('/')<0 && path2.indexOf('/')>0){
					var rawName = path.replace(/__define__\.js$/,'');
					var relId = path2.replace(/\.c?js$/,'');
					text = "(function(distName,deps,impl){"
					+"$JSI.define({'"+relId+"':[impl]}),"
					//+"map[distName]=[impl].concat(deps);"
					+"$JSI.define('"+rawName+"',deps,function(e,r,m){m.exports = r('"+relId+"')})"
						+"})"
					+text.replace("$JSI.define",'');
				}
				//console.log('####',path,path2,file)
				text = transformCode(text,file,format);
				callback(text,path2,file);
			},text,file);
		}else if(exportType == 'export'){
			var moduleAndPath = loader.findModuleAndPath(path);
			//console.log(moduleAndPath)
			require('./exports').exportScript(moduleAndPath[0],[moduleAndPath[1]],function(text){
				callback(text);
			},format)
		}else{
			new Define(loader,path2,function(){
				text = this.toString().replace(/^[^{]+\{|\}\s*\)\s*;?\s*$/g,'');
				text = transformCode(text,file,format);
				callback(text,path2,file);
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
	//console.log('!!!!!!')
	/**
	 * @param path node module 对应文件路径（带扩展名(.js),无__define__段，无前置'/'）
	 * @param callback(data,path,file)
	 */
	return function(path,callback){
		//console.log("###",path)
		if(!path || path == 'undefined.js'){
			throw new Error();
		}
		try{
			var moduleAndPath = loader.findModuleAndPath(path);
		}catch(e){
			console.error('load failed:'+path)
			throw e;
		}
		//console.log('@@%@',path,moduleAndPath,loader.bases[0].base)
		if(moduleAndPath){
			path = moduleAndPath[1];
			var file = moduleAndPath.join('');
			//console.log('000!',file)
			function fileLoaded(err,data){
				//console.log("@@@111",file,path)
				if(err){
					//console.error('load file err:',file,err);
					callback('console.log('+JSON.stringify("read module err!!!"+path+'\n'+err)+')',path,file)
				}else{
					//console.log("@@@22",file,path)
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
				fileLoaded(err,data);

			}
			
		}else if(!loadBuildInSource(path,callback)){
			console.error('load inline file err:',path,moduleAndPath);
			//throw new Error();
			callback('console.log('+JSON.stringify("read module err!!!"+path+'\n')+')',path,file)
		}
	}
			
}


function absoluteModule(moduleId,parentModule){
	//console.log('absModule:',moduleId,'|',parentModule)
	if(moduleId.charAt(0) == '.'){
		moduleId = moduleId.replace(/\.js$/i,'');
		moduleId = parentModule.replace(/([^\/]*)?$/,'')+moduleId
		while(moduleId != (moduleId =moduleId.replace( /(\/)([^\/]+\/\.)?\.\//,'$1')));
	}
	//console.log(url)
	return moduleId;
}

exports.transformCode = transformCode;
exports.ScriptLoader = ScriptLoader;
