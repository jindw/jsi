this.addScript("request.js",["Request"]);
this.addScript("json.js",['JSON']);

this.addScript('template.js',"Template","XMLParser");
this.addScript('template-parser.js',"TemplateParser");
this.addScript('text-parser.js',["TextParser","parseText"]
               ,"TemplateParser");
this.addScript('xml-parser.js',"XMLParser"
               ,["TextParser","parseText"]); 
