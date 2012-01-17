var FS = require('fs');
var Path = require('path');
var root;
var listener;
var watchMap = {};
var checkInterval;
var checkIntervalByLoop = true;

var dirMap;//dir=>{stat,map}
var realpathMap;//realpath=>{instanceKey=>[abstractPath]}
var binaryMap;//realpath=>buffer

function _getDir(absPath){
	var dir = dirMap[absPath];
	if(!dir){
		dirMap[absPath] = dir = {
			stat:FS.statSync(absPath),
			path:absPath,
			failedMap:{},
			fileMap:_readFileMap(absPath)
		}
		_addDirWatch(absPath);
	}
	return dir;
}
function _readFileMap(absPath){
	var files = FS.readdirSync(absPath);
	var map = {};
	var i = files.length;
	while(i--){
		var n = files[i];
		var stat = FS.statSync(Path.resolve(absPath,n))
		if(stat.isFile()){
			map[n] = stat;
		}
	}
	return map;
}
function _fixpath(dirpath,fileName,prefix,postfix){
	var dir = _getDir(dirpath);
	if(postfix){
		var tmp = fileName.lastIndexOf('.');
		var postfixName = fileName.substr(0,tmp)+postfix+fileName.substr(tmp);
	}
	if(prefix){
		if(postfixName){
			tmp = _tryfix(dir,prefix+postfixName,fileName);//prepost
			if(tmp){
				return tmp;
			}
		}
		tmp = _tryfix(dir, prefix+fileName,fileName);//pre
		if(tmp){
			return tmp;
		}
	}
	if(postfixName){
		tmp = _tryfix(dir,postfixName,fileName);//post
		if(tmp){
			return tmp;
		}
	}
	return _tryfix(dir,fileName,fileName);
	
}
function _tryfix(dir,path){
	if(path in dir.fileMap){
		return Path.resolve(dir.path,path);//prepost
	}else{
		dir.failedMap[path]=fileName;//新建分支，这种需求很少见，不需要记录更多信息。
	}
}
function getDataAsBinary(path,prefix,postfix){
	var absPath = Path.resolve(root,path.substr(1));
	var dirpath = Path.dirname(absPath);
	var fileName = path.substr(path.lastIndexOf('/')+1);
	var fixPath = _fixpath(dirpath,fileName,prefix,postfix);
	if(fixPath){
		var realPath = fixPath;
		var data = binaryMap[realPath];
		if(!data){
			dirMap[dirpath].fileMap[fileName].stat = FS.statSync(realPath)
			var instanceKey = [prefix,postfix].join('::');
			var instanceMap = realpathMap[realPath] || (realpathMap[realPath] = {})
			var fileList = instanceMap[instanceKey]  || (instanceMap[instanceKey] = [])
			fileList.push(path);
			fixPath && _addFileWatch(fixPath);
			data = binaryMap[realPath] = FS.readFileSync(realPath);
		}
		return data;
	}
}

function _addFileWatch(path){
	if(!checkIntervalByLoop){
		var handler = watchMap[path];
		handler || (watchMap[path] = FS.watch(path, function (event, filename) {
			var instanceMap = realpathMap[path];
			for(var key in instanceMap){
				var list = instanceMap[key]
				listener(key,list);
			}
		}));
	}
}
function _addDirWatch(path){
	if(!checkInterval){
		checkInterval = setInterval(_checkDir,300);
	}
}
function _checkDir(){
	for(var dirpath in dirMap){
		try{
			var dir = dirMap[dirpath]
			var stat = FS.statSync(dirpath);
			var newFileMap = dir.stat.mtime.getTime() !== stat.mtime.getTime() && _readFileMap(dirpath);
		}catch(e){
			//TODO:remove dir
			_on_file_change_dir_removed(dir,true);
			continue;
		}
		if(newFileMap){
			dir.stat = stat;
			_on_dirchanged(dir,newFileMap);
		}else{//dir changed
			//尝试遍历子节点？
			if(checkIntervalByLoop){
				_on_file_change_dir_removed(dir,false);
			}
		}
	}
}
function _on_dirchanged(dir,newFileMap){
	dir.fileMap = newFileMap;
	var failedMap = dir.failedMap;
	for(var n in failedMap){//add
		var absPath = Path.resolve(dir.path,n);
		var instanceMap = realpathMap[absPath];
		if(instanceMap){
			for(var key in instanceMap){
				var ppfix = key.split('::')
				var list = instanceMap[key];
				var i = list.length;
				while(i--){
					var path2 = list[i];
					var p = path2.lastIndexOf('/')
					path2 = _fixpath(path2.substr(0,p),path2.substr(p+1),ppfix[0],ppfix[1]);
					if(absPath != path2){
						delete binaryMap[absPath];
						listener(key,list.splice(i,1));
					}
				}
			}
		}
	}
}
function _on_file_change_dir_removed(dir,remove){
	var fileMap = dir.fileMap;
	for(var n in fileMap){
		var path = Path.resolve(dir.path,n);
		var instanceMap = realpathMap[path];
		if(instanceMap){
			if(remove){
				delete dirMap[dir.path]
			}else{
				try{
					var newStat = FS.statSync(path);
				}catch(e){
					console.warn('file statSync failed',e);
					newStat = null;
				}
				if(newStat == null ){
					console.warn('file not found failed',path);
				}else if(fileMap[n].stat.mtime.getTime()==newStat.mtime.getTime()){
					continue;
				}
			}
			_clean_cache(path);
		}
	}
}
function _clean_cache(path,instanceMap){
	delete binaryMap[path];
	for(var key in instanceMap){
		var list = instanceMap[key];
		if(list && list.length){
			listener(key,list);
			list.length=0;
			delete instanceMap[key]
		}
	}
}
exports.getDataAsBinary = getDataAsBinary;

exports.initialize = function(root_,listener_){
	for(var n in watchMap){
		watchMap[n].close();
	}
	checkInterval && clearInterval(checkInterval);
	checkInterval = null;
	root = root_;
	listener = listener_;
	watchMap = {};
	dirMap = {}
	binaryMap = {};
	realpathMap = {};
}

//