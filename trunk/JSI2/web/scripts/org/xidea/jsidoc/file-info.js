/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: file-info.js,v 1.7 2008/03/15 08:59:26 jindw Exp $
 */

/**
 * @public
 */
function FileInfo(packageInfo,fileName){
    this.packageInfo = packageInfo;
    this._depInf = new DependenceInfo(packageInfo.packageObject.name.replace(/\.|$/g,'/')+fileName)
    this.packageObject = packageInfo.packageObject;
    this.name = fileName;
    //$log.debug(this.packageObject.dependenceMap)
    this.dependences = this.packageObject.dependenceMap[fileName] || [];
    this.objects = [];
    var objectScriptMap = this.packageObject.objectScriptMap;
    for(var o in objectScriptMap){
        if(objectScriptMap[o] == fileName){
            this.objects.push(o);
        }
    }
}
/**
 * @public
 */
FileInfo.prototype.getDescription = function(){
    return this.getSourceEntry().getDescription();
}

FileInfo.prototype.getPath = function(){
     return this.packageInfo.name.replace(/\.|$/g,'/')+this.name;
}
/**
 * @public
 */
FileInfo.prototype.getSourceEntry = function(){
    if(!this._sourceEntry){
        this._sourceEntry = SourceEntry.require(this.name,this.packageObject.name);
    }
    return this._sourceEntry;
};


/**
 * @public
 */
FileInfo.prototype.getDocEntry = function(name){
    return this.getSourceEntry().getDocEntry(name);
};


/**
 * @public
 */
FileInfo.prototype.getObject = function(name){
    return this.getObjectMap()[name];
};
var importedMap = {};
/**
 * @friend
 */
FileInfo.prototype.getObjectMap = function(){
    if(!this._objectMap){
	    try{
	        var _objectMap = {};
	        //this.packageObject.loadScript(this.name,false);
	        try{
	            var path = this.packageObject.name.replace(/\.|$/g,'/') +this.name;
	            //this.packageObject.loadScript(this.name,false);
	            //Avoid document.write
	            var backup = document.write;
	            document.write = voidWrite;
	            importedMap[path] && $import(path,null);
	            document.write = backup;
	        }catch(e){
	            $log.info("文档工具装载脚本失败：",path,e);
	        }
	        for(var i=0;i<this.objects.length;i++){
	            _objectMap[this.objects[i]] = getObject(this.objects[i],this.packageObject.objectMap)
	        }
	        this._objectMap = this._objectMap || _objectMap
	    }catch(e){
	        $log.error(e);
	    }
    }
    return this._objectMap;
};
/**
 * @internal 
 */
function voidWrite(){
    
}

/**
 * @public
 */
FileInfo.prototype.getObjectInfo = function(name){
    return this.getObjectInfoMap()[name]
}


/**
 * @public
 */
FileInfo.prototype.getObjectInfoMap = function(){
    if(!this._objectInfoMap){
	    try{
	    	//$log.debug("getObjectInfoMap",this.objects);
	        var _objectInfoMap = {}
	        for(var i = 0;i<this.objects.length;i++){
	            _objectInfoMap[this.objects[i]] = ObjectInfo.create(this,this.objects[i]);
	        }
	        this._objectInfoMap = this._objectInfoMap || _objectInfoMap;
	    }catch(e){
	        $log.error(e);
	    }
    }
    return this._objectInfoMap;
}


/**
 * @friend
 */
FileInfo.prototype.getAvailableObjectInfo = function(name){
    var fileInfo = this.getAvailableObjectFileInfoMap()[name];
    if(fileInfo){
        return fileInfo.getObjectInfo(name);
    }
}
/**
 * @friend
 */
FileInfo.prototype.getAvailableObjectFileInfoMap = function(name){
    if(!this._availableOFMap){
        var _availableOFMap = {};
        var dependenceInfos = this._depInf.getBeforeInfos();
        dependenceInfos = dependenceInfos.concat(this._depInf.getAfterInfos());
        for(var i = 0;i<dependenceInfos.length;i++){
            var dependenceInfo = dependenceInfos[i];
            var names = dependenceInfo.objectNames;
            var depPkgName = dependenceInfo.packageObject.name;
            var depPkgInfo = PackageInfo.require(depPkgName);
            var depFileInfo = depPkgInfo.fileInfos[dependenceInfo.fileName];
            for(var j = 0; j<names.length;j++){
                _availableOFMap[names[j]] = depFileInfo;
            }
        }
        var names = this.objects;
        for(var i = 0;i<names.length;i++){
            _availableOFMap[names[i]] = this;
        }
        this._availableOFMap = this._availableOFMap || _availableOFMap;
        
    }
    return this._availableOFMap;
}


/**
 * @friend
 */
FileInfo.prototype.getAvailableObjectMap = function(name){
    if(!this._availableMap){
        var objectFileInfoMap = this.getAvailableObjectFileInfoMap();
        var objectMap = {};
        for(var n in objectFileInfoMap){
            objectMap[n] = objectFileInfoMap[n].getObject(n);
        }
        this._availableMap = this._availableMap || objectMap;
    }
    return this._availableMap;
}




/**
 * @public
 */
function getObject(name,map){
    var name = name.split('.');
    for(var i=0;map!=null && i<name.length;i++){
        map = map[name[i]];
    }
    return map;
}