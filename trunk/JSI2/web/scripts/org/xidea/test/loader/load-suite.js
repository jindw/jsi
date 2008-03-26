/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: loader-test.js,v 1.3 2008/02/19 13:39:03 jindw Exp $
 */

var scriptBase = this.scriptBase;
var inc = 0;
function LoadSuite(packageList,collectAll){
    LoadSuite[this.id = "$"+(inc++)] = this;
    packageList=collectAll?findPackages(packageList):packageList;
    packageList.sort();
    var paths = [];
    for(var i = 0;i<packageList.length;i++){
        var packageName = packageList[i];
        var packageObject = $import(packageName + ':');
        if(packageObject.name == packageName){
            for(var fileName in packageObject.scriptObjectMap){
                paths.push(packageName.replace(/\.|$/g,'/')+fileName);
            }
            for(var objectName in packageObject.objectScriptMap){
                paths.push(packageName+":"+objectName);
            }
        }else{
            //ref
        }
    }
    this.paths = paths;
    this.index = 0;
}


LoadSuite.prototype = {
    typeList : "SAL",
    /**
     * @public
     */
    typeText:{
        S:'同步装载',
        A:'异步装载',
        L:'延迟装载'
    },
    /**
     * @public
     */
    startTest:function(type){
        var path = this.paths[this.index];
        type = type || this.typeList.charAt(0);
        this.showLoading(path,type)
        var src = scriptBase+"loader.html?path="+encodeURIComponent(path) + '&type='+type+'&id='+this.id;
        window.open(src,"loader");
    },
    /**
     * @public
     */
    nextTest : function(previousPath,type,previousTarget,exception){
        this.updateLoading.apply(this,arguments);
        var typeList = this.typeList;
        //SALS  S
        var index = typeList.indexOf(type);
        if(index == typeList.length-1){
            this.index++;
            type = typeList.charAt(0);
        }else{//SA
            type = typeList.charAt(index+1);
        }
        if(this.index<this.paths.length){
            this.startTest(type);
        }else{
            prompt("测试完成",suite);
        }
    },
    /**
     * @protected
     */
    showLoading:function(path,type){
        var div = document.createElement('div');
        div.className = "loading";
        div.innerHTML = this.typeText[type] + ":"+path +'....';
        this.getOutputContainer().appendChild(div);
    },
    /**
     * @protected
     */
    updateLoading:function(previousPath,previousType,previousTarget,exception){
        var div = this.getOutputContainer().lastChild;
        var msg = [this.typeText[previousType] ,":",previousPath];
        if(exception){
            div.className="error";
            msg.push("失败！！！<br/>Exception:<br/><pre>");
            for(var n in exception){
                msg.push("  ",n,"=",exception[n]);
                msg.push("\n");
            }
            msg.pop();
            msg.push("</pre>");
        }else{
            if(this.isValid(previousTarget,previousPath)){
                div.className="success";
                msg.push("成功。")
            }else{
                div.className="wanring";
                msg.push("完成，对象为空！！")
            }
            
            
        }
        msg.push('<hr/>');
        div.innerHTML = msg.join('');
    },
    /**
     * @protected
     */
    isValid:function(target,path){
        path = path.split(':')[1];
        if(path){
            path = path.split('.');
            path.reverse();
            var object = target
            while(path.length){
                object = object[path.pop()]
            }
            return object!=target['+undefined'];
        }
        return true;
        
    },
    getOutputContainer:function(){
        return document.getElementById("console");
    }
}