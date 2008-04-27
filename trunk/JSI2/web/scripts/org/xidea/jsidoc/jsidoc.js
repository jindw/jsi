/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: jsidoc.js,v 1.9 2008/02/28 14:39:09 jindw Exp $
 */
var templateMap = {};
var documentMap = {};
var documentBase = (this.scriptBase + 'html/').substr($JSI.scriptBase.length);
var jsiCacher = $JSI.preload;
var cachedScripts = {};

var MENU_FRAME_ID = "menu";
var CONTENT_FRAME_ID = "content";
//var loadingHTML = '<img style="margin:40%" src="../styles/loading2.gif"/>';

/**
 * @public
 */
var JSIDoc = {
    /**
     * @return packageGroupMap 或者 false(发现有外部脚本文件)
     */
    prepare:function(){
        try{
            var link = window.location.hash || top.location.hash;
        }catch(e){}
        var search = window.location.search;
        var packageGroup = [];
        window.onload = function(){
            if(link && link.length>2){
                var content = document.getElementById("content");
                content.src = decodeURIComponent(link.substr(1));
                content.onload = contentLoad;
                link = '';
            }
        }
        function contentLoad(){
            if(document.all){
                try{
                    var location = top.location;
                }catch(e){
                    var location = window.location;
                }
            }
            
            if(location){
                location.replace("#"+encodeURIComponent(this.contentWindow.location.href));
            }
        }
        if(search && search.length>2){
            var exp = /([^\?=&]*)=([^=&]*)/g;
            var match;
            while(match = exp.exec(search)){
                var name = decodeURIComponent(match[1]);
                var value = decodeURIComponent(match[2]);
                if("externalScript" == name){
                    if(window.clipboardData && value.indexOf("file://") == 0){
                        var text = window.clipboardData.getData("Text");
                        document.write("<script>"+text+"<\/script>")
                    }else{
                        document.write("<script src='"+value+"'><\/script>")
                    }
                    return value
                }else if(name = name.replace(/^group\.(.*)|.*/,'$1')){//group
                    packageGroup.push(name)
                    packageGroup[name] = value.split(',');
                }
            }
        }
        return packageGroup;
    },
    /**
     * @public
     * @param packageGroupMap 包含那些包组
     * @param findDependence 是否查找依赖，来收集其他包
     */
    resetPackageMap : function(packageGroupMap,findDependence){
        this.rootInfo = PackageInfo.requireRoot();
        this.packageInfoGroupMap = [];
        this.packageInfoMap = {};
        if(!(packageGroupMap instanceof Array)){
            var packageGroupMap2 = [];
            for(var key in packageGroupMap){
                packageGroupMap2.push(key);
                packageGroupMap2[key] = packageGroupMap[key];
            }
            packageGroupMap = packageGroupMap2;
        }
        for(var i = 0;i<packageGroupMap.length;i++){
            var key = packageGroupMap[i]
            var packages = packageGroupMap[key];
            var packages = findPackages(packages,findDependence);
            var packageInfos = this.packageInfoGroupMap[key] = [];
            this.packageInfoGroupMap.push(key);
            for(var j = 0;j<packages.length;j++){
                var packageInfo = PackageInfo.require(packages[j])
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
     * 获取文档源代码
     * @param packageName
     * @param fileName
     */
    getSource:function(fileName){
        var cache = cachedScripts[fileName];
        if(cache && cache.constructor == String){
            return cache;
        }else{
            var result = this.loadTextByURL($JSI.scriptBase + fileName);
            if(result !=null){
                return result;
            }else if(cache){
                return cache.toString();
            }
        }
    },
    /**
     * 
     */
    loadTextByURL : function(url){
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
    },
    /**
     * @public
     */
    genMenu : function(){
        var template = getTemplate("menu.xhtml");
        //out.open();
        return template.render(
        {
            JSIDoc:JSIDoc,
            rootInfo:JSIDoc.rootInfo,
            packageInfoGroupMap:JSIDoc.packageInfoGroupMap
        });
        //out.close();
    },
    /**
     * @public
     */
    genPackage : function(packageName,document){
        var template = getTemplate("package.xhtml");
        var packageInfo = PackageInfo.require(packageName);
        function run(){
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
            try{
                document.documentElement.innerHTML = "";//for IE
                data = data.replace(/^(<[^<>]*>)?<html[^>]*>|<\/html>\s*$/,'');
                document.documentElement.innerHTML = data;
            }catch(e){
                document.open();
                document.write(data);
                document.close();
            }
            //out.close();
        }
        //TODO: 有待改进
        var tasks = packageInfo.getInitializers();
        tasks.push(run);
        for(var i=0;i<tasks.length;i++){
            tasks[i]();
        }
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
        return template.render(
        {
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
    cacheScript:function(packageMap,groupName){
        if(!this.packageGroupMap){
            this.packageGroupMap = {};
        }
        groupName = groupName||"未命名分组";
        var groupPackages = this.packageGroupMap[groupName];
        if(!groupPackages){
            groupPackages = this.packageGroupMap[groupName] = [];
        }
        for(var packageName in packageMap){
            groupPackages.push(packageName);
            preload(packageName,packageMap[packageName]);
        }
    },
    exportToJSI:function(newJSI){
        for(var path in cachedScripts){
            var items = path.split('/');
            var file = items.pop();
            var packageName = items.join('.');
            newJSI.preload(packageName,file == "__package__.js"?'':file,cachedScripts[path]);
        }
    }
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
/**
 * @internal
 */
function getTemplate(path){
    if(templateMap[path]){
        return templateMap[path]
    }
    var template = new Template(loadDocument(path));
    template.load  = loadDocument;
    return templateMap[path] = template;
}
function loadDocument(path){
    if(documentMap[path]){
        return documentMap[path]
    }
    var cache = JSIDoc.getSource(documentBase+path)
    return documentMap[path] = parseDocument(cache);
}
function parseDocument(value){
    if(/^[\s\ufeff]*</.test(value)){
        value = value.replace(/^[\s\ufeff]*</,'<');
    }else{
        value = new Request(value).send('',true).getResult();
    }
    if(window.DOMParser){//code for Mozilla, Firefox, Opera, etc.
        return new DOMParser().parseFromString(value,"text/xml");
    }else{
        var doc=new ActiveXObject("Microsoft.XMLDOM");
        doc.async="false";
        doc.loadXML(value);
        return doc;
    }
}
