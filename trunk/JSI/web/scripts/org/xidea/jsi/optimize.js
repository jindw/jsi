/**
 * @jsiparser org.xidea.jsi.parse
 * @import org.xidea.jsidoc.util.findGlobals
 * @import org.xidea.jsidoc.util.loadText
 * @import org.xidea.jsi.parse
 * @export beforeAddScript
 * @export beforeAddDependence
 * 
 * @return [[objectNames, beforeLoadDependences, afterLoadDependences]]
 */
var findGlobals,parse,loadText;
function beforeAddScript(scriptPath, objectNames, beforeLoadDependences, afterLoadDependences){
	if(/\*/.test(objectNames)){
		if(objectNames instanceof Array){
			var i = objectNames.length;
            while(i--){
            	this.addScript.call(this,scriptPath, objectNames[i], beforeLoadDependences, afterLoadDependences)
            }
		}else{
    		var pattern = objectNames.replace(/\*/,'.*');
    		loadText = loadText || $import("org.xidea.jsidoc.util.loadText");
    		findGlobals = findGlobals || $import("org.xidea.jsidoc.util.findGlobals");
			var source = loadText($JSI.scriptBase+this.name.replace(/\.|$/g,'/')+scriptPath);
            objectNames = findGlobals(source);
            pattern = new RegExp('^'+pattern+'$');
            var i = objectNames.length;
            while(i--){
            	if(!pattern.test(objectNames[i])){
            		objectNames.splice(i,1);
            	}
            }
            this.addScript.call(this,scriptPath, objectNames, beforeLoadDependences, afterLoadDependences)
        }
    	return true;
	}else if(arguments.length == 1){
    	//TODO:从源码分析依赖关系
    	parse = parse || $import("org.xidea.jsi.parse");
    	var result = parse(this.name,scriptPath);
    	var i = result.length;
    	while(i--){
    		var item = result[i];// objectNames, beforeLoadDependences, afterLoadDependences
    		this.addScript.call(this,scriptPath,item[0],item[1],item[2],true)
    	}
    	return true;
    }
}
function beforeAddDependence(thisPath,targetPath,afterLoad){
	if(!afterLoad ){
    	//TODO:我未必想吧全部的依赖文件的全部对象名暴露出来
        thisPath = this.objectScriptMap[thisPath] || thisPath;
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
    if(targetPath.charAt(0) == '.'){
        var splitPos2Exp = targetPath.indexOf('/');
        var packageName = this.name;
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
    this.dependenceMap.push([thisPath,targetPath,afterLoad]);
    return true;
}