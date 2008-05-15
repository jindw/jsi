this.addScript("request.js",["Request"]);
this.addScript("json.js",['JSON']);
this.addScript("template.js",['Template','OutputContext'],
                null, 
                ["Request","tag.js"]);
this.addScript("tag.js",['DefaultTag','AbstractTag','XHTMLTag','CoreTag']);    