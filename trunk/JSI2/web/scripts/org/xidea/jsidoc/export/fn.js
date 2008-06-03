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

function loadTextByURL(url){
    //$log.info(url);
    var req = new XMLHttpRequest();
    req.open("GET",url,false);
    try{
        //for ie file 404 will throw exception 
        req.send(null);
        if(req.status >= 200 && req.status < 300 || req.status == 304 || !req.status){
            //return  req.responseText;
            return req.responseText;
        }else{
            $log.debug("load faild:",url,"status:",req.status);
        }
    }catch(e){
        $log.debug(e);
    }finally{
        req.abort();
    }
}
var specialRegExp = new RegExp(['/\\*(?:[^\\*]|\\*[^/])*\\*/',//muti-comment
            '//.*$',                      //single-comment
            '/(?:\\\\.|[^/\\n\\r])+/',     //regexp 有bug
            '"(?:\\\\(?:.|\\r|\\n|\\r\\n)|[^"\\n\\r])*"',
            "'(?:\\\\(?:.|\\r|\\n|\\r\\n)|[^'\\n\\r])*'",    //string
            '^\\s*#.*'].join('|'),'gm');                         //process
function specialReplacer(text){
    if(text.charAt(0) == '/'){
        switch(text.charAt(1)){
            case '/':
            case '*':
              return '';
        }
    }
    return '""';
}
function findGlobals(source){
    var source = source.replace(specialRegExp,specialReplacer);
    //简单的实现，为考虑的问题很多很多：
    var result = [];
    var pattern = /^(?:var|function)\s+([\w]+)/mg;
    var match;
    while(match = pattern.exec(source)){
        result.push(match[1])
    }
    return result;
}