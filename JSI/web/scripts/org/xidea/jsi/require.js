var findPackageByPath0;
var realPackage0;
function buildRequire(scriptWriter,findPackageByPath,realPackage){
	var requireList = [];
	var loaderMap = {}
	if(findPackageByPath0){
		findPackageByPath = findPackageByPath0;
		realPackage = realPackage0;
	}else{
		if(findPackageByPath){
			findPackageByPath0 = findPackageByPath;
			realPackage0 = realPackage;
		}else{
			$require();
		}
	}
	function writeRequire(packageObject,fileName,object){
		var result = require(packageObject,fileName,object);
		for(var i = 0,l = result.length;i<l;i++){
			(scriptWriter || defaultWriter)(result[i]);
		}
	}
	function defaultWriter(file){
		document.write("<script src='"+file+"'></script>");
	}
	function require(packageObject,fileName,object){
		var i = requireList.length;
		requireScript(packageObject,fileName,object);
		return requireList.slice(i);
	}
	function getLoader(packageObject,fileName){
		return loaderMap[packageObject.name+'/'+fileName]
	}
	function setLoader(packageObject,fileName,loader){
		loaderMap[packageObject.name+'/'+fileName] = loader;
	}
	/*
	 * @see boot.js
	 */
	function requireScript(packageObject,fileName,object){
	    var loader = getLoader(packageObject,fileName);
	    if(!loader){
	        //trace("load script path:",packageObject.scriptBase ,fileName);
	        if(packageObject.scriptObjectMap[fileName]){
	            //不敢确认是否需要延迟到这里再行初始化操作
	            if(packageObject.initialize){
	                packageObject.initialize();
	            }
	            loader = new ScriptLoader(packageObject,fileName);
	        }else{
	            //TODO: try parent
	            if(":Debug"){
	                throw new Error('Script:['+packageObject.name+':'+fileName+'] Not Found')
	            }
	        }
	    }
	    if(loader.initialize){
	        //trace("object loader initialize:",packageObject.scriptBase ,fileName);
	        loader.initialize(object);
	    }
	}
	/*
	 * @see boot.js
	 */
	function requireDependence(data){
	    requireScript(data[0],data[1],data[2]);
	}
	/**
	 * @see boot.js
	 */
	function ScriptLoader(packageObject,fileName){
	    this.name = fileName;
	    this.scriptBase = packageObject.scriptBase;
	    var loader = prepareScriptLoad(packageObject,this)
	    if(loader){
	        return loader;
	    }
	    doScriptLoad(packageObject,this);
	};
	/*
	 * 前期准备，初始化装载单元的依赖表，包括依赖变量申明，装载前依赖的装载注入
	 * Dependence = [0            , 1             , 2               , 3            ,4         ,5    ]
	 * Dependence = [targetPackage, targetFileName, targetObjectName,thisObjectName, afterLoad,names]
	 * @see boot.js
	 */
	function prepareScriptLoad(packageObject,loader){
	    var name = loader.name;
	    var deps = packageObject.dependenceMap[name];
	    var i = deps && deps.length;
	    while(i--){
	        var dep = deps[i];
	        var key =  dep[3] || 0;
	        if(dep[4]){//装在后依赖，记录依赖，以待装载
	            if(map){
	                if(map[key]){
	                    map[key].push(dep);
	                }else{
	                    map[key] = [dep]
	                }
	            }else{
	                //函数内只有一次赋值（申明后置，也就你JavaScript够狠！！ ）
	                var map = loader.dependenceMap = {};
	                loader.initialize = ScriptLoader_initialize;
	                map[key] = [dep]
	            }
	        }else{//直接装载（只是装载到缓存对象，没有进入装载单元），无需记录
	            //这里貌似有死循环的危险
	            requireDependence(dep);
	            if(dep = getLoader(packageObject,name)){
	                return dep;
	            }
	        }
	    }
	}
	
	
	/*
	 * 装载脚本
	 * 这里没有依赖装载，装载前依赖装载在prepareScriptLoad中完成，装载后依赖在ScriptLoader.initialize中完成。
	 * @private 
	 */
	function doScriptLoad(packageObject,loader){
	    var loaderName = loader.name;
	    var packageName = packageObject.name;
	    setLoader(packageObject,loaderName,loader);
	    requireList.push(packageObject.scriptBase+loaderName);
	    return;
	}
	/*
	 * 初始化制定对象，未指定代表全部对象，即当前转载单元的全部对象
	 * @private
	 */
	function ScriptLoader_initialize(object){
	    //也一定不存在。D存I存，D亡I亡
	    var dependenceMap = this.dependenceMap;
	    var loaderName = this.name;
	    var dependenceList = dependenceMap[0];
	    if(dependenceList){
	        //一定要用delete，彻底清除
	        delete dependenceMap[0];
	        var i = dependenceList.length;
	        while(i--){
	            //alert("ScriptLoader#initialize:"+loaderName+"/"+dep.getNames())
	            requireDependence(dependenceList[i]);
	        }
	    }
	    //这里进行了展开优化，有点冗余
	    if(object){//装载对象
	        if(dependenceList = dependenceMap[object]){
	            //一定要用delete，彻底清除
	            delete dependenceMap[object];
	            var i = dependenceList.length;
	            while(i--){
	                requireDependence(dependenceList[i]);
	            }
	        }
	        //谨慎，这里的i上面已经声明，不过，他们只有两种可能，undefined和0 
	        for(var i in dependenceMap){
	              break;
	        }
	        if(!i){
	            //initialize 不能delete
	            this.dependenceMap = this.initialize = 0;
	        }
	    }else{//装载脚本
	        for(var object in dependenceMap){
	            var dependenceList = dependenceMap[object];
	            delete dependenceMap[object];
	            var i = dependenceList.length;
	            while(i--){
	                requireDependence(dependenceList[i]);
	            }
	        }
	        //initialize 不能delete
	        this.dependenceMap = this.initialize = 0;
	    }
	}
	
	return function(path){
		if(path){
	        var packageObject = findPackageByPath(path);
	        var objectName = path.substr(packageObject.name.length+1);
	        packageObject = realPackage(packageObject);
	        if(path.indexOf('/')+1){//path.indexOf('/') == -1
	            writeRequire(packageObject,objectName,null);
	        }else{
	            if(objectName == '*'){
	                for(var fileName in packageObject.scriptObjectMap){
	                    writeRequire(packageObject,fileName,null);
	                }
	            }else{
	                writeRequire(packageObject,packageObject.objectScriptMap[objectName],objectName);
	            }
	        }
		}
	}
}