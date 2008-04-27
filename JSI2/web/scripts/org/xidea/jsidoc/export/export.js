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
    getTextContent : function(){
        var content = [];
        for(var i = 0;i<this.result.length;i++){
            content[i] = this.getSource(this.result[i]);
        }
        return content.join('\n')
    },
    getXMLContent : function(){
        var content = ["<script-map export='",this.imports.join(','),"'>\n"];
        var packageMap = {};
        var packageList = [];
        for(var i = 0;i<this.result.length;i++){
            var path = this.result[i];
            var packageName = path.replace(/\/[^\/\/]+$/,"").replace(/\//g,'.');
            if(!packageMap[packageName]){
                packageMap[packageName] = true;
                packageList.push(packageName)
            }
            var txt = this.getSource(path);
            content.push("<script path='",path,"'>") ;
            content.push(txt.replace(/[<>&]/g,xmlReplacer));
            content.push("</script>\n");
        }
        packageList = findPackages(packageList,true);
        for(var i = 0;i<packageList.length;i++){
            var path = packageList[i].replace(/\./g,'/')+"/__package__.js";
            var txt = this.getSource(path);
            content.push("<script path='",path,"'>") ;
            content.push(txt.replace(/[<>&]/g,xmlReplacer));
            content.push("</script>\n");
        }
        content.push("</script-map>\n");
        return content.join('')
    },
    getDocumentContent : function(jsiDocURL){
        var packageMap = {};
        var packageList = [];
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
                var txt = this.getSource(base + file);
                packageMap[packageName][file] = txt;
            }
        }
        var content = ["/*<meta http-equiv='Content-Type' content='text/html;utf-8' />"];
        content.push("<meta http-equiv='X-JSIDoc-Version' content='1.0' />");
        content.push("<script>document.onkeydown = function(){text.focus();text.select();};");
        content.push("var documentURL = '",jsiDocURL,"?externalScript='+encodeURIComponent(location.href);");
        content.push("function printDocument(){document.open();");
        content.push("document.write(\"<html><frameset rows='100%'><frame src='\"+documentURL+\"'></frame></frameset></html>\");");
        content.push("document.close();}");
        
        content.push("if(location.protocol!='file:'){");
        content.push("printDocument();setTimeout(printDocument,10);}else{");
        content.push("var script = document.getElementsByTagName('script')[0];");
        content.push("var preText = script.previousSibling;");
        content.push("preText.parentNode.removeChild(preText);");
        content.push("}</script><textarea onfocus='this.select()' onclick='this.select()' wrap='off' style='position:absolute;top:100px;width:100%;height:60px;'>/* */");
        content.push("JSIDoc.cacheScript(");
        content.push(JSON.serialize(packageMap).replace(/[<&>]|--/g,encodeReplacer));
        content.push(")\r\n");
        content.push("//</textarea>");
        
        content.push("<script>");
        content.push("var text = document.getElementsByTagName('textarea')[0];");
        content.push("if(window.clipboardData){");
        content.push("clipboardData.setData('Text',text.value);");
        content.push("printDocument();setTimeout(printDocument,10);");
        content.push("}</script>");
        content.push("<div style='position:absolute;top:0px;height:100px;'><h3>您的浏览器可能不支持本地脚本读取</h3>")
        content.push("<p>您需要拷贝(Ctrl+C)文本筐中的脚本数据,确认后在提示筐中输入拷贝脚本<button onclick='printDocument();'>确认</button></p>");
        content.push("</div>");
        content.push("");
        return content.join('')
    },
    getSource:function(path){
        if(parentJSIDoc && parentJSIDoc.getSource){
            //$log.info(packageName,path.substr(packageName.length+1));
            var rtv = parentJSIDoc.getSource(path);
        }else{
            var rtv = JSIDoc.loadTextByURL($JSI.scriptBase +path);
        }
        if(rtv == null){
            $log.error("装载源代码失败:",path);
        }
        return rtv;
    }
}
function encodeReplacer(c){
    return encodeMap[c];
}
var encodeMap = {
    '--':'\\u002d-',
    '<':'\\u003c',
    '&':'\\u0026',
    '>':'\\u003e'
}
try{
   var parentJSIDoc = parent.JSIDoc;
}catch(e){
}

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

