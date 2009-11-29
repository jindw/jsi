/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: jsidoc.js,v 1.9 2008/02/28 14:39:09 jindw Exp $
 */
/**
 * @param packageGroupMap {"groupName":['example',"example.internal"]}
 */
function initializePackageFromQuery(packageGroupMap){
    var search = window.location.search;
    var hit = false;
    if(search && search.length>2){
        var exp = /([^\?=&]*)=([^=&]*)/g;
        var match;
        while(match = exp.exec(search)){
            var groupName = decodeURIComponent(match[1]);
            var value = decodeURIComponent(match[2]);
            if(value){
            	if(groupName == "group"){
                    value = JSON.parse(value);
                    for(groupName in value){
	                    packageGroupMap.push(groupName)
	                    packageGroupMap[groupName] = value[groupName];
	                    hit = true;
                    }
                }else if(groupName = groupName.replace(/^group\.(.+)|.+/,'$1')){//old 
                    packageGroupMap.push(groupName)
                    packageGroupMap[groupName] = value.split(',');
                    hit = true;
                }
            }
        }
    }
    return hit;
}
function initializePackageAndDataDataFromHash(packageGroupMap){
    var data,win = parent;
    while(win && win != top){
        data = win.location.hash;
        if(data){
            var packageMap = JSON.parse(data.substring(1));
            var groupName ="未命名分组";
            packageGroupMap.push(groupName);
            var groupPackages = packageGroupMap[groupName] = [];
            for(var packageName in packageMap){
            	if(packageName){
                    groupPackages.push(packageName);
            	}
                preload(packageName,packageMap[packageName]);
            }
            return true;
        }
        win = win.parent;
    }
}
function initializeHistory(){
    var contentWindow = document.getElementById("content").contentWindow;
	
	setInterval(function(){
		if(initializeURL){
			contentWindow.location.replace(getTrueHref(initializeURL));
			initializeURL = null;
		}
	},100);
	if(checkInterval){
    	clearInterval(checkInterval);
    }
    /**
     * >/       ==       </   #
     *     1    ==  2
     */
	checkInterval = setInterval(function(){
		var offset = getHistoryOffset();
		var i = 10;
		while(offset && i--){
			top.history.go(offset);
			if(getHistoryOffset() == 0){
				var dest = getTrueHref(checkLocation.hash.substring(1));
				contentWindow.location.replace(dest)
				//抛掉向前记录
				//contentWindow.history.go(offset);
				break;
			}
		}
		
	},100);
}
function getHistoryOffset(){
	var hash = checkLocation.hash;
	var flag = hash.substring(1,3);
	if(flag == '>/'){
		return 1;

	}else if(flag == '</'){
		return -1;
	}else{
		return 0;
	}
}
function getTrueHref(href){
	if(href.substring(0,2) == ":/"){
        href = urlPrefix+ href.substring(2);
	}
	return href;
}
function getPureHref(contentHref){
	var pos = contentHref.lastIndexOf('#');
	if(pos>0){
		var url = checkLocation.protocol+checkLocation.host
		contentHref = contentHref.substring(0,pos)
	}
	if(contentHref.indexOf(urlPrefix) ==0){
		contentHref = ":/"+contentHref.substring(urlPrefix.length)
	}
	return contentHref;
}
var MENU_FRAME_ID = "menu";
var CONTENT_FRAME_ID = "content";
//var loadingHTML = '<img style="margin:40%" src="../styles/loading2.gif"/>';

