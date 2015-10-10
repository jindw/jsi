function export4web(root,paths,callback){
	//console.log('export4web:',root,paths)
	var loader = new ScriptLoader(root);
	var idIndex = paths.map(function(path){
		path = path.replace(/(.\/.+)\.js$/,'$1');
		return path;
	})
	var defineExp = /^\s*\$JSI\.define\(\s*['"]([\w-\.\/]+)['"]\s*,\s*(\[[^\]\s]*\])\s*,\s*(function[\s\S]+?\})\s*\)\s*;?\s*$/;
	
	var pathds = paths.map(function(path){
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
						if(paths.indexOf(path.replace(/\/.*/,''))<0){
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

function buildExportSource(baseDir,impls,idIndex,ns,publicCount){
	if(!ns){//export require
		return exportRequireExample.replace('__dirname','"./"')+"("+impls.join(',')+','+JSON.stringify(idIndex)+')'
	}else{
		ns = ns.replace(/^(this|window)$/,'');
		var result = [];
		var source = exportExample.replace('__dirname',baseDir).replace(/\breturn\s+internal_require\s*\(\s*0\s*\)/,function(a){
			var buf = [];
			if(ns){
				if(publicCount == 1){
					return a;
				}else{
					ns && buf.push('var o = {};')
				}
			}
			ns && buf.push('return ')
			for(var i=0;i<publicCount;i++){
				if(i){
					buf.push(',');
				}
				buf.push('internal_require(',i,',',ns?'o':'this',')');
			}
			return buf.join('')
		});
		
		if(ns){
			result.push('var ',ns,'=',source);
		}else{
			result.push('~',source);
		}
		result.push('(',impls
					//.map(function(path){return path.replace(/^((?:.*[\r\n]*){1,2})[\s\S]*([\r\n]*.*\}\s*)$/,'$1...$2')})
					.join(','),')');
		return result.join('')
	}
}
var fs = require('fs')
var exportExample = fs.readFileSync(require.resolve('../assets/test/export-example.js')).toString()
	.replace(/^.*?(function[\s\S]+?\})\s*\(\s*function\([\s\S]+$/,"$1");
var exportRequireExample = fs.readFileSync(require.resolve('../assets/test/export-require-example.js')).toString();
//console.log(exportExample)
var path = require('path')
var ScriptLoader = require('./js-loader').ScriptLoader;
var fileutil = require('./file-util')

exports.export4web = export4web ;
exports.buildExportSource = buildExportSource ;
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
