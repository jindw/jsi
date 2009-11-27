
/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: member-info.js,v 1.4 2008/02/24 08:58:15 jindw Exp $
 */

/**
 * 成员信息类
 * 成员信息动态继承(拷贝属性)成员的类型类（ObjectInfo、ConstructorInfo、FunctionInfo...）
 * @public
 */
function MemberInfo(ownerInfo,ownerInherits,memberName,docEntry,isStatic){
    var name = ownerInfo.name+(isStatic?'#':'#prototype.')+memberName;
    this.id = ownerInfo.packageInfo.name +':' +name;
    name = name.replace('#','.');
    var clazz = ownerInfo.object;
    if(clazz){
        if(isStatic){
                var object = clazz[memberName]
        }else if(clazz.prototype){
                var object = clazz.prototype[memberName]
        }
    }
    if(!(docEntry instanceof DocEntry)){
        docEntry = DocEntry.EMPTY;
    }
    var objectInfo = ObjectInfo.create(ownerInfo.fileInfo,name,object,docEntry);
    for(var n in objectInfo){
            this[n] = objectInfo[n];
    }
    this.docEntry = docEntry;
    //this.name = name;
    this.ownerInfo = ownerInfo;
    this.memberName = memberName;
    this.ownerInherits = ownerInherits;
    this.isStatic = this['static'] = isStatic;
}

MemberInfo.prototype = createPrototypeStack(ObjectInfo,MemberInfo);

/**
 * @internal
 * @param objectInfo 对象信息，其对象值不能为空。
 * @param isStatic 是否取静态成员。
 */
MemberInfo.createMembers = function(ownerInfo,isStatic){
    var infos = [];
    if(!ownerInfo.object){
        return infos;
    }
    //var fileObjectInfoMap = objectInfo.fileInfo.getObjectInfoMap();
    var memberHolder = ownerInfo.object ||{}
    var fileInfo = ownerInfo.fileInfo;
    var sourceEntry = fileInfo.sourceEntryList||fileInfo.getSourceEntry();
    var memberDocMap = {};
    var patternMap = {};
    //
    if(isStatic){
        appendToMap(sourceEntry, ownerInfo.name + '.',memberDocMap,patternMap);
    }else{
        var ownerInherits = ownerInfo.getInheritList();
        memberHolder = memberHolder.prototype ||{}
        appendToMap(sourceEntry, ownerInfo.name + '.this.',memberDocMap,patternMap);
        appendToMap(sourceEntry, ownerInfo.name + '.prototype.',memberDocMap,patternMap);
    }
    
    //反射取成员
    for(var memberName in memberHolder){
        var memberDocEntry = memberDocMap[memberName]||findPatternMemberDocEntry(patternMap,memberName);
        var info = new MemberInfo(ownerInfo,ownerInherits,memberName,memberDocEntry,isStatic);
        if(isStatic){
            if(memberName == 'constructor'){
                continue;
            }
        }else{
            info.memberType = 'prototype';
        }
        try{
            if(!/^\d+$/.test(memberName)){
                infos[memberName] = info;
                infos.push(memberName);
            }
        }catch(e){}
    }
    //文档取成员
    for(var memberName in memberDocMap){
        if(!(memberName in memberHolder)){
            var info = new MemberInfo(ownerInfo,ownerInherits,memberName,memberDocMap[memberName],isStatic);
            if(!isStatic){
                info.memberType = 'instance';
            }
            try{
                if(!/^\d+$/.test(memberName)){
                    infos[memberName] = info;
                    infos.push(memberName);
                }
                
            }catch(e){}
        }
    }
    //$log.debug(info.getAccess)
    //infos.sort();
    infos.sort(function(a,b){
        //$log.debug(a + b + infos[a].memberInfo.docEntry + infos[b].memberInfo.docEntry);
        try{
            var av = infos[a].getAccess();
            var bv = infos[b].getAccess();
        }catch(e){
            try{
                $log.debug("getAccess",isStatic,"a:"+a,"b:"+b,'infos:'+infos);
                $log.debug(e.message,infos[a].docEntry,infos[b].docEntry.constructor,infos[a].getAccess ,infos[b].getAccess)
            }catch(e){
                $log.debug(e);
            }
        }
        return (accessOrder.indexOf(bv)-accessOrder.indexOf(av))||(a>b?1:-1);
    });
    //$log.info(infos.join('\n'));
    return infos;
};
/**
 * 获取成员的申明位置
 */
