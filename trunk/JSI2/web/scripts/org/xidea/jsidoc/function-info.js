/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: function-info.js,v 1.3 2008/02/19 12:54:00 jindw Exp $
 */

/**
 * 函数信息类
 * @param <FileInfo> fileInfo
 * @param <String> name
 * @param <Object> object
 * @param <DocEntry> docEntry
 * @public
 */
function FunctionInfo(fileInfo,name,object,docEntry){
    this.fileInfo = fileInfo;
    this.packageInfo = fileInfo.packageInfo;
    this.name = name;
    this.object = object;
    this.docEntry = docEntry;
}
FunctionInfo.prototype = createPrototypeStack(ObjectInfo,FunctionInfo);
FunctionInfo.prototype.getStaticInfos = function(){
    if(!this._staticInfos){
        this._staticInfos = MemberInfo.createMembers(this,true);
        for(var i=0;i<this._staticInfos.length;i++){
            if(this._staticInfos[i] == 'prototype'){
                this._staticInfos.splice(i,1);
                delete this._staticInfos['prototype'];
            }
        }
    }
    return this._staticInfos;
}
FunctionInfo.prototype.getConstructorInfo = function(){
    var functionInfo;
    return function(){
        if(!functionInfo){
            functionInfo = {}
            for(n in ConstructorInfo.prototype){
                functionInfo[n] = ConstructorInfo.prototype[n];
            }
            functionInfo.object = Function;
            functionInfo.docEntry = DocEntry.EMPTY;
        }
        return functionInfo;
    }
}();
FunctionInfo.prototype.getParams = function(){
    if(this._params){
        return this._params;
    }
    var params = this.docEntry.getParams();
    if(!params){
        if(this.object instanceof Function){
            try{
                params = /\(([^\(\)]*)\)/.exec(this.object.toString())[1].split(/\s*,\s*/);
            }catch(e){
                params = [];
            }
        }else{
            params = [];
        }
    }
    return this._params = new ParamInfo(params,this.fileInfo.getAvailableObjectFileInfoMap())
};
FunctionInfo.prototype.getArguments = function(){
    return this.docEntry.getArguments();
};
FunctionInfo.prototype.getReturnInfo = function(){
    if(!this._returnInfo){
        var returnInfo = this.docEntry.getReturn();
        returnInfo = new TypeInfo(returnInfo,this.fileInfo.getAvailableObjectFileInfoMap());
        this._returnInfo = returnInfo;
    }
    return this._returnInfo;
};
//MemberInfo.prototype.getReturnInfo = function(){
//    if(!this._returnInfo){
//        var returnInfo = this.docEntry.getReturn();
//        returnInfo = new TypeInfo(returnInfo,this.fileInfo.getAvailableObjectFileInfoMap());
//        returnInfo.type = this.docEntry.getReturnType() || returnInfo.type||"void";
//        this._returnInfo = returnInfo;
//    }
//    return this._returnInfo;
//};

FunctionInfo.prototype.type = 'function';
/**
 * @public
 * @param <FileInfo> fileInfo
 * @param <String> name
 * @param <Object> object
 * @param <DocEntry> docEntry
 */
function ConstructorInfo(fileInfo,name,object,docEntry){
    this.fileInfo = fileInfo;
    this.packageInfo = fileInfo.packageInfo;
    this.name = name;
    this.object = object;
    this.docEntry = docEntry;
}

ConstructorInfo.prototype = createPrototypeStack(FunctionInfo,ConstructorInfo);


ConstructorInfo.prototype.type = 'constructor';


/**
 * 获取父类信息
 * @public
 */
ConstructorInfo.prototype.getSuperInfo = function(){
    if(!('_superInfo' in this)){
        var superName = this.docEntry.getExtend();
        if(superName){
            this._superInfo =  this.fileInfo.getAvailableObjectInfo(superName);
        }
        if(!this._superInfo && this.object instanceof Function){
            var thisPrototype = this.object.prototype;
            var sub = false;
            for(var n in thisPrototype){
                if(n == 'constructor'){
                    sub = true;//(p[n] instanceof Function);
                }
            }
            //alert("#####"+sub + (thisPrototype instanceof Object && thisPrototype.constructor != this.object))
            if(sub || (thisPrototype instanceof Object && thisPrototype.constructor != this.object)){
                //var omap =  this.fileInfo.getAvailableObjectMap();
                this._superInfo = findSupperInfo(this, this.object.prototype);
            }else{
                this._superInfo = null;
            }
        }else{
            //this._superInfo = null;
        }
    }
    return this._superInfo;
}
/**
 * 获取当前类申明的实例信息
 * @public
 */
ConstructorInfo.prototype.getDeclaredInstanceInfos = function(){
    if(!this._declaredInstanceInfos){
        var instanceInfos = this.getInstanceInfos();
        var declaredInstanceInfos = [];
        for(var i=0;i<instanceInfos.length;i++){
            var n = instanceInfos[i];
            var o = instanceInfos[n];
            if(o.getPlace() == this){
                declaredInstanceInfos.push(n);
                declaredInstanceInfos[n] = o;
            }
        }
        this._declaredInstanceInfos = declaredInstanceInfos;
    }
    return this._declaredInstanceInfos;
}
/**
 * 获取当前类继承树
 * 自上而下列出[顶级就是Object]
 * <code>[this,super,super.super,....].reverse();</code>
 * @public
 */
ConstructorInfo.prototype.getInheritList = function(){
    if(!this._inheritList){
        var cs = [];
        var c = this;
        //try{
        do{
            cs.push(c);
        } while(c = c.getSuperInfo())
        //}catch(e){alert($log.error(e))}
        this._inheritList = cs.reverse();
    }
    return this._inheritList;
}
/**
 * 获取当前类静态成员信息
 * @public
 */
ConstructorInfo.prototype.getStaticInfos = function(){
    if(!this._staticInfos){
        var staticInfos = MemberInfo.createMembers(this,true);
        for(var i=0;i<staticInfos.length;i++){
            var n = staticInfos[i];
            if(n == 'prototype'){
                staticInfos.splice(i,1);
                delete staticInfos[n];
                i--;
            }
        }
        this._staticInfos = staticInfos;
    }
    return this._staticInfos;
}


ConstructorInfo.prototype.getInstanceInfo = function(name){
    var rtv = this.getInstanceInfos()[name];
    return rtv==Array.prototype[name]?null:rtv;
}
/**
 * 获取当前类实例成员信息
 * @public
 */
ConstructorInfo.prototype.getInstanceInfos = function(){
    if(!this._instanceInfos){
        this._instanceInfos = MemberInfo.createMembers(this,false);
    }
    return this._instanceInfos;
}
