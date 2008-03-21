/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */




/**
 * @private
 */
function findPackages(sourcePackageNames,findDependence){
    var packageMap = {};
    var packageFlags = {};
    var currentList = [].concat(sourcePackageNames);
    for(var i = 0;i<currentList.length;i++){
        packageFlags[currentList[i]] = true;
    }
    do{
        var newList = [];
        for(var i = 0;i<currentList.length;i++){
            try{
                var packageObject = $import(currentList[i]+':');
                if(packageObject == null){
                    continue;
                }
            }catch(e){
                continue;
            }
            packageMap[currentList[i]] = packageObject;
            if(packageObject.name!=currentList[i]){
                if(!packageFlags[packageObject.name]){
                    packageFlags[packageObject.name] = true;
                    newList.push(packageObject.name);
                }
            }else if(findDependence){
                if(packageObject.initialize){
                    packageObject.initialize();
                }
                var dependenceMap = packageObject.dependenceMap;
                for(var scriptFile in dependenceMap){
                    var dependences = dependenceMap[scriptFile];
                    for(var j=0;j<dependences.length;j++){
                        var dependence = dependences[j];
                        try{
                            var packageObject = dependence[0];
                            var packageName = packageObject.name;
                            if(!packageFlags[packageName]){
                                packageFlags[packageName] = true;
                                newList.push(packageName);
                            }
                        }catch(e){
                            //依赖可能无效
                        }
                    }
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
    }
    for(var i=0;i<sourcePackageNames.length;i++){
        var list = listMap[sourcePackageNames[i]];
        result.push.apply(result,list.sort());
    }
    result.push.apply(result,extention.sort());
    return result;
}

function xmlReplacer(c){
    switch(c){
        case '<':
          return '&lt;';
        case '>':
          return '&gt;';
        case '&':
          return '&amp;';
        case "'":
          return '&#39;';
        case '"':
          return '&#34;';
    }
}