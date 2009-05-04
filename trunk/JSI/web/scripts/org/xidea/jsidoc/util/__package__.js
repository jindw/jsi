this.addScript("request.js",["Request"]
                ,"$log");
this.addScript("json.js",['JSON']);

this.addScript("fn.js",['xmlReplacer','loadTextByURL']
                ,"$log");
this.addScript("log.js","$log")
//findGlobalsAsList 是为java提供的接口的。
this.addScript("find-globals.js",['findGlobals','findGlobalsAsList']);