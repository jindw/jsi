/*
 * JavaScript Integration Doc Tool
 */
this.addScript('doc-entry.js','DocEntry'
                            ,['valuesTag','accessTag','flagTag','valueTag'])

this.addScript('file-info.js',['FileInfo']
                            ,null
                            ,['SourceEntry','ObjectInfo','PackageInfo','org.xidea.jsidoc.export:DependenceInfo'])

this.addScript("fn.js",['computeURL','createPrototypeStack','accessOrder','findSupperInfo','scrollOut','findPackages']);

this.addScript('function-info.js',['FunctionInfo','ConstructorInfo']
                            ,['ObjectInfo','createPrototypeStack']
                            ,['TypeInfo','DocEntry','ParamInfo','MemberInfo','findSupperInfo'])

this.addScript('globals-info.js','RootInfo'
                            ,['FileInfo','PackageInfo','createPrototypeStack']
                            ,['SourceEntry','ObjectInfo','computeURL'])

this.addScript('jsidoc.js',['JSIDoc']
                            ,['org.xidea.jsidoc.util:Template']
                            ,['MenuUI','SourceEntry','PackageInfo','findPackages',
                                "org.xidea.jsidoc.util:JSON",
                                "org.xidea.jsidoc.util:loadTextByURL",
                                'org.xidea.jsidoc.export:ExportUI'])

this.addScript('member-info.js','MemberInfo'
                            ,['DocEntry','ObjectInfo','createPrototypeStack']
                            ,['accessOrder'])

this.addScript("menu.js",'MenuUI'
                ,0
                ,'org.xidea.jsidoc:JSIDoc');

this.addScript('object-info.js',['ObjectInfo','UnknowInfo']
                            ,['DocEntry','createPrototypeStack']
                            ,['FunctionInfo','MemberInfo','ConstructorInfo','findSupperInfo'])

this.addScript('package-info.js',['PackageInfo']
                            ,null
                            ,['SourceEntry','RootInfo','FileInfo'])

this.addScript('source-entry.js',['SourceEntry']
                            ,['ECMAParser']
                            ,['DocEntry','JSIDoc'])

this.addScript("syntax-parser.js",['SyntaxParser','ECMAParser','LineIterator']);

this.addScript('tags.js',['valuesTag','accessTag','flagTag','valueTag']);

this.addScript('type-info.js',['TypeInfo','ParamInfo'])