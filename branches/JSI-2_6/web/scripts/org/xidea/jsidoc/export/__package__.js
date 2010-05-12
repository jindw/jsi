/*
 * JavaScript Integration Doc Tool
 * //为了方便导出bug检查,改包中不能包含外部引用
 */

this.addScript('fn.js','findPackages');

this.addScript('dependence-info.js','DependenceInfo');

this.addScript('export.js','Exporter'
                ,"..util:$log"
                ,[
                'DependenceInfo',
                '..util:findGlobals',
                'findPackages',
                '..util:JSON',
                '..util:xmlReplacer',
                '..util:loadTextByURL']);

this.addScript('export-ui.js','ExportUI'
                ,"..util:$log"
                ,['Exporter','DependenceInfo',
                'findPackages',
                '..util:Zip',
                '..util:xmlReplacer',
                '..util:Request']);
