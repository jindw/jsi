this.addScript("request.js",["Request"]);
this.addScript("json.js",['JSON']);

this.addScript('template.js',"Template");
this.addScript('template-parser.js',["TemplateParser","EL_TYPE","VAR_TYPE","IF_TYPE","ELSE_TYPE","FOR_TYPE","ATTRIBUTE_TYPE","FOR_KEY"]);
this.addScript('text-parser.js',["TextParser","parseText","parseEL"]
               ,["TemplateParser","EL_TYPE","VAR_TYPE","IF_TYPE","ELSE_TYPE","FOR_TYPE","ATTRIBUTE_TYPE","FOR_KEY"]);
this.addScript('xml-parser.js',"XMLParser"
               ,["TextParser","parseText","parseEL","EL_TYPE","VAR_TYPE","IF_TYPE","ELSE_TYPE","FOR_TYPE","ATTRIBUTE_TYPE","FOR_KEY"]); 

this.addScript("fn.js",['xmlReplacer','loadTextByURL']);

//findGlobalsAsList 是为java提供的接口的。
this.addScript("find-globals.js",['findGlobals','findGlobalsAsList']);