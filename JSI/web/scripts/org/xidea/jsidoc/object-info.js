/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: object-info.js,v 1.7 2008/03/01 07:18:13 jindw Exp $
 */



/**
 * 对象信息类
 * @public
 * @param <FileInfo> fileInfo
 * @param <String> name
 * @param <Object> object
 * @param <DocEntry> docEntry
 * @public
 */
function ObjectInfo(fileInfo,name,object,docEntry){
    this.name = name;
    this.object = object;
    this.fileInfo = fileInfo;
    this.packageInfo = fileInfo.packageInfo;
    this.docEntry = docEntry;
    this.type = guessType(object,docEntry);
};
/**
 * 创建对象信息
 * @param fileInfo <FileInfo> 对象所在的文件信息
 * @param name <String> 对象名
 * @param object 对象值
 * @param [docEntry] 对象doclet信息
 */
ObjectInfo.create = function(fileInfo,name,object,docEntry){
    object = object || fileInfo.getObject(name);
    docEntry = docEntry || fileInfo.getDocEntry(name);
    if(object == null){
        if(docEntry){
            var c = docEntry.getInstanceof();
            var t = docEntry.getTypeof();
            if(t == 'function' || (t == null && c == 'Function') || docEntry.getReturn() || docEntry.getParams()){
                if(docEntry.isConstructor()){
                    return new ConstructorInfo(fileInfo,name,object,docEntry);
                }else{
                    return new FunctionInfo(fileInfo,name,object,docEntry);
                }
            }
            if(c || t == 'object'){
                return new ObjectInfo(fileInfo,name,object,docEntry);
            }else if(t){
                return new PrimitiveInfo(fileInfo,name,object,docEntry);
            }else{
                return new UnknowInfo(fileInfo,name,object,docEntry);
            }
        }else{
            return new UnknowInfo(fileInfo,name,object,DocEntry.EMPTY);
        }
    }else if(object instanceof Function || object ==Function.prototype){
        for(var x in object.prototype){
            return  new ConstructorInfo(fileInfo,name,object,docEntry||DocEntry.EMPTY);
        }
        if(docEntry && docEntry.isConstructor()){
            return  new ConstructorInfo(fileInfo,name,object,docEntry);
        }
        return new FunctionInfo(fileInfo,name,object,docEntry||DocEntry.EMPTY);
    }else if(object instanceof Object){
        //some IE native function is object
        if(docEntry && docEntry.isConstructor()){
            return  new ConstructorInfo(fileInfo,name,object,docEntry||DocEntry.EMPTY);
        }
        return new ObjectInfo(fileInfo,name,object,docEntry||DocEntry.EMPTY);
    }else{
        return new PrimitiveInfo(fileInfo,name,object,docEntry||DocEntry.EMPTY);
    }
  
}
ObjectInfo.prototype.docEntry = DocEntry.EMPTY;
ObjectInfo.prototype.getDescription = function(){
    return this.docEntry.getDescription();
}

ObjectInfo.prototype.getPath = function(){
     return this.packageInfo.name+":"+this.name;
}
ObjectInfo.prototype.getLink = function(){
    return this.docEntry.tagAttributes['link'];
}
ObjectInfo.prototype.getSee = function(){
    return this.docEntry.tagAttributes['see'];
}


ObjectInfo.prototype.getShortDescription = function(){
    if(!('_shortDescription' in this)){
        var sd = this.getDescription();
        if(sd){
            this._shortDescription = sd.replace(/^([^\.\n\r\u3002]*)[\s\S]*$/,'$1.')
        }else{
            this._shortDescription = '';
        }
    }
    return this._shortDescription;
}



/**
 * 对象构造器信息(instanceof)
 */
ObjectInfo.prototype.getConstructorInfo = function(){
    if(!('_constructorInfo' in this)){
        var c = this.docEntry.getInstanceof();
        if(c){
            this._constructorInfo = this.fileInfo.getAvailableObjectInfo(c.replace(/^\s*|\s*$/g,''));
        }
        if(!this._constructorInfo){
            if(this.object == null || this.object == window || this.object.constructor == Object){
                this._constructorInfo = null;
            }else{
      	      	//TODO:
                this._constructorInfo = findSupperInfo(this,this.object);
            }
        }
    }
    return this._constructorInfo;
};


ObjectInfo.prototype.getDeclaredStaticInfos = function(){
    if(!this._declaredStaticInfos){
        var staticInfos = this.getStaticInfos();
        var declaredStaticInfos = [];
        for(var i=0;i<staticInfos.length;i++){
            var n = staticInfos[i];
            var o = staticInfos[n];
            if(o.getPlace() == this){
                declaredStaticInfos.push(n);
                declaredStaticInfos[n] = o;
            }
        }
        this._declaredStaticInfos = declaredStaticInfos;
    }
    return this._declaredStaticInfos;
}


ObjectInfo.prototype.getStaticInfo = function(name){
    return this.getStaticInfos()[name]
}
ObjectInfo.prototype.getStaticInfos = function(){
    if(!this._staticInfos){
        this._staticInfos = MemberInfo.createMembers(this,true);
    }
    return this._staticInfos;
}
ObjectInfo.prototype.toString = function(){
    return this.type+this.name;
};
ObjectInfo.prototype.getAccess = function(){
    if(this.docEntry){
        return this.docEntry.getAccess() || '';
    }
    return '';
};
/**
 */
function UnknowInfo(fileInfo,name,object,docEntry){
    this.fileInfo = fileInfo;
    this.packageInfo = fileInfo.packageInfo;
    this.name = name;
    this.object = object;
    this.docEntry = docEntry||DocEntry.EMPTY;
    this.type = guessType(object,docEntry);
}
UnknowInfo.prototype = createPrototypeStack(ObjectInfo,UnknowInfo);;
UnknowInfo.prototype.getConstructorInfo = function(){return null;};
/**
 */
function PrimitiveInfo(fileInfo,name,object,docEntry){
    this.fileInfo = fileInfo;
    this.packageInfo = fileInfo.packageInfo;
    this.name = name;
    this.object = object;
    this.docEntry = docEntry||DocEntry.EMPTY;
    this.type = guessType(object,docEntry);
}
PrimitiveInfo.prototype = createPrototypeStack(ObjectInfo,PrimitiveInfo);;
PrimitiveInfo.prototype.getConstructorInfo = function(){return null;};


function guessType(object,docEntry){
    if(object != null){
        if(object instanceof RegExp){//firefox bug?
            return "object";
        }else{
            return (typeof object);
        }
    }else if(object === null){
        return docEntry.getTypeof()||"null"
    }else{
        //alert(this.docEntry);
        return docEntry.getTypeof()||'undefined'
    }
}

