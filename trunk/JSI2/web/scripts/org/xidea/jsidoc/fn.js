/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.4 2008/02/24 08:58:15 jindw Exp $
 */

function createPrototypeStack(superclass,constructor) {
    function f(){};
    f.prototype = superclass.prototype;
    var npt = new f();
    npt.constructor = constructor;
    return npt;
}


function findSupperInfo(baseInfo,object){
    var superList = [];
    var availableObjectMap = baseInfo.fileInfo.getAvailableObjectMap();
    for(var n in availableObjectMap){
        var superObject = availableObjectMap[n];
        if(superObject instanceof Function && object instanceof superObject){
            var k = 0;
            for(var n2 in superObject.prototype){
                k++;
            }
            if(superList.length){
                if(k>superList[0]){
                        superList = [k,n]
                }else if(k==superList[0]){
                        superList.push(n)
                }
            }else{
                superList = [k,n];
            }
        }
    }
    //alert(superList)
    if(superList.length){
        if(superList.length == 2){
                //alert(superList[1])
                return baseInfo.fileInfo.getAvailableObjectInfo(superList[1]);
        }else{
                superList[0] = superList.pop();
                while((n = superList.pop())&&(n2 = superList.pop())){
                        var obj1 = availableObjectMap[n];
                        var obj2 = availableObjectMap[n2];
                        //保留子类
                        if(obj1.prototype instanceof obj2){
                                superList.push(obj1);
                        }else{
                                superList.push(obj2);
                        }
                }
                //alert(n)
                return baseInfo.fileInfo.getAvailableObjectInfo(n);
        }
    }
}

/**
 * @internal
 */
var accessOrder = "private,internal,protected,friend,public";

function scrollOut(ele){
    if(ele.scrollIntoView){
        ele.scrollIntoView(false);
    }
}




/**
 * @private
 */
function findPackages(sourcePackageNames,findDependence){
    var packageMap = {};
    var packageFlags = {};
    for(var i = 0;i<sourcePackageNames.length;i++){
        try{
            var packageObject = $import(sourcePackageNames[i]+':');
            packageFlags[sourcePackageNames[i]] = true;
        }catch(e){
            sourcePackageNames.splice(i,1);
        }
    }
    var currentList = [].concat(sourcePackageNames);
    do{
        var newList = [];
        for(var i = 0;i<currentList.length;i++){
            try{
                var packageObject = $import(currentList[i]+':');
            }catch(e){
                currentList.splice(i,1);
                continue;
            }
            packageMap[currentList[i]] = packageObject;
            if(packageObject.name!=currentList[i]){
                if(!packageFlags[packageObject.name]){
                    packageFlags[packageObject.name] = packageObject;
                    newList.push(packageObject.name);
                }
            }else if(findDependence){
                try{
                    if(packageObject.initialize){
                        packageObject.initialize();
                    }
                    var dependenceMap = packageObject.dependenceMap;
                    for(var scriptFile in dependenceMap){
                        var dependences = dependenceMap[scriptFile];
                        for(var j=0;j<dependences.length;j++){
                            var dependence = dependences[j];
                            var packageObject = dependence[0];
                            var packageName = packageObject.name;
                            if(!packageFlags[packageName]){
                                packageFlags[packageName] = packageObject;
                                newList.push(packageName);
                            }
                        }
                    }
                }catch(e){
                    //依赖可能无效
                }
            }
        }
        currentList = newList;
    }while(currentList.length>0);
    
    var result = [];
    for(var n in packageMap){
        result.push(n);
    }
    return sortPackages(sourcePackageNames,result);

};
function sortPackages(sourcePackageNames,allPackageNames){
    /** 升序 */
    var short2longList = sourcePackageNames.concat([]).sort(function(a,b){return a.length-b.length});
    var listMap = {};
    var emptyMap = {};
    var i = allPackageNames.length;
    var extention = [];
    var extentionPerfixMap = {};
    var result = [];
    list:
    while(i--){
        var item = allPackageNames[i];
        var j = short2longList.length;
        while(j--){
            var key = short2longList[j]
            if(item.indexOf(key) == 0){
                if(listMap[key] == emptyMap[key]){
                    listMap[key]= [];
                }
                listMap[key].push(item);
                continue list;
            }
        }
        extention.push(item);
        extentionPerfixMap[item] = item.replace(/(^|\.)[^.]+$/,'');
    }
    var count = extention.length;
    while(count){
        count = 0;
        for(var i=0;i<extention.length;i++){
            var item = extention[i];
            var prefix = extentionPerfixMap[item];
            if(prefix){
                extentionPerfixMap[item] = prefix.replace(/(^|\.)[^.]+$/,'');
                for(var j=0;j<sourcePackageNames.length;j++){
                    var previous = sourcePackageNames[j];
                    if(previous.indexOf(prefix) == 0){
                        var list = listMap[previous];
                        list.push(item);
                        extention.splice(i,1);
                        break;
                    }
                }
                count++;
            }
        }
    }
    for(var i=0;i<sourcePackageNames.length;i++){
        var list = listMap[sourcePackageNames[i]];
        result.push.apply(result,list.sort());
    }
    result.push.apply(result,extention.sort());
    return result;
}

/*
 * Dependence = [0            , 1             , 2               , 3            ,4         ,5    ]
 * Dependence = [targetPackage, targetFileName, ,thisObjectName, afterLoad,names]
 * afterLoad,thisObject 有点冗余
 */
//var DEPENDENCE_TARGET_PACKAGE = 0;
//var DEPENDENCE_TARGET_FILE_NAME = 1;
//var DEPENDENCE_TARGET_OBJECT_NAME = 2;
//var DEPENDENCE_THIS_OBJECT_NAME = 3;
//var DEPENDENCE_AFTER_LOAD = 4;
//var DEPENDENCE_NAMES = 4;
