/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: export-ui.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */

var packageNodes = [];
var nodeMap = {};
var checkMap = {};
var TREE_CONTAINER_ID = "treeContainer";
var FILE_OUTPUT_ID = "fileOutput";
var OBJECT_OUTPUT_ID = "objectOutput";
var EXPORT_BUTTON = "exportButton";
var PREFIX_CONTAINER_ID = "prefixContainer";
var JSIDOC_URL_CONTAINER_ID = "jsidocURLContainer";
var compressServiceURL = $JSI.scriptBase + "export.action";

var inc = 0;
var exportDocument;
//var PACKAGE_TEMPLATE = "<li></li>"
var ExportUI = {
    prepare:function(doc,sourcePackage){
    	exportDocument = doc;
        var nameList = findPackages(sourcePackage,true);
        for(var i=0; i<nameList.length; i++) {
            var name = nameList[i];
        	var packageObject = $import(name+':');
        	if(packageObject.name == nameList[i]){
                packageNodes.push(name);
                packageNodes[name] = new PackageNode(packageObject);
        	}
        }
        return packageNodes;
    },
    initialize:function(){
        initializeForm();
        var request1 = new Request(compressServiceURL,"post",function(success){
            if(success){
                updateDisabledForm('1');
            }
        });
        request1.send("level=2");
        var request2 = new Request(compressServiceURL,"post",function(success){
            if(success){
                updateDisabledForm('2');
            }
        });
        request2.send("level=2");
    },
    clickScript : function(objectId){
        var checked = checkMap[objectId];
        if(checked){
            delete checkMap[objectId];
        }else{
            checkMap[objectId] = true;
        }
        updateTree();
    },
    clickPackage : function(packageId){
        var packageNode = nodeMap[packageId];
        var selectedCount = getPackageSelectedCount(packageNode)
        var childNodes = packageNode.children;
        var i = childNodes.length;
        if(selectedCount < i){
            while(i--){
                checkMap[childNodes[childNodes[i]].id] = true;
            }
        }else{
            while(i--){
                delete checkMap[childNodes[childNodes[i]].id];
            }
        }
        updateTree();
    },
    checkLevel:function(levelInput){
        var level = levelInput.value;
        var prefix = exportDocument.getElementById(PREFIX_CONTAINER_ID);
        var jsidoc = exportDocument.getElementById(JSIDOC_URL_CONTAINER_ID);
        prefix.style.display = (level == 1 || level ==2) ? 'block':'none';
        jsidoc.style.display = (level == -2) ? 'block':'none';
        var lis = levelInput.form.getElementsByTagName("li");
        for(var i=0;i<lis.length;i++){
            lis[i].style.display = (i-3 == level)?'block':'none';
        }
        
    },
    doExport : function(form){
        var level = form.level;
        var i=level.length;
        while(i--){
            var input = level[i];
            if(input.checked){
                level = input.value;
                break;
            }
        }
        var exporter = new Exporter();
        for(var path in checkMap){
            exporter.addImport(path);
        }
        switch(level*1){
        case -2:
            showResult(exporter.getDocumentContent(form.jsidocURL.value));
            break;
        case -1:
            showResult(exporter.getXMLContent());
            break;
        case 0:
            showResult(exporter.getTextContent());
            break;
        
        case -3:
            //导出分析报告
        case 1:
            //导出时解决内部变量之间的冲突
        case 2:
            //导出时尽可能解决全部冲突
            //submit to JSA
            var xmlContent = exporter.getXMLContent();
            showResult("数据装在中.....");
            var request = new Request(compressServiceURL,"post",function(){
                showResult(this.getText(),true)
            });
            var prefix = form.prefix.value;//PARAM_PREFIX
            request.send("level="+level+"&prefix="+prefix+"&content="+encodeURIComponent(xmlContent));
            break;
        default:
            $log.error("不支持导出级别["+level+"],将导出xml格式打包文件");
            showResult(exporter.getXMLContent());
            break;
        }
    }
}
var dialog;
function showResult(content,reuse){
    if(!reuse && dialog){
        try{
            dialog.close();
        }catch(e){}
        dialog = null;
    }
    dialog = dialog || window.open('about:blank','source','modal=yes,left=200,top=100,width=600px,height=600px');
    var doc = dialog.document;
    doc.open();
    doc.write("<html><title>==请将文本筐中的内容拷贝到目标文件(js,xml,html,hta)==</title><style>*{width:100%;height:100%;padding:0px;margin:0px;}</style><body><textarea readonly='true' wrap='off'>");
    doc.write(content.replace(/[<>&]/g,xmlReplacer));
    doc.write("</textarea></body></html>");
    doc.close();
}
function initializeForm(){
    var levels = exportDocument.forms[0].level;
    for(var i=0; i<levels.length; i++) {
        var input = levels[i];
        if(input.value ==1 ||input.value ==2){
            input.disabled = true;
        }else if(!input.disabled && input.checked ){
            input.click();
        }
    }
}
function updateDisabledForm(value){
    var levels = exportDocument.forms[0].level;
    for(var i=0; i<levels.length; i++) {
        var input = levels[i];
        if(input.disabled && input.value == value){
            input.disabled = false;
            if(input.getAttribute('checked')){
                input.checked = true;
                input.click(); 
            }
        }
    }

}
function updateTree(){
    var i = packageNodes.length;
    var resultMap = updateOutput();
    while(i--){
        var packageNode = packageNodes[packageNodes[i]];
        var childNodes = packageNode.children;
        var selectCount = 0;
        var j = childNodes.length;
        var packageState13 = 2;
        while(j--){
            var child = childNodes[childNodes[j]];
            var checked = checkMap[child.id];
            if(checked){
                var state = 4;//装载并导出
            }else{
                var state = 0;
                //1:文件已经装载,但缺乏相关依赖
                //2:文件及依赖文件已经装载
                //3:文件及依赖文件已经装载并且注入了相关依赖 (暂时不考虑2,3区别)
                if(resultMap[child.filePath]){
                    var dependenceInfo = new DependenceInfo(child.id);
                    //var allLoaded = true;
                    var state = 2;
                    var afterInfos = dependenceInfo.getAfterInfos();
                    var k = afterInfos.length;
                    while(k--){
                        var subDependenceInfo = afterInfos[k];
                        if(!resultMap[subDependenceInfo.filePath]){
                            //allLoaded = false;
                            state = 1;
                            break;
                        }
                    }
                    packageState13 = Math.min(packageState13,state);
                }else{
                    packageState13 = 1;
                }
            }
            updateNode(child,state);
            selectCount +=state;
        }
        if(selectCount ==0){
            updateNode(packageNode,0);
        }else{
            updateNode(packageNode,selectCount == childNodes.length*4?4:packageState13);
        }
    }
    //update button
    var button = exportDocument.getElementById(EXPORT_BUTTON);
    button.disabled = true;
    for(var n in checkMap){
        button.disabled = false;
        break;
    }
}
function updateNode(node,state){
    exportDocument.getElementById(node.htmlId).className = "checkbox"+state;
}
function updateOutput(){
    var fileListOutput = exportDocument.getElementById(FILE_OUTPUT_ID);
    var objectListOutput = exportDocument.getElementById(OBJECT_OUTPUT_ID);
    var objectNames = [];
    var exporter = new Exporter();
    for(var path in checkMap){
        exporter.addImport(path);
        var objectName = path.split(':')[1];
        if(objectName){
            objectNames.push("<div title='",path,"'>",objectName,"</div>");
        }
    }
    var result = exporter.getResult();
    var resultMap = {};
    for(var i=0; i<result.length; i++) {
    	resultMap[result[i]] = true;
    }
    fileListOutput.innerHTML = result.join('<br />');
    objectListOutput.innerHTML = objectNames.join('');
    return resultMap;
}
function getPackageSelectedCount(packageNode){
    var childNodes = packageNode.children;
    var i = childNodes.length;
    var j = 0;
    while(i--){
        if(checkMap[childNodes[childNodes[i]].id]){
            j++;
        }
    }
    return j;
}
function buildPackageNodes(packageObject){
    var nodes = [];
    for(var fileName in packageObject.scriptObjectMap){
        var objectNames = packageObject.scriptObjectMap[fileName];
        if(objectNames == null || objectNames.length==0){
            var fileNode = new FileNode(packageObject,fileName);
            nodes.push(fileName);
            nodes[fileName] = fileNode;
        }
    }
    for(var objectName in packageObject.objectScriptMap){
        var objectNode = new ObjectNode(packageObject,objectName);
        nodes.push(objectName);
        nodes[objectName] = objectNode;
    }
    nodes.sort();
    return nodes;
}
function PackageNode(packageObject){
    this.shortName = packageObject.name;
    this.id = packageObject.name +':';
    this.children = buildPackageNodes(packageObject);
    nodeMap[this.id] = this;
    this.htmlId = "__$ID"+inc++;
}
function FileNode(packageObject,fileName){
    this.shortName = fileName;
    this.packageName = packageObject.name;
    this.filePath = this.id = packageObject.name.replace(/\.|$/g,'/')+fileName;
    nodeMap[this.id] = this;
    this.htmlId = "__$ID"+inc++;
}
function ObjectNode(packageObject,objectName){
    this.shortName = objectName;
    this.packageName = packageObject.name;
    this.filePath = packageObject.name.replace(/\.|$/g,'/')+packageObject.objectScriptMap[objectName];
    this.id = packageObject.name+':'+objectName
    nodeMap[this.id] = this;
    this.htmlId = "__$ID"+inc++;
}
