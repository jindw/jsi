/*
 * JavaScript Integration Doc Tool
 * //为了方便导出bug检查,改包中不能包含外部引用
 */

this.addScript('fn.js','findPackages');

this.addScript('dependence-info.js','DependenceInfo');

this.addScript('export.js','Exporter'
                ,"org.xidea.jsidoc.util:$log"
                ,[
                'DependenceInfo',
                'org.xidea.jsidoc.util:findGlobals',
                'findPackages',
                'org.xidea.jsidoc.util:JSON',
                'org.xidea.jsidoc.util:xmlReplacer',
                'org.xidea.jsidoc.util:loadTextByURL']);

this.addScript('export-ui.js','ExportUI'
                ,"org.xidea.jsidoc.util:$log"
                ,['Exporter','DependenceInfo',
                'findPackages',
                'org.xidea.jsidoc.util:Zip',
                'org.xidea.jsidoc.util:xmlReplacer',
                'org.xidea.jsidoc.util:Request']);

