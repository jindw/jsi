exports.export4web = function(root,path,dest){
	var loader = new ScriptLoader(root);
	var idIndex = [path.replace(/\.js$/,'')]
	var defineExp = /^\s*\$JSI\.define\(\s*['"]([\w-\.\/]+)['"]\s*,\s*(\[[^\]\s]*\])\s*,\s*(function[\s\S]+?\})\s*\)\s*;?\s*$/;
	
	
	var pathd = path.replace(/\.js$/,'__define__.js');
	var impls = [];
	var internalDeps = [];
	var externalDeps = [];
	loader.load(pathd,function(content){
		var m = content .match(defineExp);
		//console.log(content.replace(/^([\s\S]{80})[\s\S]*?([\s\S]{20})$/,"$1...$2"))
		if(m){
			var id = m[1];
			var deps = JSON.parse(m[2]);
			var impl = m[3];
			impls[idIndex.indexOf(id)] = impl;
			//console.log("!!!",idIndex,(id),implMap)
			var i = deps.length;
			while(i--){
				var p = deps[i];
				if(/^[0-9]+$/.test(p)){
					p = p*1;
					if(internalDeps.indexOf(p)<0){
						internalDeps.push(p);
					}
				}else{
					if(externalDeps.indexOf(p)<0){
						externalDeps.push(p);
					}
				}
			}
			var i = internalDeps.length;
			//console.log(Object.keys(impls))
			while(i--){
				if(!(internalDeps[i] in impls)){
					var index = internalDeps[i];
					loader.load(idIndex[index]+"__define__.js",arguments.callee,idIndex)
					return;
				}
			}
			//console.log("externalDeps:",externalDeps)
			//complete
			
			var file = require.resolve('../assets/test/export-example.js');
			FS.readFile(file,function(err,data){
				if(err){
					console.error(err)
				}else{
					var tmp = data.toString();//function(
					var id = path.replace(/^(..?\/)*|\.js$/g,'').replace(/[^\w]/g,'_');
					
					console.log(path,id)
					var result = "var "+id+tmp.replace(/\}\s*\(\s*function\([\s\S]+$/,"}(").replace(/^var\s+\w+/,'')+impls.join(',')+')';
					mkdirs(root,dest.split('/'),function(path){
						FS.writeFile(path, result, function (err) {
							if (err) throw err;
							console.log('saved:',path);
						})
					});
					
				}
			})
			//console.log(deps,'\n===========\n\n');
			//console.log(String(impl).replace(/^([\s\S]{80})[\s\S]*?([\s\S]{20})$/,"$1...$2"))
		}else{
			//error
		}
	},idIndex)
}


var FS = require('fs')
var ScriptLoader = require('./js-loader').ScriptLoader;
function readFiles(root,callback){
	var result = [];
	var inc = 0;
	function read(file,path){
		inc++;
		FS.stat(file,function(err,stat){
			if(stat){
				//console.log(path,file,stat.isDirectory())
				if(stat.isDirectory()){
					inc++;
					FS.readdir(file,function(err,files){
						var i = files.length;
						while(i--){
							var n = files[i];
							if(n.charAt() !== '.'){
								read(file+'/'+n,path+'/'+n)
							}
						}
						inc--;
						if(inc<1){callback(result)}
					});
				}else{
					result.push(path)
				}
			}
			inc--;
			//console.log(inc,file)
			if(inc<1){callback(result)}
		})
	}
	read(root,'');
}
function writeTo(source,dest,p){
	if(!/^\/?([\w\-]+)\.js$/.test(p)){
		p = p.replace(/\.js$/,'__define__.js');
	}
	p = p.replace(/^[\\\/]+/,'');
	console.log('start:'+p);
	
	var loader = new ScriptLoader(source);
	loader.load(p,function(content){
		var path = p.split(/[\\\/]/);
		FS.realpath(dest,function(err,dest){
			if(err){
				console.error("dest dir not found!!",err);
			}else{
				mkdirs(dest,path,function(path){
					FS.writeFile(path, content, function (err) {
						if (err) throw err;
						console.log('saved:'+path);
					})
				});
			}
		})
	});
}
function mkdirs(dest,paths,callback){
	dest = dest.replace(/[\\\/]?$/,'/')+paths.shift();
	//console.log(dest)
	if(paths.length==0){
		callback(dest);
	}else{
		FS.exists(dest, function(exist){
			if(exist){
				mkdirs(dest,paths,callback)
			}else{
				FS.mkdir(dest,function(err){
					console.log("mkdir:",err)
					mkdirs(dest,paths,callback)
				})
			}
		});
	}
}
exports.build = function(source,dest){
	readFiles(source,function(list){
		var i=list.length;
		while(i--){
			var p = list[i];
			if(/\.js$/.test(p)){
				writeTo(source,dest,p);
			}
		}
	})
}