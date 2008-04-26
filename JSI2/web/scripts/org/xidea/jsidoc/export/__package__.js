/*
 * JavaScript Integration Doc Tool
 * //为了方便导出bug检查,改包中不能包含外部引用
 */

this.addScript("dependence-info.js",'DependenceInfo');

this.addScript("export.js",['Exporter']
              ,['DependenceInfo','JSON','org.xidea.jsidoc:JSIDoc']);
this.addScript("export-ui.js",['ExportUI']
              ,['DependenceInfo','org.xidea.sandbox.xml:Template',"org.xidea.sandbox.io:Request"]);

this.addScript("fn.js",['findPackages','xmlReplacer']);

this.addScript("json.js",['JSON']);
              
this.addDependence("*","*",true);
              