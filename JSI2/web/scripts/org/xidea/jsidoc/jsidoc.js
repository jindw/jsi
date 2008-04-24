/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: jsidoc.js,v 1.9 2008/02/28 14:39:09 jindw Exp $
 */

var loadingHTML = '<img style="margin:40%" src="../styles/loading2.gif"/>';

/**
 * @public
 */
var JSIDoc = {
    /**
     * @public
     * @param bootScripts 包含那些启动文件
     * @param packages 包含那些包
     * @param findDependence 是否查找依赖，来收集其他包
     */
    initialize : function(bootScripts,packages,findDependence){
        this.rootInfo = PackageInfo.requireRoot(bootScripts);
        var packages = findPackages(packages,!findDependence);
        this.packageInfos = [];
        this.packageNames = [].concat(packages);
        for(var i = 0;i<packages.length;i++){
            var pi = PackageInfo.require(packages[i])
            this.packageInfos.push(pi);
            this.packageInfos[pi.name] = pi;
        }
    },
    /**
     * 获取文档源代码
     * @param packageName
     * @param fileName
     */
    getSource:function(packageName,fileName){
        var xhr = new XMLHttpRequest();
        xhr.open('GET',$JSI.scriptBase + (packageName?packageName.replace(/\.|$/g,'/'):'')+fileName,false);
        xhr.send('')
        return xhr.responseText;
    },
    /**
     * 渲染文档，输出页面
     * @param document
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
            packageInfos:JSIDoc.packageInfos
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
    }

        
}
var templateMap = {};
var documentMap = {};
var scriptBase = this.scriptBase;
var resourceBase = scriptBase + 'html/';
var packageName = scriptBase.substr($JSI.scriptBase.length).replace(/\//g,'.').replace(/\.$/,'');
var resourcePackageName = packageName + '.html';
var jsiCacher = $JSI.preload;
var cachedScripts = {};
$JSI.preload = function(pkg,file2dataMap,value){
    if(cachedScripts[pkg]){ //比较少见
        pkg = cachedScripts[pkg];
        if(value == null){//null避免空串影响
            for(var n in file2dataMap){
                pkg[n] = file2dataMap[n];
            }
        }else{
            pkg[file2dataMap] = value;
        }
    }else {
        if(value == null){//null避免空串影响
            cachedScripts[pkg] = file2dataMap;
        }else{
            (cachedScripts[pkg] = {})[file2dataMap] = value;
        }
    }
};
/*
 * 获取脚本缓存。
 * @private
 * @param <string>packageName 包名
 * @param <string>fileName 文件名
 */
function getCacheScript(pkg,fileName){
    pkg = cachedScripts[pkg];
    return pkg && pkg[fileName];
};
/**
 * @internal
 */
function getTemplate(path){
    if(templateMap[path]){
        return templateMap[path]
    }
    var template = new Template(loadDocument(null,path));
    template.load  = loadDocument;
    return templateMap[path] = template;
}
function loadDocument(base,path){
    if(documentMap[path]){
        return documentMap[path]
    }
    var cache = JSIDoc.getSource(resourcePackageName,path)
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
