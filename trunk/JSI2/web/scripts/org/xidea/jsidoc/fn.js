/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.4 2008/02/24 08:58:15 jindw Exp $
 */

function createPrototypeStack(superclass,constructor) {
    function f(){};
    f.prototype = superclass.prototype;
    var npt = new f();
    npt.constructor = constructor;
    return npt;
}


function findSupperInfo(baseInfo,object){
    var superList = [];
    var availableObjectMap = baseInfo.fileInfo.getAvailableObjectMap();
    for(var n in availableObjectMap){
        var superObject = availableObjectMap[n];
        if(superObject instanceof Function && object instanceof superObject){
            var k = 0;
            for(var n2 in superObject.prototype){
                k++;
            }
            if(superList.length){
                if(k>superList[0]){
                        superList = [k,n]
                }else if(k==superList[0]){
                        superList.push(n)
                }
            }else{
                superList = [k,n];
            }
        }
    }
    //alert(superList)
    if(superList.length){
        if(superList.length == 2){
                //alert(superList[1])
                return baseInfo.fileInfo.getAvailableObjectInfo(superList[1]);
        }else{
                superList[0] = superList.pop();
                while((n = superList.pop())&&(n2 = superList.pop())){
                        var obj1 = availableObjectMap[n];
                        var obj2 = availableObjectMap[n2];
                        //保留子类
                        if(obj1.prototype instanceof obj2){
                                superList.push(obj1);
                        }else{
                                superList.push(obj2);
                        }
                }
                //alert(n)
                return baseInfo.fileInfo.getAvailableObjectInfo(n);
        }
    }
}

/**
 * @internal
 */
var accessOrder = "private,internal,protected,friend,public";

function scrollOut(ele){
    if(ele.scrollIntoView){
        ele.scrollIntoView(false);
    }
}

/*
 * Dependence = [0            , 1             , 2               , 3            ,4         ,5    ]
 * Dependence = [targetPackage, targetFileName, ,thisObjectName, afterLoad,names]
 * afterLoad,thisObject 有点冗余
 */
//var DEPENDENCE_TARGET_PACKAGE = 0;
//var DEPENDENCE_TARGET_FILE_NAME = 1;
//var DEPENDENCE_TARGET_OBJECT_NAME = 2;
//var DEPENDENCE_THIS_OBJECT_NAME = 3;
//var DEPENDENCE_AFTER_LOAD = 4;
//var DEPENDENCE_NAMES = 4;