var checkInterval;
var checkLocation = top.location;
var initializeURL = (checkLocation.hash || '').substr(1);
var urlPrefix = location.href;
urlPrefix = urlPrefix.replace(/[^\/]*[\?#][\s\S]+/,'');

/**
 * @public
 */
var JSIDoc = {
    /**
     * @return packageGroupMap 或者 false(发现有外部脚本文件)
     */
    prepare:function(){
        var packageGroupMap = [];
        initializePackageAndDataDataFromHash(packageGroupMap) || initializePackageFromQuery(packageGroupMap);
        //setup url history
        if(packageGroupMap.length == 0){
            var dkey = "托管脚本示例";
            packageGroupMap.push(dkey);
            packageGroupMap[dkey]= ["example","example.alias","example.internal","example.dependence"]
        }
        JSIDoc.addPackageMap(packageGroupMap);
        document.getElementById("menu").setAttribute("src","html/controller.html?@menu");
        initializeHistory();
    },
    
    /**
     * @public
     * @param packageGroupMap 包含那些包组
     * @param findDependence 是否查找依赖，来收集其他包
     */
    addPackageMap : function(packageGroupMap,findDependence){
        this.rootInfo = PackageInfo.requireRoot();
        this.packageInfoGroupMap = this.packageInfoGroupMap || [];
        this.packageInfoMap = this.packageInfoMap || {};
        for(var i = 0;i<packageGroupMap.length;i++){
            var key = packageGroupMap[i]
            var packages = packageGroupMap[key];
            var packages = findPackages(packages,findDependence);
            var packageInfos = this.packageInfoGroupMap[key];
            if(!packageInfos){
            	 this.packageInfoGroupMap[key] = packageInfos = [];
            	 this.packageInfoGroupMap.push(key);
            }
            outer:
            for(var j = 0;j<packages.length;j++){
                var packageInfo = PackageInfo.require(packages[j])
                var k=packageInfos.length;
                while(k--){
                	if(packageInfos[k] == packageInfo){
                		continue outer;
                	}
                }
                packageInfos.push(packageInfo);
                this.packageInfoMap[packageInfo.name] = packageInfo;
            }
        }
    },
    /**
     * 折叠packageMenu的函数
     */
    collapsePackage:function(name){
        var menuDocument = document.getElementById(MENU_FRAME_ID).contentWindow.document;
        MenuUI.loadPackage(menuDocument,name);
    },
    jump:function(a){
    	if(a.getAttribute('href').charAt() != '#'){
	    	var url = '#'+encodeURIComponent(getPureHref(a.href));
	    	checkLocation.hash = ">/"+url;
    	}
    },
    /**
     * 渲染文档，输出页面
     * @param document 目标文档,menu 或content frame 的文档
     */
    render:function(document){
        var path = document.location.href;
        path = path.replace(/^([^#\?]+[#\?])([^#]+)(#.*)?$/g,"$2");
        if(path == "@menu"){
            document.write(this.genMenu());
        }else{
        	var url = '#'+encodeURIComponent(getPureHref(document.location.href));
        	//if(window.ActiveXObject){
        	checkLocation.hash = "</"+url;
        	checkLocation.hash = url;
        	//}
        	if(path == "@export"){
	            document.write(this.genExport(document));
	        }else{
	            var pos = path.lastIndexOf('/');
	            if(pos>=0){
	                var packageName = path.substr(0,pos).replace(/\//g,'.');
	                var fileName = path.substr(pos+1);
	                document.write(this.genSource(packageName,fileName));
	            }else{
	                var data = path.split(':')
	                if(data[1]){
	                    document.write(this.genObject(data[0],data[1]));
	                }else{
	                    this.genPackage(data[0],document);
	                }
	            }
	        }
        }
	    document.close();
    },

    /**
     * @public
     */
    genMenu : function(){
        var template = templateMap.menu;
        //out.open();
        var text =  template.render(
        {
            JSIDoc:JSIDoc,
            rootInfo:JSIDoc.rootInfo,
            packageInfoGroupMap:JSIDoc.packageInfoGroupMap
        });
        //alert(text)
        return text;
        //out.close();
    },
    
    /**
     * @public
     */
    genExport : function(document){
        var template = templateMap['export'];
        var packageNames = [];
        for(var name in this.packageInfoMap){
            packageNames.push(name);
        }
        //out.open();
        var text =  template.render({packageNodes:ExportUI.prepare(document,packageNames)});
        //alert(text)
        return text;
        //out.close();
    },
    /**
     * @public
     */
    genPackage : function(packageName,document){
        var template = templateMap['package'];
        var packageInfo = PackageInfo.require(packageName);
        //TODO: 有待改进
        var tasks = packageInfo.getInitializers();
        tasks.push(function(){
            var infos = packageInfo.getObjectInfos();
            var constructors = [];
            var functions = [];
            var objects = [];
            for(var i=0;i<infos.length;i++){
                var info = infos[i];
                switch(info.type){
                    case "constructor":
                        constructors.push(info);
                        break;
                    case "function":
                        functions.push(info);
                        break;
                    default:
                        objects.push(info);
                }
            }
            //out.open();
            var data = template.render(
            {
                constructors:constructors,
                functions:functions,
                objects:objects,
                files:packageInfo.fileInfos,
                packageInfo:packageInfo
            });
            document.open();
            document.write(data);
            document.close();
            //out.close();
        });
        var pos = 0;
        function run(){
        	//$log.debug([pos,tasks.length])
            if(pos<tasks.length){
                tasks[pos++]();
                run();
            }
        }
        run();
        //runTasks(tasks);
        //return "<html><body><h1>装在中......</h1></body></html>"
    },
    
    /**
     * @public
     */
    genObject : function(packageName,objectName){
        var packageInfo = PackageInfo.require(packageName);
        var objectInfo = packageInfo.getObjectInfoMap()[objectName];
        switch(objectInfo.type){
            case "constructor":
                var template = templateMap['constructor'];
                break;
            case "function":
                var template = templateMap['function'];
                break;
            case "object":
                var template = templateMap.object;
                break;
            default:
                var template = templateMap['native'];
        }
        //out.open();
        return template.render({
            objectInfo:objectInfo
        });
    },
    
    
    /**
     * @public
     */
    genSource : function(packageName,file){
        var sourceEntry = SourceEntry.require(file,packageName);
        var source = sourceEntry.source.toString();
        var ds = sourceEntry.docEntries;
        var anchors = [];
        //build a list
        //var anchorPrefix = getAnchorPrefix(out);
        if(ds!=null&&ds[0]!=null){
            for(var i = 0;i<ds.length;i++){
                var id = ds[i].getId();
                if(id && id.length>0){
                    anchors.push({name:id,position:ds[i].end});
                }
            }
        }
        sourceEntry.anchors = anchors;
        var lines = sourceEntry.parse();
        var template = templateMap.source;
        template.beforeOutput = function(){
            //JSIDoc.context.evalString = function(){};
        }
        //out.open();
        return template.render(
        {
            JSIDoc:JSIDoc,
            lines:lines
        });
    },
    /**
     * 获取文档源代码
     * @param packageName
     * @param fileName
     */
    getSource:function(filePath){
    	filePath = filePath.replace(/^\//,'');
        var cache = cachedScripts[filePath];
        if(cache && cache.constructor == String){
            return cache;
        }else{
            var result = loadText($JSI.scriptBase +filePath);
            if(result !=null){
            	preload(filePath.replace(/\/[^\/]+$/,'').replace(/\//g,'.'),
            	    filePath.replace(/.*\//,''),result)
                return result;
            }else if(cache){
                return cache.toString();
            }
        }
    },
    
    
    /**
     * 
     */

    exportToJSI:function(newJSI){
        for(var path in cachedScripts){
            var items = path.split('/');
            var file = items.pop();
            var packageName = items.join('.');
            newJSI.preload(packageName,file == "__package__.js"?'':file,cachedScripts[path]);
        }
    }
}

var documentMap = {};
var scriptBase = this.scriptBase
var documentBase = (scriptBase + 'html/').substr($JSI.scriptBase.length);
var jsiCacher = $JSI.preload;
var cachedScripts = {};




function preload(pkg,file2dataMap,value){
    jsiCacher.apply($JSI,arguments);
    var base = pkg.replace(/\.|(.)$/g,'$1/');
    if(value == null){//null避免空串影响
        for(var n in file2dataMap){
            cachedScripts[base + (n || "__package__.js")] = file2dataMap[n];
        }
    }else{
        cachedScripts[base + (file2dataMap || "__package__.js")] = value;
    }

};
$JSI.preload = preload;