MemberInfo.prototype.getPlace = function(){
    if(!this.place){
        var length = this.ownerInherits && this.ownerInherits.length
        for(var i=0 ; i<length; i++){
            var node = this.ownerInherits[i];
            var name = this.memberName;
            if((name in node.object.prototype) || node.getInstanceInfo (name)){
                this.place = node;
                break;
            }
        }
        if(!this.place){
            this.place = this.ownerInfo;
        }
    }
    return this.place;
}

function findPatternMemberDocEntry(patternMap,memberName){
    for(name in patternMap){
        var entry = patternMap[name];
        var pattern = entry.pattern;
        var matchs = pattern.exec(memberName);
        if(matchs){
            return new PatternDocEntry(entry,memberName,matchs);
        }
    }
}
function PatternDocEntry(entry,memberName,matchs){
    for(var n in entry){
        this[n] = entry[n];
    }
    this.name = memberName;
    this.id = this.id.replace(/[^\.]+$/,memberName);
    var description = this.description||'';
    description =  description.replace(/\$(\d)/,function(t,d){
        return matchs[d];
    })
    this.description = description;
}
PatternDocEntry.prototype = DocEntry.prototype;
//ownerInfo.docEntry.getInstanceMemberMap(this,ownerInfo);
function appendToMap(sourceEntry,prefix,strictMap,patternMap){
    if(sourceEntry instanceof Array){
        var i = sourceEntry.length; 
        while(i--){
            appendToMap(sourceEntry[i],prefix,strictMap,patternMap);
        }
    }else{
        var ids = sourceEntry.docIds;
        for(var i=0;i<ids.length;i++){
            var id = ids[i];
            if(id.indexOf(prefix)==0){
                var name = id.substr(prefix.length);
                if(name.indexOf('.')<0){
                    if(name.indexOf('*')>=0){
                        (patternMap[name] = sourceEntry.getDocEntry(id)).pattern = new RegExp(name.replace(/\*/g,"(.*)"));;
                    }else{
                        strictMap[name] = sourceEntry.getDocEntry(id);
                    }
                }
            }
        }
    }
}
//function getStaticMemberMap(ownerInfo){
//    var docEntry = ownerInfo.docEntry;
//    if(!docEntry.staticMenberMap){
//        var id = docEntry.getId();
//        var ids = docEntry.sourceEntry.docIds;
//        docEntry.staticMenberMap = {};
//        var mp = new RegExp("^"+id.replace(/([\$\.])/g,'\\$1')+"\\.([^\\.]+)$")
//        for(var i=0;i<ids.length;i++){
//            if(mp.test(ids[i])){
//                //alert(ids[i])
//                docEntry.staticMenberMap[ids[i].replace(mp,"$1")] = docEntry.sourceEntry.getDocEntry(ids[i]);
//            }
//        }
//    }
//    return docEntry.staticMenberMap
//}
//function getInstanceMemberMap(ownerInfo){
//    var docEntry = ownerInfo.docEntry;
//    if(!docEntry.instanceMemberMap){
//        var id = docEntry.getId();
//        var ids = docEntry.sourceEntry.docIds;
//        docEntry.instanceMemberMap = {};
//        var mp = new RegExp("^"+id.replace(/([\$\.])/g,'\\$1')+"\\.(?:prototype|docEntry)\\.([^\\.]+)$")
//        //alert(mp.source)
//        for(var i=0;i<ids.length;i++){
//            if(mp.test(ids[i])){
//                //alert(ids[i])
//                docEntry.instanceMemberMap[ids[i].replace(mp,"$1")] =  docEntry.sourceEntry.getDocEntry(ids[i]);
//            }
//        }
//    }
//    return docEntry.instanceMemberMap
//}

