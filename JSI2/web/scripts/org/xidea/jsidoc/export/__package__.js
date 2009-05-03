/*
 * JavaScript Integration Doc Tool
 * //为了方便导出bug检查,改包中不能包含外部引用
 */

this.addScript('dependence-info.js','DependenceInfo');

this.addScript('export.js','Exporter'
                ,0
                ,["org.xidea.jsidoc:getTemplate",'DependenceInfo','org.xidea.jsidoc.util:findGlobals','org.xidea.jsidoc:findPackages','org.xidea.jsidoc.util:JSON','org.xidea.jsidoc.util:xmlReplacer','org.xidea.jsidoc.util:loadTextByURL']);

this.addScript('export-ui.js','ExportUI'
                ,0
                ,['Exporter','DependenceInfo','org.xidea.jsidoc:findPackages','org.xidea.jsidoc.util:xmlReplacer','org.xidea.jsidoc.util:Request']);

