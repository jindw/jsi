this.addScript("request.js",["Request"]
                ,["$log","XMLHttpRequest"]);
this.addScript("xhr.js","XMLHttpRequest");
this.addScript("json.js",['JSON']);

this.addScript("fn.js",['xmlReplacer','loadTextByURL']
                ,["$log",'XMLHttpRequest']);
this.addScript("log.js","$log")

//findGlobalsAsList 是为java提供的接口的。
this.addScript("find-globals.js",['findGlobals','findGlobalsAsList']
				,"$log");
//无压缩功能的Zip文件生成（UTF8编码）
this.addScript("zip.js","Zip")