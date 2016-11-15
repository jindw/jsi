var UglifyJS = require('uglifyjs');
var analyse = require('./uglify-analyse')
var fs = require('fs');
//var numbers = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$_0123456789";
var updateExportMap = require('./uglify-analyse2').updateExportMap;

var KEYWORDS = 'break case catch const continue debugger default delete do else finally for function if in instanceof new return switch throw try typeof var void while with';
var KEYWORDS_ATOM = 'false null true';
var RESERVED_WORDS = 'abstract boolean byte char class double enum export extends final float goto implements import int interface long native package private protected public short static super synchronized this throws transient volatile yield'
    + " " + KEYWORDS_ATOM + " " + KEYWORDS;
RESERVED_WORDS = RESERVED_WORDS.split(/\s+/)
var DEBUG 
//DEBUG= true;
//require('util').error = function(){throw new Error()}

function readContent(path){
	return fs.readFileSync(path).toString().replace(/require\(['"](.*\.xml)["']\)/g,function(a,xml){
			var xmlpath = require('path').join(path.replace(/[^\\\/]+$/,''),xml);
			try{
				return JSON.stringify(fs.readFileSync(xmlpath).toString());
			}catch(e){
				console.error('read xml failed',e)
				return a;
			}
			
		});
}
exports.transform = function(paths,dest,format){
	for(var i=0;i<paths.length;i++){
		//UglifyJS.AST_Node.warn_function = Function.prototype;
		var path = paths[i];
		var contents = readContent(path)
		try{
			var compressed = UglifyJS.parse(contents,{filename:path});
		}catch(e){
			if(e.line){
				var lines = contents.split(/\r\n?|\n/);
				var line = Math.max(0,e.line-1-2)
				console.log("error code:\n"+lines.slice(line,line+3).join('\n'))
			}
			console.error(e);
			throw e;
		}
		if(!format){
			var compressor = UglifyJS.Compressor();
			compressed.figure_out_scope();
			compressed.compute_char_frequency();
			//if(!DEBUG)compressed.mangle_names();
			compressed = compressed.transform(compressor);
		}
		//console.dir(opt)
		var source = compressed.print_to_string({beautify:!!format});
		if(dest){
			var file= require('path').join(dest,path.replace(/.*\//,''));
			fs.writeFileSync(file,source);
		}else{
			console.log(path+':\n',source)
		}
	}
}
/**
 * jsi merge <path1>#exportValue1,exportValue2 <path2> ..')
 * console.log('jsi merge <path1>#* <path2> ..
 */
exports.runMerge = function(paths,opt){
	
	var globalExportMap = {};
	var analyseMap = genAnalyseMap(paths,globalExportMap)
	
	var exportMangledMap = {};
	var contents = mergeAnalysedFile(analyseMap,exportMangledMap)
	
	
	var vars = [];
	var result =[];
	var ns = opt && opt.ns;
	for(var n in globalExportMap){
		var id = globalExportMap[n];
		
		var mangledVars = exportMangledMap[id];
		var info =  analyseMap[id];
		var realName = info.exportMap[n];
		var v = info.ast.variables.get(realName);
		var mangled = mangledVars[realName];
		
		//toMangledName();
		//console.error('@@@@',n,realName,mangled)
		//console.dir(mangledVars)
		if(ns){
			result.push(',',n,':',mangled);
		}else{
			result.push(',',n+'=',mangled);
		}
		vars.push(n);
		//mangleds.push(mangled);
	}
	
	//console.error('@@@@@')
	//contents = "alert(123)"
	if(ns && vars.length){
		result[0] = 'return {';
		result.push('}}()');
		result.unshift(ns.replace(/^[\w\$]+$/,'var $&'),'=function(){',contents)
	}else{
		result[0] = vars.length?'\nreturn ':'';
		//console.warn(result.join(''))
		result.splice(result.length-2,1)
		result.push('}()')
		//console.warn(result.join(''))
		result.unshift(vars.join(',').replace(/.+/,'var $&=')||'+','function(){',contents);
	}
	
	//console.error('@@@@@')
	contents = result.join('');
	
	//console.log(contents)
	//console.error(contents)
	//console.log(contents);
	var compressor = UglifyJS.Compressor();
	//UglifyJS.AST_Node.warn_function = Function.prototype;
	try{
	var compressed = UglifyJS.parse(contents,{filename:'#merge-output.js'});
	}catch(e){
		if(e.line){
			var lines = contents.split(/\r\n?|\n/);
			var line = Math.max(0,e.line-1-2)
			console.log("error code:\n"+lines.slice(line,line+3).join('\n'))
		}
		console.error(e);
		throw e;
	}
	compressed.figure_out_scope();
	compressed.compute_char_frequency();
	if(!DEBUG)compressed.mangle_names();
	compressed = compressed.transform(compressor);
	//console.dir(opt)
	compressed = compressed.print_to_string({beautify:!!(opt.f||opt.format)});
	return (compressed)
}
function genAnalyseMap(paths,globalExportMap){
	var analyseMap = {}
	var prefix = null;
	var cmdExportMap = {};
	//var idPathMap={};
	var idList = [];
	var topMap = [];
	
	//console.log('###########'+paths)
	var realPaths = paths.map(function(path){
		var ps = path.split('#');
		var hash = ps[1];
		if(/\.js$/.test(ps[0])){
			var path = fs.realpathSync(ps[0]).replace(/[\\]/g,'/');
			if(prefix == null){
				prefix = path.replace(/[^\/]*$/,'')
			}else{
				while(path.indexOf(prefix)!=0){
					prefix = prefix.replace(/[^\/]*$/,'')
				}
			}
			var id =  path.substring(prefix.length).replace(/\.js$/,'');
		}else{
			var id = ps[0];
			var path = require.resolve(id);
			
			if(id.match(/^[\w\-]+$/)){
				topMap[id] = path.replace(/\\/g,'/').replace(/.*\/node_modules\//,'')
			}
			console.log(path)
		}
		
		if(ps[1]){
			cmdExportMap[ path] = hash.split(/[^\w*]+/);
		}
		idList.push(id)
		return path;
	});
	//console.log('do merge',path)
	var globalExportList =[];
	var len = realPaths.length;
	
	for(var i=0;i<len;i++){
		var path = realPaths[i];
		//var id = path.substring(prefix.length).replace(/\.js$/,'');
		var id = idList[i]//idPathMap
		var code = readContent(path);
		
		var info = analyseMap[id] = analyse.analyse(code,id in topMap?topMap[id]:id);
		if(info.invalidRequires.length ==0 && info.invalidExports.length ==0){//do replace!!
			info.exportMap = {};//导出变量的真实变量名表（有可能指向另一个模块变量或者函数，而不是exports属性名）
			info.ast = updateExportMap(info);
		}else{
			console.log("has unsupport useage for exports and requires:"+info.invalidRequires+info.invalidExports);
			throw new Error();
		}
		
		var exportVarNames = info.exportVars;
		var cmdFileExports = cmdExportMap[path];
		
		//console.error(exportVarNames,cmdFileExports)
		if(cmdFileExports == null){
			cmdFileExports = [];
		}else if(cmdFileExports.indexOf('*')>=0){
			cmdFileExports = exportVarNames.concat();
		}
		
		//console.warn(id,cmdFileExports)
		var j = cmdFileExports.length;
		while(j--){
			var v = cmdFileExports[j]
			if(exportVarNames.indexOf(v) >=0){
				if(globalExportList.indexOf(v) >=0){
					console.error('muti exports variable:',path+'#'+v+' and '+globalExportMap[v]);
				}
				globalExportList.push(v);
				globalExportMap[v] = id;
			}else{
				console.error('miss exports variable:',path+'#'+v);
				cmdFileExports.splice(j,1)
			}
		}
		cmdExportMap[path] = cmdFileExports;
	}
	return analyseMap;
}
function mergeAnalysedFile(analyseMap,exportMangledMap){
	
	for(var id in analyseMap){
		var info = analyseMap[id];
		
		var mangledVars = exportMangledMap[id] = {};// || (mangledMap[moduleId]= {});
		for(var n in info.exportMap){
			var realName = info.exportMap[n];
			var variable = info.ast.variables.get(realName);
			if(!variable){
				console.log('variable not found:'+realName)
			}
			//realName may be import from other module
			mangledVars[realName] = doMangledName(id,variable,analyseMap);
			//console.log('####',n,realName,v.mangled_name)
		}
	}
	///console.log('!@@@@@@@')
	for(var id in analyseMap){
		var info = analyseMap[id];
		info.ast.variables.each(function(variable,n){
			if(!variable.mangled_name){
				doMangledName(id,variable,analyseMap);
			}
		})
	}
	
	
	var contents = [];
	for(var id in analyseMap){
		var info = analyseMap[id];
		//console.warn('mangled!!')
		var compressAst = info.ast;
		//必要，否则可能出现重名现象
		//compressAst.mangle_names();
		contents.push(compressAst.print_to_string({beautify:true}));
		//console.warn('mangled!!printed')
	}
	return contents.join('\n');
}
function doMangledName(moduleId,variable,analyseMap,from){
	var varName = variable.name;
	if(!variable.mangled_name){
		var info = analyseMap[moduleId];
		if(varName in info.exportMap){
			//varName = info.exportMap[varName] || varName;
		}
		var requireVarInfo = analyse.decodeRequireVariable(varName);
		if(requireVarInfo){
			var externalModule = requireVarInfo[0];
			var property = requireVarInfo[1];
			if(externalModule in analyseMap){
				var externalInfo = analyseMap[externalModule];
				var externalRealName = externalInfo.exportMap[property];
				var externalVariable = externalInfo.ast.variables.get(externalRealName);
				//console.log(property,externalRealName,externalInfo.exportMap)
				if(!externalVariable){
					console.log('variable not found('+moduleId+'.js):',
						externalRealName,externalModule+'#'+property,externalInfo.exportMap)
					//console.log('@@@@',externalInfo.exportAst.variables.get(externalRealName).managled_name)
				}
				if(externalVariable.mangled_name){
					varName =  externalVariable.mangled_name;
				}else{
					varName= doMangledName(externalModule,externalVariable,analyseMap
						//from+"/"+externalModule
						);
				}
			}else{
				var err = 'dependenced module not fount('+moduleId+'.js):'+externalModule+'.'+varName;
				console.error(err);
				throw new Error(err)
			}
		}else if(variable.undeclared){
			//如果是exports呢？
			console.warn('foun undeclared variables:'+variable.name)
		} else{
			//没必要，反正后面还要做一次整体混淆?
			//非常必要， 不然不同module全局变量冲突就完蛋了！！
			varName = asGlobals(moduleId,varName);
		}
	}
	return variable.mangled_name =variable.mangled_name||varName ;
}
function asGlobals(module,rawId){
	//realName may be import from other module
	return module.replace(/[^\w]/g,'_')+'$'+rawId;
}

