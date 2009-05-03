/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: export.js,v 1.8 2008/02/24 08:58:15 jindw Exp $
 */

function Exporter(){
    this.imports = [];
    this.externPackage = [];
    this.result = [];
    this.cachedInfos = [];
    this.featrueMap = {};
}
Exporter.prototype = {
    addImport : function(path){
    	if(/.\*$/.test(path)){
    		var packageName = path.substr(0,path.length-2).replace(/\//g,'.');
    		var packageObject = $import(packageName+':');
    		var packagePath = packageObject.name.replace(/\./g,'/')+'/';
    		for(var fileName in packageObject.scriptObjectMap){
    			this.addImport(packagePath + fileName);
    		}
    	}else if(/.:$/.test(path)){
    		this.externPackage.push(path.substring(0,path.length-1));
    	}else{
            this.imports.push(path);
            addDependenceInfo(new DependenceInfo(path),this.result,this.cachedInfos)
    	}
    },
    getResult : function(){
        return this.result;
    },
    addFeatrue : function(featrue){
        this.featrueMap[featrue] = true;
    },
    buildSourceFilter : function(){
        var list = [];
        function filter(text,path){
            var i=list.length
            while(i--){
                text = list[i](text,path);
            }
            return text;
        }
        if(this.featrueMap.mixTemplate){
            list.push(defaultTemplateFilter);
        }
        list.reverse();
        return filter;
    },
    getTextContent : function(){
        var content = [];
        var objectMap = {}
        var conflictMap = {};
        var compileFilter = this.buildSourceFilter();
        for(var i = 0;i<this.result.length;i++){
            var path = this.result[i];
            var vars = findGlobals(content[i] = this.getSource(path,compileFilter));
            var j = vars.length;
            while(j--){
                var n = vars[j];
                if(objectMap[n]){
                    if(!conflictMap[n]){
                        conflictMap[n] = [path];
                    }else{
                        conflictMap[n].push(path);
                    }
                }else{
                    objectMap[n] = path;
                }
            }
        }
        for(var n in conflictMap){
            var report = ["直接合并可能引起脚本冲突，客户端检测到可能的脚本冲突如下：\n"];
            for(var n in conflictMap){
                report.push(n,":",objectMap[n],',',conflictMap[n],'\n');
            }
            confirm(report.join(''))
            break;
        }
        return content.join('\n')
    },
    getImports:function(){
    	return this.imports
    },
    getXMLContent : function(){
        var packageMap = {};
        var packageList = [];
        var compileFilter = this.buildSourceFilter();
        var content = ['<?xml version="1.0" encoding="UTF-8"?>\n',
			'<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">\n',
			"<properties>\n"];
        //appendEntry(content,'#export',this.imports.join(','));
        for(var i = 0;i<this.result.length;i++){
            var path = this.result[i];
            var packageName = path.replace(/\/[^\/\/]+$/,"").replace(/\//g,'.');
            if(!packageMap[packageName]){
                packageMap[packageName] = true;
                packageList.push(packageName)
            }
            
            var text = this.getSource(path,compileFilter);
            appendEntry(content,path,text);
        }
        for(var i = 0;i<this.externPackage.length;i++){
        	var packageName = this.externPackage[i];
        	if(!packageMap[packageName]){
                packageMap[packageName] = true;
                packageList.push(packageName)
            }
        }
        //alert("xxx.yyy.zz".replace(/\./g,'/'));
        //alert("xxx.yyy.zz".replace(/\./g,'/'));
        
        //mozilla bug fix
        //Why?
        ''.replace(/\./g,'/');
        packageList = findPackages(packageList,true);
        for(var i = 0;i<packageList.length;i++){
            var path = packageList[i].replace(/\./g,'/')+"/__package__.js";
            var text = this.getSource(path,compileFilter);
            appendEntry(content,path,text);
        }
        content.push("</properties>\n");
        return content.join('')
    },
    getDocumentContent : function(jsiDocURL){
        var packageMap = {};
        var packageList = [];
        var compileFilter = this.buildSourceFilter();
        for(var i = 0;i<this.result.length;i++){
            var path = this.result[i];
            var packageName = path.replace(/\/[^\/\/]+$/,"").replace(/\//g,'.');
            if(!packageMap[packageName]){
                packageMap[packageName] = {};
                packageList.push(packageName)
            }
        }
        packageList = findPackages(packageList,true);
        for(var i = 0;i<packageList.length;i++){
            var packageName = packageList[i];
            var base = packageName.replace(/\.|$/g,'/')
            var packageObject = $import(packageName + ':');
            packageMap[packageName] = {'':this.getSource(base+"__package__.js")}
            for(var file in packageObject.scriptObjectMap){
                var text = this.getSource(base + file,compileFilter);
                packageMap[packageName][file] = text;
            }
        }
        return getTemplate("export-doc").render({
            documentURL:jsiDocURL,
            data:JSON.encode(packageMap)
        });
    },
    getSource:function(path,compileFilter){
        if(parent.JSIDoc && parent.JSIDoc.getSource){
           var parentJSIDoc = parent.JSIDoc;
        }else if(window.JSIDoc &&window.JSIDoc.getSource){
           var parentJSIDoc = window.JSIDoc;
        }
        if(parentJSIDoc && parentJSIDoc.getSource){
            //$log.info(packageName,path.substr(packageName.length+1));
            var rtv = parentJSIDoc.getSource(path);
        }else{
            var rtv = loadTextByURL($JSI.scriptBase+"?path=" +path);
        }
        if(rtv == null){
            $log.error("装载源代码失败:",path);
        }
        if(compileFilter){
            rtv = compileFilter(rtv,path);
            
        }
        return rtv;
    }
}


var encodeMap = {
    '--':'\\u002d-',
    '<':'\\u003c',
    '&':'\\u0026',
    '>':'\\u003e'
}
var templateRegexp = /new\s+Template\s*\(((?:[\w\.\+\s]+|'.*?'|".*?")+)\)/g;
function defaultTemplateFilter(text,path){
    if(templateRegexp.test(text)){
    
        $import(path,{});
        var packageObject = $import(path.replace(/\/[^\/]+$/,':').replace(/\//g,'.'));
        var loader = packageObject.loaderMap[path.replace(/.*\//,'')];
        
        text = text.replace(templateRegexp,function(templateCode,path){
            try{
                if(path && (typeof loader.hook(path) == 'string')){
                    var object = loader.hook(templateCode);
                    if(object && (object = object.compileData)){
                    	if(object instanceof Function){
                    		object = object.toString();
                    	}else{
                    		object = JSON.encode(object);
                    	}
                    	
                        return "new Template"+"("+object+")";
                        
                    }
                }
            }catch(e){$log.error("替换出错：",templateCode)}
            
            return templateCode;
        });
        
    }
    return text;
}

function encodeReplacer(c){
    return encodeMap[c];
}
function appendEntry(content,path,text){
	content.push("<entry key='",path,"'>") ;
    content.push(/[<>&]/.test(text) && text.indexOf(']]>')<0?
	    "<![CDATA["+text + ']]>'
	    :text.replace(/[<>&]/g,xmlReplacer));
    content.push("</entry>\n");
}

function addDependenceInfo(dependenceInfo,result,cachedInfos){
    var befores = dependenceInfo.getBeforeInfos();
    var i = befores.length;
    dependenceLoop:
    while(i--){
        var item = befores[i];
        var j = cachedInfos.length;
        while(j--){
            if(cachedInfos[j].implicit(dependenceInfo)){//一旦发现自己已被包含，可以立即跳出判断（需要后续条件）
                return;
            }
            if(cachedInfos[j].implicit(item)){
                continue dependenceLoop;
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
            if(cachedInfos[j].implicit(item)){//后面的一定不能中途跳出，否者前面的跳出条件不充分
                continue dependenceLoop;
            }
        }
        addDependenceInfo(item,result,cachedInfos);
    }
    
}

