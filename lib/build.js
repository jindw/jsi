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
	var loader = new ScriptLoader(source);
	if(!/^\/?([\w\-]+)\.js$/.test(p)){
		p = p.replace(/\.js$/,'__define__.js');
	}
	p = p.replace(/^[\\\/]+/,'');
	console.log('start:'+p);
	loader.load(p,function(content){
		var path = p.split(/[\\\/]/);
		FS.realpath(dest,function(err,dest){
			if(err){
				console.error("dest dir not found!!",err);
			}else{
				mkdirs(dest,path,function(path){
					FS.writeFile(path, content, function (err) {
						if (err) throw err;
						console.log('It\'s saved!');
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

exports.process = function(args){
	console.log("cmd:",args);
	console.log("cmd is coming....")
}
