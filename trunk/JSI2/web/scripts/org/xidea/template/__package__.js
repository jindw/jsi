this.addScript('template.js',"Template");

this.addScript('parser.js',["Parser","EL_TYPE","VAR_TYPE","IF_TYPE","ELSE_TYPE","FOR_TYPE","ATTRIBUTE_TYPE","EL_TYPE_XML_TEXT","FOR_KEY","TEMPLATE_NS_REG"]);
this.addScript('text-parser.js',["TextParser","parseText","parseEL"]
               ,["Parser","EL_TYPE","VAR_TYPE","IF_TYPE","ELSE_TYPE","FOR_TYPE","ATTRIBUTE_TYPE","EL_TYPE_XML_TEXT","FOR_KEY","TEMPLATE_NS_REG"]);
this.addScript('xml-parser.js',"XMLParser"
               ,["TextParser","parseText","parseEL","EL_TYPE","VAR_TYPE","IF_TYPE","ELSE_TYPE","FOR_TYPE","ATTRIBUTE_TYPE","EL_TYPE_XML_TEXT","FOR_KEY","TEMPLATE_NS_REG"]); 

//this.addDependence('template.js',"xml-parser.js");