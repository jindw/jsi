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
    this.index = -1;
}


LoadSuite.prototype = {
    typeText:{
        S:'同步装载',
        A:'异步装载',
        L:'延迟装载'
    },
    testNext : function(previousPath,type,previousTarget,exception){
        if(previousPath){
            this.updateLoading.apply(this,arguments)
        }
        //SALS  S
        if(type == 'L'){
            var path = this.paths[++this.index];
            type = 'S';
        }else{//SA
            type = type == 'A'?'L':'A';
            var path = this.paths[this.index];
        }
        if(path){
            this.showLoading(path,type)
            var src = scriptBase+"loader.html?path="+encodeURIComponent(path) + '&type='+type;
            window.open(src,"loader");
        }else{
            prompt("测试完成",suite);
        }
    },
    showLoading:function(path,type){
        var div = document.createElement('div');
        div.className = "loading";
        div.innerHTML = this.typeText[type] + ":"+path +'....';
        this.getOutputContainer().appendChild(div);
    },
    isValid:function(target,path){
        path = path.split(':')[1];
        if(path){
            path = path.split('.');
            path.reserve();
            while(path.length){
                target = target[path.pop()]
            }
        }
        return false;
        
    },
    updateLoading:function(previousPath,type,previousTarget,exception){
        var div = this.getOutputContainer().lastChild;
        var msg = [this.typeText[type] ,":",path];
        if(exception){
            div.className="error";
            msg.push("失败！！！<br/>Exception:<br/><pre>");
            for(var n in e){
                msg.push("  ",n,"=",e[n]);
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
    getOutputContainer:function(){
        return document.getElementById("console");
    }
}