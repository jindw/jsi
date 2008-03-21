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
var FILE_LIST_OUTPUT_ID = "fileListOutput";
var OBJECT_LIST_OUTPUT_ID = "objectListOutput";
var EXPORT_BUTTON = "exportButton";

var inc = 0;
//var PACKAGE_TEMPLATE = "<li></li>"
var ExportUI = {
    initialize:function(sourcePackage,treeTemplateText){
        var nameList = findPackages(sourcePackage,true);
        var treeTemplate = new Template(treeTemplateText);
        for(var i=0; i<nameList.length; i++) {
            var name = nameList[i];
        	var packageObject = $import(name+':');
        	if(packageObject.name == nameList[i]){
                packageNodes.push(name);
                packageNodes[name] = new PackageNode(packageObject);
        	}
        }
        document.getElementById(TREE_CONTAINER_ID).innerHTML = treeTemplate.render({packageNodes:packageNodes});
    },
    clickScript : function(objectId){
        var checked = checkMap[objectId];
        if(checked){
            delete checkMap[objectId];
        }else{
            checkMap[objectId] = true;
        }
        update();
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
        update();
    },
    doExport : function(form){
        var level = form.level;
        var i=level.length;
        while(i--){
            var input = level[i];
            if(input.checked){
                doExportByLevel(input.value);
                return;
            }
        }
        $log.error("不支持导出级别:",level)
    }
}
function doExportByLevel(level){
    var exporter = new Exporter();
    for(var path in checkMap){
        exporter.addImport(path);
    }
    if(level == 0){
        var content = exporter.getContent();
    }else if(level == 1){
        var content = exporter.getFileMap();
    }
    var dialog = window.open('about:blank','source','modal=yes,left=200,top=100,width=600px,height=600px');
    var document = dialog.document;
    document.open();
    document.write("<html><style>*{width:100%;height:100%;padding:0px;margin:0px;}</style><body><textarea>");
    document.write(content.replace(/[<>&]/g,xmlReplacer));
    document.write("</textarea></body></html>");
    document.close();
}
function update(){
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
    var button = document.getElementById(EXPORT_BUTTON);
    button.disabled = true;
    for(var n in checkMap){
        button.disabled = false;
        break;
    }
}
function updateNode(node,state){
    document.getElementById(node.htmlId).className = "checkbox"+state;
}
function updateOutput(){
    var fileListOutput = document.getElementById(FILE_LIST_OUTPUT_ID);
    var objectListOutput = document.getElementById(OBJECT_LIST_OUTPUT_ID);
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
