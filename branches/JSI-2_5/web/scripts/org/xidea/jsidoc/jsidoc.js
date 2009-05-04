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
            var name = decodeURIComponent(match[1]);
            var value = decodeURIComponent(match[2]);
            if(value){
            	if(name == "group"){
                    value = JSON.decode(value);
                    for(name in value){
	                    packageGroupMap.push(name)
	                    packageGroupMap[name] = value[name];
	                    hit = true;
                    }
                }else if(name = name.replace(/^group\.(.+)|.+/,'$1')){//old 
                    packageGroupMap.push(name)
                    packageGroupMap[name] = value.split(',');
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
            var packageMap = JSON.decode(data.substring(1));
            var groupName ="未命名分组";
            packageGroupMap.push(groupName);
            groupPackages = packageGroupMap[groupName] = [];
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
	if(checkInterval){
    	clearInterval(checkInterval);
    }
	checkInterval = setInterval(function(){
		var url = decodeURIComponent(checkLocation.hash.substr(1));
		var contentLocation = contentWindow.location;
		if(url && url != contentLocation.href){
            contentLocation.replace(url);
		}
	},100);
}
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
        initializeHistory();
        if(packageGroupMap.length == 0){
            var dkey = "托管脚本示例";
            packageGroupMap.push(dkey);
            packageGroupMap[dkey]= ["example","example.alias","example.internal","example.dependence"]
        }
        JSIDoc.addPackageMap(packageGroupMap);
        setTimeout(function(){
            document.getElementById("menu").setAttribute("src","html/controller.html?@menu");
        },100)
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
        	var url = '#'+encodeURIComponent(document.location.href);
        	checkLocation.hash = url;
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
    },

    /**
     * @public
     */
    genMenu : function(){
        var template = getTemplate("menu.xhtml");
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
        var template = getTemplate("export.xhtml");
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
        var template = getTemplate("package.xhtml");
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
                var template = getTemplate("constructor.xhtml");
                break;
            case "function":
                var template = getTemplate("function.xhtml");
                break;
            case "object":
                var template = getTemplate("object.xhtml");
                break;
            default:
                var template = getTemplate("native.xhtml");
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
        var template = getTemplate("source.xhtml");
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
            var result = loadTextByURL($JSI.scriptBase +"?path="+ filePath);
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

var MENU_FRAME_ID = "menu";
var CONTENT_FRAME_ID = "content";
//var loadingHTML = '<img style="margin:40%" src="../styles/loading2.gif"/>';

var win = window;
var checkLocation = win.location;
var checkInterval;

while(win!=win.top){
	try{
		win.parent.document.forms.length;
	}catch(e){
		break;
	}
	win = win.parent;
}
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