/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: dependence-info.js,v 1.5 2008/03/02 08:01:09 jindw Exp $
 */


var dependenceInfoMap = {};


/**
 * @param packageName 包名,不可为空
 * @param fileName 文件名,当为空时 objectName 参数一定不为空 (偷懒行为,构造时自动通过objectName查找)
 * @param objectName 对象名,可为空
 * 
 */ 
function DependenceInfo(path){
    if(dependenceInfoMap[path]){
        return dependenceInfoMap[path];
    }else{
        dependenceInfoMap[path] = this;
    }
    var packageFileObject = parsePath(path);
    var packageName = packageFileObject[0];
    var fileName = packageFileObject[1];
    var objectName = packageFileObject[2];
    var packageObject = this.packageObject = $import(packageName+':');
    if(packageObject.initialize){
        packageObject.initialize();
    }
    this.path = path;
    this.filePath = packageName.replace(/\.|$/g,'/')+fileName;
    this.fileName = fileName;
    this.objectName = objectName;
    this.subInfos = [];
    if(objectName){
        this.objectNames = [objectName];
    }else{
        this.objectNames = [];
        var map = this.packageObject.objectScriptMap;
        for(var n in map){
            if(map[n] == fileName){
                this.objectNames.push(n);
            }
        }
    }
}
DependenceInfo.prototype = {
    /**
     * 获取装载前依赖对应的变量(直接依赖)
     * @public
     * @owner DependenceInfo.prototype
     */
    getBeforeVars : function(){
        
    },
    /**
     * 获取装载后依赖对应的变量(直接依赖)
     * @public
     * @owner DependenceInfo.prototype
     */
    getAfterVars : function(){
        
    },
    /**
     * 获取全部(直接和间接)装载后依赖
     * @public
     * @owner DependenceInfo.prototype
     */
    getBeforeInfos : function(){
        return findDependence(this,0)
    },
    /**
     * 获取全部(直接和间接)装载前依赖
     * @public
     * @owner DependenceInfo.prototype
     */
    getAfterInfos : function(){
        return findDependence(this,1)
    },
    /**
     * 本依赖包含指定依赖
     * @public
     * @owner DependenceInfo.prototype
     */
    implicit : function(dest){
        if(this.packageObject == dest.packageObject && this.fileName == dest.fileName){
            if(this.objectName == null || this.objectName == dest.objectName){
                return true;
            }else{
                //this.objectName != null
                //dest.objectName == null || not null
                //return ! dest.getAfterInfos().length;
                return afterInfosIsEmpty(dest);
            }
        }
    }
};
if(":debug"){
    DependenceInfo.prototype.toString = function(){
        return [this.packageObject.name,this.fileName,this.objectName].join('/');
    }
}
function parsePath(path){
    var pos = path.lastIndexOf('/');
    if(pos>0){
        //file
        var packageName = path.substr(0,pos).replace(/\//g,'.');
        var fileName = path.substr(pos+1);
    }else{
        pos = path.lastIndexOf(':');
        if(pos == -1){
            pos = path.lastIndexOf('.');
        }
        var packageName = path.substr(0,pos);
        var objectName = path.substr(pos+1);
        var fileName = $import(packageName+':').objectScriptMap[objectName];
    }
    return [packageName,fileName,objectName]
}

function afterInfosIsEmpty(dependenceInfo){
    if(dependenceInfo.subInfos[1]){
        return !dependenceInfo.subInfos[1].length;
    }
    var dependences = dependenceInfo.packageObject.dependenceMap[dependenceInfo.fileName];
    var i = dependences && dependences.length;
    while(i--){
        var dependence = dependences[i];
        if(dependence[4]){//afterload
            var thisObject = dependence[3];
            if(!thisObject || !dependenceInfo.objectName || dependenceInfo.objectName == thisObject){
                return false;
            }
        }
    }
    return true;
}
function findDependence(dependenceInfo,index){
    if(!dependenceInfo.subInfos[index]){
        var dependences = dependenceInfo.packageObject.dependenceMap[dependenceInfo.fileName];
        var result = [];
        var i = dependences && dependences.length;
        dependenceLoop:
        while(i--){
            var dependence = dependences[i];
            if(!index == !dependence[4]){
                var thisObject = dependence[3];
                if(!index || !thisObject || !dependenceInfo.objectName || dependenceInfo.objectName == thisObject){
                    if(dependence[2]){//object
                        var path = dependence[0].name + ':' + dependence[2];
                    }else{
                        var path = dependence[0].name.replace(/\.|$/g,'/') + dependence[1];
                    }
                    var itemInfo = new DependenceInfo(path);
                    var j = result.length;
                    while(j--){
                        if(result[j].implicit(itemInfo)){
                            continue dependenceLoop;
                        }
                    }
                    result.push(itemInfo);
                }
            }
        }
        dependenceInfo.subInfos[index] = result;
    }
    return dependenceInfo.subInfos[index];
}
