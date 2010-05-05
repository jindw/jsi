/**
 * @jsiparser org.xidea.jsi.parse
 * #@import org.xidea.jsidoc.util.findGlobals
 * #@import org.xidea.jsi.parse
 * @export optmizePackage
 */
var findGlobals,parse,loadText;
function optimizePackage(PackageClass,loadTextArg){
	loadText = loadTextArg;
	var pp = PackageClass.prototype;
	function OptmizePackage(){
		PackageClass.apply(this,arguments);
		for(var temp in this.scriptObjectMap){
			return;
		}
		if(!this.implementation){
			this.addScript("*");
		}
	}
	OptmizePackage.prototype = pp;
	addCallFilter(pp,'addScript',addScriptFilter);
	addCallFilter(pp,'addDependence',addDependenceFilter);
	return OptmizePackage;
}
function addCallFilter(pp,key,filter){
	var chain = pp[key];
	pp[key] = function(){
		return filter.apply([this,chain],arguments) 
	}
}
            	
function addScriptFilter(scriptPath, objectNames, beforeLoadDependences, afterLoadDependences){
	var thiz = this[0];
	var chain = this[1]
	if(/\*/.test(objectNames)){
		if(objectNames instanceof Array){
			var i = objectNames.length;
            while(i--){
            	thiz.addScript.call(thiz,scriptPath, objectNames[i], beforeLoadDependences, afterLoadDependences)
            }
		}else{
    		var pattern = objectNames.replace(/\*/,'.*');
    		findGlobals = findGlobals || $import("org.xidea.jsidoc.util.findGlobals");
			var source = loadText($JSI.scriptBase+thiz.name.replace(/\.|$/g,'/')+scriptPath);
            objectNames = findGlobals(source);
            pattern = new RegExp('^'+pattern+'$');
            var i = objectNames.length;
            while(i--){
            	if(!pattern.test(objectNames[i])){
            		objectNames.splice(i,1);
            	}
            }
            thiz.addScript.call(thiz,scriptPath, objectNames, beforeLoadDependences, afterLoadDependences)
        }
	}else if(arguments.length == 1){
    	//TODO:从源码分析依赖关系
    	parse = parse || $import("org.xidea.jsi.parse");
    	var result = parse(thiz.name,scriptPath,loadText);
    	var i = result.length;
    	while(i--){
    		var item = result[i];// scriptPath,objectNames, beforeLoadDependences, afterLoadDependences
    		thiz.addScript.apply(thiz,item)
    	}
    }else{
    	chain.apply(thiz,arguments)
    }
}
function addDependenceFilter(thisPath,targetPath,afterLoad){
	var thiz = this[0];
	var chain = this[1]
	if(typeof targetPath == 'string'){
		if(!afterLoad ){
	    	//TODO:我未必想吧全部的依赖文件的全部对象名暴露出来
	        thisPath = thiz.objectScriptMap[thisPath] || thisPath;
	    }
	    targetPath = trimPath(thiz.name,targetPath);
	    thiz.dependenceMap.push([thisPath,targetPath,afterLoad]);
	}else{
		chain.apply(thiz,arguments)
	}
}
/*
 * 绝对路径:
 *   example:sayHello
 *   example/hello.js
 * 上级路径:
 *   ..util:JSON,....:Test
 *   ../util/json.js,../../test.js
 * 下级相对路径
 *   .util:JSON
 *   ./util/json.js
 * 
 */
function trimPath(packageName,targetPath){
	if(targetPath.charAt(0) == '.'){
	    var splitPos2Exp = targetPath.indexOf('/');
	    if(splitPos2Exp>0){
	        packageName = packageName.replace(/[\.$]/g,'/') ;
	        // thispkg/../util/json.js   
	        // thispkg/../../test.js
	        // thispkg/./util/json.js
	        splitPos2Exp = /(?:\w+\/\.|\/)\./
	    }else{
	        // thispkg..util:JSON
	        // thispkg....:Test
	        // thispkg.util:JSON
	        splitPos2Exp = /\w+\.\./
	    }
	    targetPath = packageName+targetPath;
	    while(targetPath!=(targetPath = targetPath.replace(splitPos2Exp,'')));
	}
    return targetPath;
}