/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: globals-info.js,v 1.3 2008/02/24 08:58:15 jindw Exp $
 */


var packageName = this.scriptBase.substr($JSI.scriptBase.length).replace(/\//g,'.').replace(/\.$/,':');
var thisPkg = $import(packageName);
var Core = {
    $JSI:{},
    Package : function (){},
    $import : function (){},
    ScriptLoader : this.constructor,
    XMLHttpRequest:function(){}
};
var enmpyFunction = function(){};
Core.XMLHttpRequest.prototype = {
    readyState:0,
    responseText:"",
    responseXML:"",
    status:"",
    statusText:"",
    
    onreadystatechange:enmpyFunction,
    
    abort:enmpyFunction,
    getAllResponseHeaders:enmpyFunction,
    getResponseHeader:enmpyFunction,
    open:enmpyFunction,
    send:enmpyFunction,
    setRequestHeader:enmpyFunction
}
for(var n in thisPkg){
    if(!thisPkg.hasOwnProperty(n)){
        Core.Package.prototype[n] = thisPkg[n];
    }
}


/**
 * @public
 */
function RootInfo(bootScript){
    var freeScript = bootScript || collectScripts();
    var sourceEntryList = [];
    this.fileInfos = [];
    for(var i=0;i<freeScript.length;i++){
        try{
            var info = new FreeFileInfo(this,freeScript[i],sourceEntryList);
            this.fileInfos.push(info);
            this.fileInfos[freeScript[i]] = info;
        }catch(e){}
    }
    this.fileInfos.sort();
    this.name = "";
    this.dependences = [];
}
RootInfo.prototype = createPrototypeStack(PackageInfo,RootInfo);


function collectScripts(){
    var scripts = document.getElementsByTagName('script');
    var freeScript = [];
    var scriptMap = {}
    for(var i=0;i<scripts.length;i++){
        var s = scripts[i];
        if(s.src){
            freeScript.push(s.src);
            scriptMap[s.src] = true;
        }
    }
    //for mozilla document.write bug
    for(var i=0;i<scripts.length;i++){
        var s = scripts[i];
        while(s=s.nextSibling){
            if(s.nodeType == 1){
                if(s.tagName == "SCRIPT"){
                    if(s.src){
                        if(scriptMap[s.src] || /__preload__\.js$/.test(s.src)){
                            continue;
                        }else{
                            freeScript.push(s.src);
                            scriptMap[s.src] = true;
                        }
                    }
                }else{
                    break;
                }
            }
        }
    }
    //alert(freeScript.join('\n'))
    for(var i=0;i<freeScript.length;i++){
        freeScript[i] = computeURL(freeScript[i]).replace($JSI.scriptBase,'');
    }
    return freeScript;
}

/**
 * @public
 */
RootInfo.prototype.getDocEntry = function(name){
    for(var i=this.fileInfos.length-1;i>=0;i--){
        var d = this.fileInfos[i]._sourceEntry.getDocEntry(name);
        if(d){
            return d;
        }
    }
};
/**
 * @public
 */
function FreeFileInfo(rootInfo,fileName,sourceEntryList){
    var sourceEntry = this._sourceEntry = SourceEntry.require(fileName);
    var topDocEntries = sourceEntry.getTopDocEntries();
    this.name = fileName;
    this.packageInfo = rootInfo;
    this.objects = [];
    this.dependences = [];
    this._depInf = {};
    this.sourceEntryList = sourceEntryList;
    sourceEntryList.push(sourceEntry);
    for(var i = 0;i<topDocEntries.length;i++){
        var name = topDocEntries[i].getId();
        this.objects.push(name);
    }
}

FreeFileInfo.prototype = createPrototypeStack(FileInfo,FreeFileInfo);

/**
 * @public
 */
FreeFileInfo.prototype.getObjectInfoMap = function(){
    if(this._objectInfoMap){
        return this._objectInfoMap;
    }
    try{
        this._objectInfoMap = {}
        for(var i = 0;i<this.objects.length;i++){
            this._objectInfoMap[this.objects[i]] = ObjectInfo.create(this,this.objects[i]);
        }
        return this._objectInfoMap;
    }catch(e){
        $log.error(e);
    }
}


/**
 * @public
 */
FreeFileInfo.prototype.getAvailableObjectFileInfoMap = function(){
    var root = this.packageInfo;
    if(!root._availableOFMap){
        root._availableOFMap = {};
        var fis =root.fileInfos;
        for(var i=0;i<fis.length;i++){
            var os = fis[i].objects;
            for(var j=0;j<os.length;j++){
                root._availableOFMap[os[j]] = fis[i];
            }
        }
    }
    return root._availableOFMap;
}

/**
 * @public
 */
FreeFileInfo.prototype.getObjectMap = function(){
    if(this._objectMap){
        return this._objectMap;
    }
    try{
        this._objectMap = {};
        for(var i=0;i<this.objects.length;i++){
            this._objectMap[this.objects[i]] = Core[this.objects[i]] || window[this.objects[i]] 
        }
    }catch(e){
        $log.error(e);
    }
    return this._objectMap;
}


/**
 * @public
 */
FreeFileInfo.prototype.getDocEntry = function(name){
    return this.packageInfo.getDocEntry(name);
};


/**
 * @public
 */
FreeFileInfo.prototype.getObject = function(name){
    return this.getObjectMap()[name];
}


