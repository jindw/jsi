/*
 * JavaScript Integration Doc Tool
 */
this.addScript("jsidoc.js",['JSIDoc']
                ,["org.xidea.sandbox.xml:Template","org.xidea.sandbox.io:Request","PackageInfo","SourceEntry"]
                ,"org.xidea.jsidoc.export:findPackages");

this.addScript('package-info.js','PackageInfo'
                ,'FileInfo')
this.addScript('file-info.js','FileInfo'
                ,["org.xidea.jsidoc.export:DependenceInfo"])
this.addScript('globals-info.js','RootInfo'
                ,["FileInfo","PackageInfo"])

this.addScript("source-entry.js",'SourceEntry'
                ,"org.xidea.sandbox.syntax.ECMAParser");
this.addScript("doc-entry.js",'DocEntry'
                ,'SourceEntry');

this.addScript("tags.js",['accessTag','flagTag','valueTag','valuesTag']);


this.addScript('object-info.js',['ObjectInfo','UnknowInfo']
                ,["SourceEntry",'DocEntry'])

this.addScript('function-info.js',['FunctionInfo','ConstructorInfo']
                ,["ObjectInfo",'MemberInfo','TypeInfo'])

this.addScript('member-info.js','MemberInfo'
                ,["ObjectInfo",'DocEntry'])

this.addScript('type-info.js',['TypeInfo','ParamInfo'])


this.addScript("menu.js",'MenuUI');
//createPrototypeStack
this.addScript("fn.js",['createPrototypeStack','accessOrder','findSupperInfo','scrollOut']);


this.addDependence("*","fn.js");
this.addDependence("*","*",true);