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
    	this.content = null;
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
    /**
     * 按需导出并直接合并源代码
     */
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
    /**
     * 将getContent 获取的数据转化为xml文件
     */
    getXMLContent : function(){
    	var content = this.getContent();
    	var result = [];
    	for(var n in content){
            result.push({path:n,content:content[n]});
    	}
        return exportDataTemplate.render({data:result});
    },
    /**
     * 按需导出部分URL，转化模板等
     */
    getContent : function(){
    	if(this.content){
    		return this.content;
    	}
    	var packageMap = {};
        var packageList = [];
        var compileFilter = this.buildSourceFilter();
        var content = {};
        //appendEntry(content,'#export',this.imports.join(','));
        for(var i = 0;i<this.result.length;i++){
            var path = this.result[i];
            var packageName = path.replace(/\/[^\/\/]+$/,"").replace(/\//g,'.');
            if(!packageMap[packageName]){
                packageMap[packageName] = true;
                packageList.push(packageName)
            }
            var text = this.getSource(path,compileFilter);
            content[path]=text;
        }
        for(var i = 0;i<this.externPackage.length;i++){
        	var packageName = this.externPackage[i];
        	if(!packageMap[packageName]){
                packageMap[packageName] = true;
                packageList.push(packageName)
            }
        }
        
        //mozilla bug fix
        //Why?
        ''.replace(/\./g,'/');
        packageList = findPackages(packageList,true);
        for(var i = 0;i<packageList.length;i++){
            var path = packageList[i].replace(/\./g,'/')+"/__package__.js";
            var text = this.getSource(path,compileFilter);
            content[path]=text;
        }
        this.content = content;
        return content;
    },
    /**
     * 需要抓取全部相关包的全部源代码（也包括未导出的部分）
     */
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
        return exportDocTemplate.render({
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


var templateRegexp = /\bnew\s+Template\s*\(/;
function defaultTemplateFilter(text,path){
	var templateBegin = text.search(templateRegexp);
	var packageName = path.replace(/\/[^\/]+$/,':').replace(/\//g,'.');
    var fileName = path.substring(packageName.length)
    if(fileName == '__package__.js'){
    	return text;
    }
    if(templateBegin >=0){
        $import(path,{});
        var result = [];
		var packageObject = $import(packageName);
        var loader = packageObject.loaderMap[fileName];
		do{
			result.push(text.substring(0,templateBegin));
			text = text.substring(templateBegin);
			tryCount = 32;
			var pathEnd=0;
			templateCode='';
			while(tryCount -- && (pathEnd=text.indexOf(')',pathEnd))){
				try{
					var templateCode = text.substring(0,pathEnd+1);
					new Function(templateCode);
					break;
				}catch(e){
					templateCode = '';
				}
			}
			if(templateCode){
				text = text.substring(templateCode.length);
				templateCode = getTemplateCode(loader,templateCode);
				result.push(templateCode);
			}else{
				templateCode = text.substring(0,Math.max(text.indexOf('('),1));
				result.push(templateCode)
				text = text.substring(templateCode.length);
			}
		}while((templateBegin = text.search(templateRegexp))>=0);
		result.push(text);
		text = result.join('')
    }
    return text;
}

function getTemplateCode(loader,templateCode){
	try{
		var path = templateCode.substring(templateCode.indexOf('('));
		path = loader.hook(path);
		if((typeof path == 'string')){
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
	}catch(e){
	}
	return templateCode;
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

/**
 * @internal
 */
function Template(path){
    if(path instanceof Function){
        this.data = path;
    }else{
        var t = $import('org.xidea.lite:Template',{} );
        t = new t(path);
        return t;
    }
}
Template.prototype.render = function(context){
    return this.data(context)
}
//alert(this.scriptBase.replace(/\w+\/$/,"html/export-data.xml"))
var exportDataTemplate = new Template(this.scriptBase+"../html/export-data.xml#//*[@id='properties']/*");
var exportDocTemplate = new Template(this.scriptBase+"../html/export-data.xml#//*[@id='document']/*");