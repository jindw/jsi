var buildDefine = require('./define').buildDefine
var IncludeBuilder = require('./merge').IncludeBuilder



function ScriptLoader(root,loader){
	this.loader = loader || buildLoader('./');
}
ScriptLoader.prototype.load = function(path,callback){
	//console.log(path)
	var sourcePath = path.replace(/__define__(?=\.js$)/,'');
	var isDefine = sourcePath != path;
	this.loader(sourcePath,function(path,text){
		var text = new IncludeBuilder(path,text,this.loader,function(){
			if(isDefine){
				var id = path.replace(/\.js$/,'');
				callback( buildDefine(id,text));
			}else{
				callback( text);
			}
		});
	})	
}


function buildLoader(root){
	var fs = require('fs');
	root = root.replace(/\/?$/,'/');
	return function(path,callback){
		path = path.replace(/^\//,'');
		fs.readFile(root+path,function(err,data){
			if(err){
				switch(path){
				case 'require.js':
				case 'console.js':
				case 'wait.js':
					try{
						var file = require.resolve('../assets/'+path);
						fs.readFile(file,function(err,data){
							if(err){
								callback(path,err)
							}else{
								callback(path,data.toString())
							}
						})
						return;
					}catch(e){
						callback(path,e)
						return ;
					}
				}
				var id = path.replace(/\.js$/,'');
				var matchs = id.match(/^[\w\.\-]+(\/.*)?$/);
				if(matchs){
					var file = require.resolve(id).replace(/\\/g,'/');
					if(matchs[1]){//path
						fs.readFile(file,function(err,data){
							if(err){
								callback(path,err)
							}else{
								callback(path,data.toString())
							}
						})
					}else{
						//compute relative path
						var p = file.lastIndexOf('/node_modules/');
						var path2 = file.substr(file.indexOf('/',p+2)+1);
						var refid = path2.replace(/\.js$/,'');
						callback(path,"$JSI.copy(require('"+refid+"'),exports)")
					}
				}else{
					callback(path,"alert('file:"+path+" not found')");
				}
				
			}else{
				callback(path,data.toString())
			}
		});
	}
}
exports.ScriptLoader = ScriptLoader;
