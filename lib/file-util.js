var fs = require('fs');
var path = require('path');
function mkdirs(f,callback,mode){
	//console.log('mkdirs',f)
	fs.exists(f, function(exists) {
		if(exists) {
			callback(f);
		} else {
			//尝试创建父目录，然后再创建当前目录
			mkdirs(path.dirname(f), function(dir,err){
				//console.log('3mkdirs',f,err)
				fs.mkdir(f, mode, function(err){
					if(err){
						console.log(err)
						callback(null,err)
					}else{
						mkdirs(f,  callback,mode);
					}
				})
			},mode);
		}
	});
}
function rmdirs(f,callback){
	fs.stat(f,function(err,stat){
		if(stat){
			//console.log(stat)
			if(stat.isDirectory()){
				fs.readdir(f,function(err,list){
					var count = list && list.length;
					var i = count;
					if(count){
						while(i--){
							var cf = f+'/'+list[i];
							rmdirs(cf,function(){
								if(--count ==0){
									fs.rmdir(f,callback);
								}
							})
						}
					}else{
						fs.rmdir(f,callback);
					}
				})
			}else if(stat.isFile()){
				fs.unlink(f,callback);
			}else{
				console.log('unknow file:',f)
			}
		}else{
			console.log('rmdirs, file not exist:',f);
			callback(null);
		}
	})
}

function copy(src,dest,callback){
	fs.stat(src,function(err,stat){
		if(stat){
			//console.log(stat)
			if(stat.isDirectory()){
				fs.readdir(src,function(err,list){
					mkdirs(dest,function(){
						var count = list && list.length;
						var i = count;
						if(count){
							while(i--){
								var cf = src+'/'+list[i];
								var df = dest+'/'+list[i];
								copy(cf,df,function(){
									if(--count ==0){
										callback();
									}
								})
							}
						}else{
							callback();
						}
					})
				},stat.mode)
			}else if(stat.isFile()){
				var in_ = fs.createReadStream( src );
				var out = fs.createWriteStream( dest );   
				in_.pipe( out );
				out.on('end', function() {
					callback()
				});
				
			}else{
				console.log('unknow file:',src)
			}
		}else{
			console.log('copy, file not exist:',src);
			callback(null);
		}
	})
}
function rename(source,dest,callback,mode){
	mkdirs(path.dirname(dest),function(){
		rmdirs(dest,function(err){
			err && console.log(err)
			fs.rename(source,dest,callback)
		})
	},mode)
}
function lsR(root,callback,regexp){
	var result = [];
	var inc = 0;
	function read(file,path){
		inc++;
		fs.stat(file,function(err,stat){
			if(stat){
				//console.log(path,file,stat.isDirectory())
				if(stat.isDirectory()){
					inc++;
					FS.readdir(file,function(err,files){
						var i = files.length;
						while(i--){
							var n = files[i];
							if(!/^\.\.?$/.test(n)){
								read(file+'/'+n, path?path+'/'+n:n)
							}
						}
						inc--;
						if(inc<1){callback(result)}
					});
				}else{
					if(!regexp || regexp.test(path)){
						result.push(path)
					}
				}
			}
			inc--;
			//console.log(inc,file)
			if(inc<=0){callback(result)}
		})
	}
	read(root,'');
}

exports.copy = copy;
exports.mkdirs = mkdirs;
exports.rmdirs = rmdirs
exports.rename = rename;
exports.lsR = lsR;