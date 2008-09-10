/*
 * JavaScript Integration Doc Tool
 * //为了方便导出bug检查,改包中不能包含外部引用
 */

this.addScript("dependence-info.js",'DependenceInfo');

this.addScript("export.js",['Exporter']
              ,['DependenceInfo','org.xidea.jsidoc.util:xmlReplacer','org.xidea.jsidoc.util:JSON','org.xidea.jsidoc.util:loadTextByURL','org.xidea.jsidoc:findPackages']);
this.addScript("export-ui.js",['ExportUI']
              ,['Exporter','DependenceInfo','org.xidea.jsidoc.util:Template','org.xidea.jsidoc:findPackages']);

