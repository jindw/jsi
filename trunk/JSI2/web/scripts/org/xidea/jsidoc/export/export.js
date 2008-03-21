/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: export.js,v 1.8 2008/02/24 08:58:15 jindw Exp $
 */

function Exporter(){
    this.imports = [];
    this.result = [];
    this.cachedInfos = [];
}
Exporter.prototype = {
    addImport : function(path){
        this.imports.push(path);
        addDependenceInfo(new DependenceInfo(path),this.result,this.cachedInfos)
    },
    getResult : function(){
        return this.result;
    },
//    isLoaded : function(path){
//        var packageFileObject = parsePath(path);
//    },
    getContent : function(){
        var content = [];
        for(var i = 0;i<this.result.length;i++){
            content[i] = this.getSource(this.result[i]);
        }
        return content.join('\n')
    },
    getFileMap : function(){
        var content = ["<script-map import='",this.imports.join(','),"'>\n"];
        var packageFileMap = {};
        for(var i = 0;i<this.result.length;i++){
            var path = this.result[i];
            var packagePath = path.replace(/[^\/\/]+$/,"__package__.js");
            if(packageFileMap[packagePath]){
                packagePath = null;
            }else{
                packageFileMap[packagePath] = true;
            }
            do{
                var txt = this.getSource(path);
                content.push("<script path='",path,"'>") ;
                content.push(txt.replace(/[<>&]/g,xmlReplacer));
                content.push("</script>\n");
            }while(path != packagePath && (path = packagePath))
        }
        content.push("</script-map>\n");
        return content.join('')
    },
    getSource:function(path){
        if(parentJSIDoc && parentJSIDoc.getSource){
            var packageName = path.substr(0,path.lastIndexOf('/')).replace(/\//g,'.');
            //$log.info(packageName,path.substr(packageName.length+1));
            var rtv = parentJSIDoc.getSource(packageName,path.substr(packageName.length+1));
        }else{
            var rtv = loadTextByURL($JSI.scriptBase +path);
        }
        if(rtv == null){
            $log.error("装载源代码失败:",path);
        }
        return rtv;
    }
}

var parentJSIDoc = parent.JSIDoc;


/*

    getResource : function(packageName,path){
        var xhr = new XMLHttpRequest();
        xhr.open('GET',$JSI.scriptBase + (packageName?packageName.replace(/\.|$/g,'/'):'')+path,false);
        xhr.send('')
        return xhr.responseText;
    }
 */
function addDependenceInfo(dependenceInfo,result,cachedInfos){
    var befores = dependenceInfo.getBeforeInfos();
    var i = befores.length;
    dependenceLoop:
    while(i--){
        var item = befores[i];
        var j = cachedInfos.length;
        while(j--){
            if(cachedInfos[j].implicit(item)){
                continue dependenceLoop;
            }
            if(cachedInfos[j].implicit(dependenceInfo)){
                return;
            }
        }
        addDependenceInfo(item,result,cachedInfos);
    }
    
    cachedInfos.push(dependenceInfo);
    var path = dependenceInfo.filePath;
    var i = result.length;
    while(i--){
        if(path == result[i]){
            i++;
            break;
        }
    }
    if(i<=0){//-1,0
        result.push(path);
    }
    var afters = dependenceInfo.getAfterInfos();
    var i = afters.length;
    dependenceLoop:
    while(i--){
        var item = afters[i];
        var j = cachedInfos.length;
        while(j--){
            if(cachedInfos[j].implicit(item)){
                continue dependenceLoop;
            }
        }
        addDependenceInfo(item,result,cachedInfos);
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
};