/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: menu.js,v 1.4 2008/02/24 08:58:15 jindw Exp $
 */

var MenuUI = {
    loadPackage:function(document,name){
        var ul = document.getElementById('package_'+name);
        if(ul.getAttribute('title') == "loading"){
            ul.setAttribute('title','loaded');
            var nextSibling = ul.firstChild;
            var packageInfo = parent.JSIDoc.packageInfos[name];
            var tasks = packageInfo.getInitializers();
            var i = 0;
            tasks.push(function(){
                var os = packageInfo.getObjectInfos();
                for(var i=0;i<os.length;i++){
                    tasks.push(buildAppender(document,nextSibling,os[i]));
                }
                tasks.push(function(){
                    while(nextSibling.nextSibling){
                        ul.removeChild(nextSibling.nextSibling)
                    }
                    ul.removeChild(nextSibling)
                    //ul.innerHTML = ""+ul.innerHTML;
                });
            });
            function run(){
                if(i<tasks.length){
                    tasks[i++]();
                }else{
                    clearTimeout(task);
                }
            }
            var task = setInterval(run,20);
        }
    }
}
function buildAppender(document,nextSibling,objectInfo){
    return function(){
        var li = document.createElement("li");
        var a = document.createElement("a");
        nextSibling.parentNode.insertBefore(li,nextSibling);
        
        li.setAttribute('title',"file:"+objectInfo.fileInfo.name);
        a.className = "item-"+objectInfo.type;
        a.setAttribute('class',a.className);
        a.setAttribute('href',"?"+objectInfo.getPath());
        a.appendChild(document.createTextNode(objectInfo.name));
        li.appendChild(a);
        //scrollOut(nextSibling)
    }
}
