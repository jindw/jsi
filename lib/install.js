var fileutil= require('./file-util');
var fs= require('fs');
var path= require('path');
var export4web = require('./exports').export4web
var execPath = process.execPath; //usr/local/bin/node
var compressJS = require('./js-token').compressJS;

if(/node.exe$/.test(execPath)){//windows
	var npmPath = execPath.replace(/[\w\.]+$/,'node_modules/npm');
}else{
	var npmPath = execPath.replace(/bin\/node$/,'lib/node_modules/npm')
}
var npm = require(npmPath);
exports.init = function(root,force){
	//console.log(root,force)
	var ScriptLoader = require('./js-loader').ScriptLoader;
	var loader = new ScriptLoader(root);
	var block = path.join(root,'block.js');
	fs.exists(block,function(exists){
		if(!exists || force){
			fs.unlink(block,function (err) {
				//if (err) console.log(err);
				loader.load('block.js',function(content){
					fs.writeFile(block,content,function(err){
						if(err){
							console.error('file write error:',block)
						}else{
							console.info('file write success:',block)
						}
					})
				});
			});
		}
	});
	var boot = path.join(root,'boot.js');
	fs.exists(boot,function(exists){
		if(!exists || force){
			fs.unlink(boot,function (err) {
				//if (err) console.log(err);
				loader.load('boot.js',function(content){
					fs.writeFile(boot,compressJS(String(content),'boot.js'),function(err){
						if(err){
							console.error('file write error:',boot)
						}else{
							console.info('file write success:',boot)
						}
					})
				});
			});
		}
	});
	
	var config = path.join(root,'config/main.js');
	fs.exists(config,function(exists){
		if(!exists){
			fileutil.mkdirs(path.dirname(config),function(){
				fs.writeFile(config,"$JSI.init({})",function(err){
					if(err){
						console.error('file write error:',config)
					}else{
						console.info('file write success:',config)
					}
				})
			})
		}
	})
	
	
}
exports.install = function(root,pkg,callback){
	exports.init(root,false);
	var file = pkg[0];
	var pkgConfig = path.join(file,'package.json');
	//console.log(pkgConfig,fs.existsSync(pkgConfig))
	if(fs.existsSync(file)){
		if(fs.existsSync(pkgConfig)){
			var json = JSON.parse(fs.readFileSync(pkgConfig)+"");
			var nameVersions = [json.name,json.version];
			var list = [[nameVersions.join('@'),file]]
			console.log('install from file:',nameVersions)
			compileModule(root,nameVersions,file,function(){
				updateConfig(root,list,function(){
					callback.apply(this,arguments);
				});
			});
		}else{
			console.log('package.json is required for module:'+file)
		}
		return;
	}
	pkg[0] = pkg[0].replace(/^https\:\/\/github\.com\/[\w\.]+\/[\w\.]+(?:.git)?$/,'git+$&');
	npm.load('',function(){
		var tmp = path.join(root,'tmp');
		npm.commands.install(tmp,pkg,function(err,list){
			if(err){
				console.log('npm install error:',err);
				return;
			}
			//console.log('npm install:',tmp,pkg,list)
			//确保子目录在前面
			list.sort(function(thiz,next){thiz[1].length-next[1].length})
			//console.log(list)
			var count = list.length;
			var i = count;
			while(i-->0){
				var line = list[i];
				var nameVersions = line[0].split('@');
				var path = line[1];
				compileModule(root,nameVersions,path,function(){
					console.log('compile complete:',path,root,nameVersions)
					if(--count ==0){//complete
						updateConfig(root,list,function(){
							fileutil.rmdirs(tmp,function(){
								callback.apply(this,arguments);
							});
						});
						
					}
				});
			}
			/*var dir = root+;
			*/
			
		})
	})
}

function updateConfig(root,list,callback){
	var mainConfig = path.join(root,'./config/main.js');
	console.log('update config:',mainConfig)
	fileutil.mkdirs(path.dirname(mainConfig),function(){
		fs.readFile(mainConfig,function(err,data){
			var json = data?data.toString().replace(/^[^{]+(\{[\s\S]*\})[^}]+$/,'$1'):'{}';
			//consle.log('update config:',json)
			var json = JSON.parse(json);
			var i = list.length;
			while(i--){
				var nvs = list[i][0].split('@');
				json[nvs[0]] = nvs[1];
			}
			var newSource = "$JSI.init("+JSON.stringify(json)+')';
			fs.writeFile(mainConfig,newSource,function(){
				console.info('write config:',newSource)
				callback();
			})
		})
	});
	
}
function compileModule(root,nameVersions,sourcePath,callback){
	//var tmp = root+'/.bin/node_modules';
	var name_ = nameVersions[0];
	var version = nameVersions[1];
	var dest =  path.join(root,'o',name_,version);
	//console.log('compileModule:',sourcePath)
	export4web(path.dirname(sourcePath),[name_],function(impls,idIndex,internalDeps,externalDeps){
		//console.log('export4web',impls.length,internalDeps,externalDeps)
		var source = '$JSI.define("'+name_+'",'+JSON.stringify(externalDeps)+","+impls.join(',')+')';
		var count = 2;
		fileutil.copy(sourcePath,dest,function(err){
			err && console.log(err)
			fs.writeFile( path.join(dest,'1.js'),source,function(err){
				console.log('export module:',err || dest+'/1.js')	
				if(--count ==0)callback();
			});
			
			fs.writeFile( path.join(dest,'0.js'),compressJS(source,name_+'__define__.js'),function(err){
				console.log('export module:',err || dest+'/0.js')	
				if(--count ==0)callback();
			});
		},0755)
		
	})
	
}
