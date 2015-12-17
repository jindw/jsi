var compressJS = require('./js-token').compressJS

var fs = require('fs')
var exportExample = fs.readFileSync(require.resolve('../assets/test/code-example-export.js')).toString()
	.replace(/^.*?(function[\s\S]+?\})\s*\(\s*function\([\s\S]+$/,"$1");
var exportRequireExample = fs.readFileSync(require.resolve('../assets/test/code-example-export-require.js')).toString();
//console.log(exportExample)
var path = require('path')
var ScriptLoader = require('./js-loader').ScriptLoader;
var fileutil = require('./file-util')

function buildExportSource(baseDir,paths,impls,exportCount,exportNS,useRequire){
	var source = (useRequire ? exportRequireExample:exportExample);
	//var exportNS = exportNS.replace(/^(this|window)$/,'');
	//console.log(source)
	exportNS = exportNS || 'this';
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
			var tail = "copy(internal_require(0),"+exportNS+")"
			source = source.replace(/\}\s*$/,dec+tail+'}')
		}else if(exportCount>1){
			tail = "var i="+(exportCount-1)+";while(i--){copy(internal_require(i),"+exportNS+")}";
			source =  source.replace(/\}\s*$/,dec+tail+'}')
		}
	}
	//console.log(source)
	source = compressJS(source.replace('__dirname','"./"')+'()','#export closure header').replace(/\(\);?$/,'');
	impls = impls.map(function(s,i){return compressJS(s,paths[i])});
	//console.log(impls)
	if(useRequire){//export require
		return source+"("+impls.join(',')+','+JSON.stringify(paths)+')'
	}else{
		return source + "("+impls.join(',')+')';
	}
}
function exportModules(root,modules,type,callback){
	if( type == 1){
		console.log('muti modules export do not  support type 1, type2 instead!');
		type = 2;
	}else if(type == 3){
		console.log('muti modules export  type3  any modules is running in clusure!');
	}else{
		type = 1
	}
	exports.export4web(root,modules,function(impls,paths,internalDeps,externalDeps){
		if(externalDeps.length >0){
			var error = 'has externalDeps not loaded!!, you must add all of the dependence modules in the exports list:'+externalDeps
			console.warn(error)
		}
		var result = buildExportSource("'./'",paths,impls,modules.length,'',type==2);
		callback(result,error);
	});
}
function exportSingleFile(root,file,type,callback){
	if(/#/.test(file)){
		var content = file.substr(1);
		var defines = require('./define').buildDefine(root,'#main','#main','#main',content);//,idIndex,mainMap)
		var deps = JSON.parse(defines.replace(/^.*?(\[[^]*\])[\s\S]*$/,'$1').replace(/'/g,'"'));
		console.info('inline file deps:',deps)
		if(deps.length){
			export4web(root,deps,function(impls,paths,internalDeps,externalDeps){
				paths.unshift('#main');
				impls.unshift(content)
				onExport(impls,paths,internalDeps,externalDeps)
			});
		}else{
			content = compressJS(content,'#main');
			callback(content);
		}
	}else{
		var modules = [file.replace(/^[\/\\]|\.js$/g,'')];
		export4web(root,modules,onExport);
	}
	function onExport(impls,paths,internalDeps,externalDeps){
		if(type == 1 || type==3){
			if(internalDeps.indexOf(0)>=0){
				var error = 'export failed: main module is required by dependenced module!  try type=2';
				console.error(error)
				callback(null,error)
				return ;
			}
			//console.log(paths,impls.length,internalDeps,externalDeps)
			var mainSource = impls[0].replace(/[^{]+\{([\s\S]*)\}/,'$1');
			//TODO: restore require path
			mainSource = mainSource.replace(/\brequire\((\d+)\)/g,function(a,id){
				return "require('"+paths[id]+"')"
			})
			mainSource = compressJS(mainSource,paths[0]);
			//console.log(mainSource)
			if(externalDeps.length ==0){
				if(paths.length==1){
					callback(mainSource);
					return;
				}else{
					//不行！！！！ id 是不匹配的
					//var result = buildExportSource("'./'",paths,impls,0,'',needRequire) + '\n'+mainSource;
					//callback(result);
					//return;
				}
			}
			var needRequire = type == 1;
			
			var modules = paths.slice(1).concat(externalDeps);
			export4web(root,modules,function(impls,paths,internalDeps,externalDeps){
				
				//console.log('>>>',modules,impls.length,internalDeps,externalDeps)
				if(externalDeps.length){
					modules = modules.concat(externalDeps);
					export4web(root,modules,arguments.callee);
				}else{
					//console.log('+++',paths,impls.length,internalDeps,externalDeps)
					var result = buildExportSource("'./'",paths,impls,0,'',needRequire) + '\n'+mainSource;
					callback(result);
				}
			})
		}else{
			var result = buildExportSource("'./'",paths,impls,0,'',true);
			callback(result,externalDeps);
		}
	}
}


function export4web(root,idIndex,callback){
	//console.log('export4web:',root,paths)
	var loader = new ScriptLoader(root);
	//var idIndex = paths.map(function(path){
	//	path = path.replace(/(.\/.+)\.js$/,'$1');
	//	return path;
	//})
	var defineExp = /^\s*\$JSI\.define\(\s*['"]([\w-\.\/]+)['"]\s*,\s*(\[[^\]\s]*\])\s*,\s*(function[\s\S]+?\})\s*\)\s*;?\s*$/;
	
	var pathds = idIndex.map(function(path){
		return path.replace(/(?:(.\/.+)\.js)?$/,'$1__define__.js');
	});
	var impls = [];
	var internalDeps = [];
	var externalDeps = [];
	//console.log(pathds)
	var currentPathd = pathds[0];
	
	loader.load(currentPathd,function(content){
		//console.log(String(content).replace(/^([\s\S]{80})[\s\S]*?([\s\S]{20})$/,"$1...$2"))
		var m = content .match(defineExp);
		//console.log(content.replace(/^([\s\S]{80})[\s\S]*?([\s\S]{20})$/,"$1...$2"))
		
		//console.log('!!',m)
		if(m){
			var refId = m[1];//refid
			var mid = refId.replace(/^\/.*$/,'');
			var id = loader.mainMap[mid] == refId?mid:refId;
			
			//console.log(id,refId,loader.mainMap);
			var deps = JSON.parse(m[2]);
			var impl = m[3];
			//console.log('impl.length:',idIndex,id,impl.length)
			impls[idIndex.indexOf(id)] = impl;
			var i = deps.length;
			while(i--){
				var path = deps[i];
				if(/^[0-9]+$/.test(path)){
					path = path*1;
					if(internalDeps.indexOf(path)<0){
						internalDeps.push(path);
					}
				}else{
					if(externalDeps.indexOf(path)<0){
						var hits = idIndex.filter(function(id){return id == path || id.replace(/\/.*/,'') == path.replace(/\/.*/,'')})
						if(hits.length){
							//error
							console.log('invalid path!!',path,idIndex)
						}else{
							externalDeps.push(path);
						}
					}
				}
			}
			
			//console.log(internalDeps,externalDeps,impls.length)
			if(pathds[0] == currentPathd){
				pathds.shift();
				currentPathd = pathds[0]
				if(currentPathd){
					
					//console.log('export4web1:',currentPathd)
					loader.load(currentPathd,arguments.callee,idIndex);
					return;
				}
			}
			//console.log('###',internalDeps,externalDeps)
			//console.log(pathds)
			var i = internalDeps.length;
			//console.log(Object.keys(impls))
			while(i--){
				if(!(internalDeps[i] in impls)){
					var index = internalDeps[i];
					//console.log('export4web2:',index,idIndex[index])
					loader.load(idIndex[index]+"__define__.js",arguments.callee,idIndex)
					return;
				}
			}
			
			//console.log("externalDeps:",externalDeps)
			//complete
			callback(impls,idIndex,internalDeps,externalDeps);
			
			//console.log(deps,'\n===========\n\n');
			//console.log(String(impl).replace(/^([\s\S]{80})[\s\S]*?([\s\S]{20})$/,"$1...$2"))
		}else{
			//error
		}
	},idIndex)
}

exports.export4web = export4web ;
exports.exportSingleFile = exportSingleFile;
exports.buildExportSource = buildExportSource ;
exports.exportModules = exportModules;
//exports.build = function(sourceRoot,destRoot,module){
//	var loader = new ScriptLoader(sourceRoot);
//	fileutil.lsR(path.join(sourceRoot,module),function(list){
//		list.forEach(function(id){
//			var id = module+'/'+id;
//			var file = path.join(destRoot,id);
//			fileutil.mkdirs(path.dirname(file),function(){
//				if(sourceRoot != destRoot){
//					var in_ = fs.createReadStream( path.join(sourceRoot,id) );
//					var out_ = fs.createWriteStream( path.join(destRoot,id) );   
//					// 通过管道来传输流
//					in_.pipe( out_ );
//				}
//				
//				if(/\.js$/.test(id)){
//					var defineId = id.replace(/\.js$/,'__define__.js');
//					var file = path.join(destRoot,defineId);
//					loader.load(defineId,function(content){
//						fs.writeFile(file, content, function (err) {
//							if (err) throw err;
//							console.log('saved:'+file);
//						})
//					});
//				}
//			})
//		})
//	})
//}
