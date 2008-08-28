/*
 * JavaScript Integration Doc Tool
 * //为了方便导出bug检查,改包中不能包含外部引用
 */

this.addScript("dependence-info.js",'DependenceInfo');

this.addScript("export.js",['Exporter']
              ,['DependenceInfo','org.xidea.jsidoc.util:JSON']
              ,["fn.js",'findGlobals']);
this.addScript("export-ui.js",['ExportUI']
              ,['Exporter','DependenceInfo','org.xidea.jsidoc.util:Template',"org.xidea.jsidoc.util:Request"]
              ,["fn.js",'findGlobals']);

this.addScript("fn.js",['findPackages','xmlReplacer','loadTextByURL']);
//findGlobalsAsList 是为java提供的接口的。
this.addScript("find-globals.js",['findGlobals','findGlobalsAsList']);
