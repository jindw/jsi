var path = require('path')
var fs = require('fs')
var exportExample = fs.readFileSync(require.resolve('../assets/resource/code-example-export.js')).toString()
	.replace(/^.*?(function[\s\S]+?\})\s*\(\s*function\([\s\S]+$/,"$1");
var exportRequireExample = fs.readFileSync(require.resolve('../assets/resource/code-example-export-require.js')).toString();
var ScriptLoader = require('./js-loader').ScriptLoader;
var Define = require('./define').Define;
var transformCode = require('./js-loader').transformCode
function formatOptions(options){
	var formats='r,f,c,raw,format,compressed';
	options = options||{};
	if(typeof options == 'string'){
		if(formats.split(',').indexOf(options)>=0){
			options = {format:options}
		}else{
			console.error('invalid format:'+options+'; supported format:'+formats)
		}
	}else if(formats.split(',').indexOf(options.format)<0){
		console.error('invalid format2:'+options.format+'; supported format:'+formats)
		//throw new Error();
	}
	return options;
}
/**
 * @param options{format:[raw,format,compressed]}
 */
function exportScript(base,paths,onComplete,options){
	options = formatOptions(options);
	//console.log('exportScript:',paths)
	//console.log(file,fs.existsSync(file) , fs.statSync(file).isFile())
	var file = require('path').join(base,paths[0]);
	if(paths.length==1 && fs.existsSync(file) && fs.statSync(file).isFile()){
		var isSingleJSFile =  paths.length == 1 && /\.js$/.test(paths[0]);
		var isSingleHTMLFile = paths.length == 1 && /\.html$/.test(paths[0]);
		//paths[0] = file.substr(base.length);
	}
	
	var base =fs.realpathSync(base||'./').replace(/\\/g,'/');
	var loader = new ScriptLoader(base);
	resetPaths(loader,base,paths)
	
	//exportHTML(root,path,callback)
	if(isSingleHTMLFile){
		require('./export-html').exportHTML('./',paths[0], onComplete)
	}else if(isSingleJSFile){
		//console.log('export single js file:'+paths)
		exportSingleFile(loader,paths[0], onComplete,options)
	}else{
		exportModules(loader,paths,onComplete,options)
	}
}
function exportModules(root,modules,callback,options){
	buildDefines(root,modules,function(defines,exportModules,internalDeps){//,externalDeps){
		/*
		 if(externalDeps.length >0){
			var error = 'has externalDeps not loaded!!, you must add all of the dependence modules in the exports list:'+externalDeps
			console.warn(error)
		}
		console.warn('externalDeps:',externalDeps)
		//*/
		//console.warn(exportModules,internalDeps)
		var exportOptions = {
			format:options.format,
			require:'require' in options?options.require:true,
			count:modules.length
		}
		var result = buildExportSource(modules,defines,exportOptions);
		callback(result);
	});
}
/**
 * <ul>
 *   <li>1: 全依赖导出
 *     <p>只指定一个入口文件，该文件在全局域运行，变量全部是全局变量。&#32; 依赖模块可用 require 函数获得。</p></li>
 *   <li>2: 单库导出
 *     <p>不自动运行，需要require，内部用 require 函数获得外部模块, 该方式不自动合并外部模块的内容（只合并模块内文件）。</p></li>
 *   <li>3  匿名导出
 *     <p>同1，但全部子模块作为匿名闭包载入，不能再用require(module)方式获得</p></li>
 * </ul>
 */
//compressJS = function(a){return a}
function exportSingleFile(loader,file,callback,options){
	if(/#/.test(file)){//假文件
		var path = '#main'
		var content = file.substr(1);
		//var deps = Define.parseDependence(root,this.source);
		new Define(loader,path,function(path,realPath){
			var modules = this.deps;
			if(modules.length){
				function inlineCallback(defines,paths,internalDeps,externalDeps){
					paths.unshift('#main');
					defines.unshift(this)
					onExport(defines,paths,internalDeps,externalDeps)
				}
				buildDefines(loader,modules,inlineCallback);
			}else{
				content = transformCode(content,'#main',options.format);
				callback(content);
			}
		},content)
		
	}else{
		var modules = [file.replace(/^[\/\\]|\.js$/g,'')];
		//console.log(modules)
		buildDefines(loader,modules,onExport);
	}
	function onExport(defines,paths,internalDeps,externalDeps){
		if(internalDeps.indexOf([paths[0]])>=0){
			var error = 'export failed: main module is required by dependenced module!  try type=2';
			console.error(error)
			callback(null,error)
			return ;
		}
		var mainPath = paths.shift();
		var mainDefine = defines.shift();
		var replaceMap = {};
		for(var i=0;i<defines.length;i++){
			replaceMap[defines[i].path]=i;
		}
		var mainSource = mainDefine.toString(replaceMap);
		//console.log(mainSource)
		mainSource = mainSource.replace(/^[^{]+\{([\s\S]*)\}\s*\)\s*;?\s*/,'$1');
		if(/\bexports\s*\./.test(mainSource)){
			mainSource='var exports = exports || window;'+mainSource;
		}
		mainSource =transformCode (mainSource,mainPath,options.format);
		
		//console.warn(mainDefine.toString(),'\n\n####\n\n',mainSource)
		if(internalDeps.length + externalDeps.length ==0){
			callback(mainSource);
		}else{
			options.count = 0;//{count:0,ns:'',format:options.format}
			options.require = true
			var result = buildExportSource(paths,defines,options) + '\n'+mainSource;
			callback(result);
		}
	}
}

/**
 * @param root
 * @param exports(module_paths)
 * @param callback(defines,exportsPaths,internalDepPaths,externalDepPaths)
 */
function buildDefines(loader,modules,callback){
	var dependenceDepth = callback.length -2;//0:noneDeps,1:only directDeps,2:any deps;
	var addInternal = callback.length > 2;
	var addExternal = callback.length > 3;
	//console.log('buildDefines:',callback.length,addExternal)
	var newExported = [];
	var exportsPaths=[],internalDepPaths=[],externalDepPaths = []
	var defines = [];
	var exportsMap = {};
	var count = modules.length;
	var loaded = 0;
	var prefixed = [];
	for(var i=0;i<count;i++){
		//console.error(modules[i])
		new Define(loader,modules[i],function(path,realPath){
			//console.log('@@1',path,realPath)
			exportsMap[path] = this;
			defines.push(this)
			exportsPaths.push(realPath);
			prefixed.push(realPath.replace(/\/.*/,''))
			
			if(++loaded == count){//为了保持顺序一致
				//console.log('prefixed:'+prefixed)
				if(addInternal){
					for(var j=0;j<count;j++){
						var def = defines[j]
						var depMap = def.depMap
						for(var n in depMap){
							var dep = depMap[n];
							if(!(dep in exportsMap)){
								if(newExported.indexOf(dep)==-1){
									var isInternal = prefixed.indexOf(dep.replace(/\/.*/,''))>=0;
									//console.log('isInternal:'+isInternal+':'+dep.replace(/\/.*/,''))
									if(addExternal || isInternal){
										//addExternal && console.log('add external:'+dep)
										newExported.push(dep);
									}
								}
							}
						}
					}
					loadNewExported();
				}else{
					callback(defines,exportsPaths);
				}
			}
			
		});
	}
	function loadNewExported(){
		var modules = newExported;
		var count = newExported.length;
		var loaded = 0;
		var baseIndex = defines.length
		if(count==0){
			callback(defines,exportsPaths,internalDepPaths,addExternal?externalDepPaths:null)
		}
		newExported = [];
		//console.log('@@####',modules)
		for(var i = 0;i<count;i++){
			new Define(loader,modules[i],function(path,realPath){
				if(path in exportsMap){
					console.error('double same require path:'+path)
				}
				exportsMap[path] = this;
				defines.push(this)
				var isInternal = prefixed.indexOf(realPath.replace(/\/.*/,''));
				(isInternal?internalDepPaths:externalDepPaths).push(realPath);
				if(++loaded == count){//为了保持顺序一致
					for(var j=0;j<count;j++){
						var def = defines[baseIndex+j]
						var depMap = def.depMap
						for(var n in depMap){
							var dep = depMap[n];
							//console.log('dep:',dep)
							if(!(dep in exportsMap)){
								if(newExported.indexOf(dep)==-1){
									var isInternal = prefixed.indexOf(dep.replace(/\/.*/,''))>=0;
									if(addExternal || isInternal){
										//addExternal && console.log('add external:'+dep)
										newExported.push(dep);
									}
								}
							}
						}
					}
					loadNewExported();;
				}
			});
		}
	}
}





function buildExportSource(paths,impls,exportConfig){
	exportConfig = exportConfig||{}
	var format = exportConfig.format;
	var exportCount = exportConfig.count || 1;
	var exportNS = exportConfig.ns || 'this';
	var exportRequire = !!exportConfig.require
	var source = (exportRequire ? exportRequireExample:exportExample);
	var replaceMap = {};
	for(var i=0;i<impls.length;i++){
		replaceMap[impls[i].path]=i;
	}
	//var exportNS = exportNS.replace(/^(this|window)$/,'');
	//console.log(source)
	if(exportCount){
		var dec = "";
		if(!/^(this|window)$/.test(exportNS)){
			if(/\./.test(exportNS)){
				dec = "var ns="+JSON.stringify(exportNS.split('.').reverse())+",current=this,i=ns.length;"+
				"while(i--){current = current[ns[i]] || (current[ns[i]] = {})}"
			}else{
				dec = "this."+exportNS+"=this.exportNS||{};";
			}
		}
		if(exportCount==1){
			var tail = "\tcopy(internal_require(0),"+exportNS+");\n"
			source = source.replace(/\}\s*$/,dec+tail+'}')
		}else if(exportCount>1){
			tail = "var i="+exportCount+";while(i--){copy(internal_require(i),"+exportNS+")}";
			source =  source.replace(/\}\s*$/,dec+tail+'}')
		}
	}
	
	source = transformCode(source.replace('__dirname','"./"')+'()','#export closure header',format).replace(/\(\);?$/,'');
	//console.log(source+compressJS.name)
	impls = impls.map(function(def,i){
		//console.log(def.path)
		var code =  def.toString(replaceMap)
		code = transformCode(code,def.path,format);
		code = code.substring(code.indexOf('function('),code.lastIndexOf('}')+1);
		if(/\/\//.test(code)){
			//console.log(def.path,code)
		}
		//$JSI.define('lite/parse/template-token.js',[],function(exports,require){
		
		return code;
	});
	//console.log(impls)
	if(exportRequire){//export require
		return source+"("+impls.join('\n,\n')+','+JSON.stringify(paths)+')'
	}else{
		return source + "("+impls.join('\n,\n')+')';
	}
}


function resetPaths(loader,base,paths){
	outer:for(var i=0;i<paths.length;i++){
		var path = paths[i]
		if(path.charAt(0) == '#'){
			continue;
		}
		if(path.match(/^\w/)){
			path = './'+path
		}
		var absPath = require('path').join(fs.realpathSync(base),path).replace(/\\/g,'/');
		var bases = loader.bases;
		//console.warn(absPath)
		for(var j=0;j < bases.length;j++){
			var nbase = bases[j].base;
			//console.warn(nbase,absPath.indexOf(nbase))
			if(absPath.indexOf(nbase)==0){
				var path = absPath.substring(nbase.length).replace(/^node_modules\//,'');
				//console.error(path,nbase,loader.findModuleAndPath(path,nbase),loader.bases)
				
				paths[i] = loader.findModuleAndPath(path,nbase)[1];
				//console.log('ModuleAndPath',paths)
				continue outer;
			}
		}
	}
	//console.log(paths)
}
exports.buildDefines = buildDefines ;
exports.exportScript = exportScript;
