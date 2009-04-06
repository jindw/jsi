/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: package-info.js,v 1.3 2008/02/24 08:58:15 jindw Exp $
 */


function PackageInfo(name,packageName){
    packageMap[name] = this;
    this.name = name;
    this.packageObject = packageName;//
    if(this.packageObject.name != name){
        this.implementation = this.packageObject.name;
        return ;
    }
    this.fileInfos = [];
    for(var file in this.packageObject.scriptObjectMap){
        var info = new FileInfo(this,file);
        this.fileInfos.push(info);
        this.fileInfos[file] = info;
    }
    this.fileInfos.sort();
}

/**
 * @private
 */
var packageMap = {};
/**
 * @public
 */
PackageInfo.require = function(name){
    if(name){
        if(packageMap[name]){//return old
            return packageMap[name];
        }else{
            var packageObject = $import(name+':');
            if(packageObject){
                return new PackageInfo(name,packageObject);
            }else{
                return null;
            }
        }
    }else{
        return this.rootInfo;
    }
}
PackageInfo.requireRoot = function(files){
    if(this.rootInfo){
        return this.rootInfo;
    }else{
        return this.rootInfo = new RootInfo(files || ['boot.js']);
    }
};



/**
 * @public
 */
PackageInfo.prototype.getObjectInfo = function(name){
        return this.getObjectInfoMap()[name]; 
}
/**
 * @protected
 */
PackageInfo.prototype.getObjectInfoMap = function(){
    if(!this._objectInfoMap){
        var _objectInfoMap = {};
        //$log.debug("getObjectInfoMap",this.fileInfos.length)
        for(var i = 0;i<this.fileInfos.length;i++){
            var objectInfoMap = this.fileInfos[i].getObjectInfoMap();
            for(var o in objectInfoMap){
                _objectInfoMap[o] = objectInfoMap[o];
            }
        }
        //$log.debug("getObjectInfoMap",_objectInfoMap)
        
        this._objectInfoMap = _objectInfoMap;
    }
    return this._objectInfoMap;
};
/**
 * @public
 */
PackageInfo.prototype.getObjectInfos = function(){
    if(!this._objectInfos){
        var _objectInfos = [];
        var objectInfoMap = this.getObjectInfoMap();
        for(var n in objectInfoMap){
            _objectInfos.push(n);
        }
        //$log.debug("getObjectInfos",_objectInfos.length)
        _objectInfos.sort();
        for(var i=0;i<_objectInfos.length;i++){
            //$log.info(list[i]);
            _objectInfos[i] = objectInfoMap[_objectInfos[i]];
            //$log.info(list[i]);
        }
        this._objectInfos = _objectInfos
    }
    return this._objectInfos;
}




/**
 * @private
 */
PackageInfo.prototype.getInitializers = function(){
    function gen(fi){
        return function(){
            fi.getObjectInfoMap();
        }
    }
    var rtvs = [];
    for(var i = 0;i<this.fileInfos.length;i++){
        rtvs.push(gen(this.fileInfos[i]));
    }
    return rtvs;
}


PackageInfo.prototype.getPath = function(){
     return this.name+":";
}
/**
 * @public
 */
PackageInfo.prototype.getDescription = function(){
    return this.getSourceEntry().getDescription();
}
/**
 * @public
 */
PackageInfo.prototype.getSourceEntry = function(){
    if(!this._sourceParser){
        this._sourceParser = SourceEntry.require("__package__.js",this.packageObject.name);
    }
    return this._sourceParser;
}
