var FS = require('fs');
var Path = require('path');
exports.VFS = VFS;
function defaultTryBuilder(fileName,id){
	id = id.split('::');
	var prefix = id[0];
	var postfix = id[1];
	var tmp;
	if(postfix){
		id = fileName.lastIndexOf('.');
		postfix = fileName.substr(0,id)+postfix+fileName.substr(id);
	}
	tmp = [];
	if(prefix){
		if(postfix){
			tmp.push(prefix+postfix);//prepost
		}
		tmp.push(prefix+fileName)//pre
	}
	if(postfix){
		tmp.push(postfix);//post
	}
	tmp.push(fileName);//raw
	return tmp;
}
function VFS(listener,tryBuilder){
var checkInterval;
var checkIntervalByLoop = true;
var dirMap = {};//dir=>{stat,failedMap,hitMap}
tryBuilder = tryBuilder || defaultTryBuilder;
this.getFile = getFile;
this.getDirectoryInfo = getDirectoryInfo;
function getFile(path,id){
	try{
		var fileName = Path.basename(path);
		var dirpath = Path.dirname(path);
		var tryNames = tryBuilder(fileName,id);
		var dirInfo = getDirectoryInfo(dirpath);
		var fileMap = dirInfo.fileMap;
		var i = tryNames.length;
	}catch(e){
		console.warn("getFile Error:",fileName,path,id,e);
		return null
	}
	while(i--){
		var tryName = tryNames[i];
		if(tryName in fileMap){
			//,FS.statSync(Path.resolve(dirpath,tryName))
			_addFileMap(dirInfo.hitMap,tryName,id,fileName);
			_addFileWatch(tryName);
			return Path.resolve(dirpath,tryName);
		}else{
			_addFileMap(dirInfo.failedMap,tryName,id,fileName);
		}
	}
}
function _addFileMap(map,tryName,id,fileName,stat){
	var pathMap = map[tryName];
	if(!pathMap){
		pathMap = map[tryName] = {};
	}
	pathMap[id] = fileName;
}
/**
 * @path File sbsolutePath
 */
function getDirectoryInfo(path){
	var dir = dirMap[path];
	if(!dir){
		dirMap[path] = dir = {
			stat:FS.statSync(path),
			path:path,
			failedMap:{},//tryPath=>{id=>path}//not exists path
			hitMap:{},//truePath=>{id=>path,stat=>initStat}//existed path
			fileMap:_readFileMap(path)
		}
		_addDirWatch(path);
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
function _addFileWatch(path){
	if(listener && !checkIntervalByLoop){
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
	if(listener && !checkInterval){
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
			_dir_removed(dir);
			continue;
		}
		
		if(checkIntervalByLoop){
			_on_file_change(dir);
		}
		if(newFileMap){
			////尝试遍历子节点？
			dir.stat = stat;//纵使 _on_file_change时删除了 dirInfo 也无所谓 ,_on_dir_changed 中不出现dir获取
			_on_dir_changed(dir,newFileMap);
		}
	}
}
function _on_dir_changed(dir,newFileMap){
	var hitMap = dir.hitMap;
	var failedMap = dir.failedMap;
	var oldFileMap = dir.fileMap ;
	dir.fileMap = newFileMap;
	for(var tryName in failedMap){//add
		if((tryName in newFileMap) && !(tryName in oldFileMap)){//new File
			var tryPathMap = failedMap[tryName];//tryPath=>{id=>path}
			var pathMap = {};
			for(var path in tryPathMap){
				pathMap[path]=1;
			}
			for(var trueName in hitMap){
				var hitPathMap = hitMap[trueName]
				for(var hitId in hitPathMap){
					if(hitPathMap[hitId] in paths){
						//listener(hitId,hitPathMap[hitId]);
						_clean_cache(hitMap,dir.path,trueName);
						break;
					}
				}
			}
			
		}
	}
}
function _dir_removed(dir){//dir removed
	delete dirMap[dir.path];
	for(var trueName in dir.hitMap){
		_clean_cache(dir.hitMap,dir.path,trueName);
	}
}
function _on_file_change(dir){//file modified,delete
	var hitMap = dir.hitMap;
	var fileMap = dir.fileMap;
	var clean = true
	for(var trueName in hitMap){
		try{
			var absPath = Path.resolve(dir.path,trueName);
			var oldStat = fileMap[trueName];
			var newStat = FS.statSync(absPath);
			if(!(oldStat.mtime - newStat.mtime)){//==
				clean = false;
				continue;
			}
			fileMap[trueName] = newStat;
			console.log('file changed',absPath)
		}catch(e){
			console.warn('file statSync failed',e);
		}
		_clean_cache(hitMap,dir.path, trueName);
	}
	if(clean){
		delete dirMap[dir.path]
	}
}

function _clean_cache(hitMap,dirPath,trueName){
	var pathMap = hitMap[trueName]
	console.log('clean cache:',trueName)
	for(var id in pathMap){
		console.log('cache id:',id)
		listener(id,Path.resolve(dirPath,pathMap[id]));
	}
	delete hitMap[trueName];
}
}