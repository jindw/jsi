this.addScript('globals-info.js','RootInfo'
                ,['createPrototypeStack','FileInfo','PackageInfo']
                ,['.util:$log','SourceEntry','computeURL','ObjectInfo']);

this.addScript('package-info.js','PackageInfo'
                ,0
                ,['SourceEntry','FileInfo','RootInfo']);

this.addScript('file-info.js','FileInfo'
                ,0
                ,['.util:$log','.export:DependenceInfo','SourceEntry','PackageInfo','ObjectInfo']);

this.addScript('type-info.js',['TypeInfo','ParamInfo']);

this.addScript('fn.js',['accessOrder','scrollOut','computeURL','findSupperInfo','createPrototypeStack']);

this.addScript('source-entry.js','SourceEntry'
                ,'ECMAParser'
                ,['JSIDoc','DocEntry']);


this.addScript('jsidoc.js','JSIDoc'
                ,0
                ,['MenuUI','SourceEntry','.export:findPackages','.util:JSON','.export:ExportUI','.util:loadText','PackageInfo']);

this.addScript('syntax-parser.js',['SyntaxParser','ECMAParser','LineIterator']);

this.addScript('function-info.js',['FunctionInfo','ConstructorInfo']
                ,['createPrototypeStack','ObjectInfo']
                ,['findSupperInfo','DocEntry','MemberInfo','type-info.js']);

this.addScript('object-info.js',['ObjectInfo','UnknowInfo']
                ,['createPrototypeStack','DocEntry']
                ,['findSupperInfo','function-info.js','MemberInfo']);

this.addScript('member-info.js','MemberInfo'
                ,['createPrototypeStack','DocEntry','ObjectInfo']
                ,['.util:$log','accessOrder']);

this.addScript('menu.js','MenuUI'
                ,0
                ,'JSIDoc');

this.addScript('tags.js',['accessTag','valuesTag','flagTag','valueTag']);

this.addScript('doc-entry.js','DocEntry'
                ,'tags.js');

