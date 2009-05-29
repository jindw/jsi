this.addScript("request.js",["Request"]
                ,["$log","XMLHttpRequest"]);
this.addScript("xhr.js","XMLHttpRequest");
this.addScript("json.js",['JSON']);

this.addScript("fn.js",['xmlReplacer','loadTextByURL']
                ,"$log");
this.addScript("log.js","$log")
//findGlobalsAsList 是为java提供的接口的。
this.addScript("find-globals.js",['findGlobals','findGlobalsAsList']
				,"$log");
				
this.addScript("base64.js",
	['UTF8ByteArrayToString'
	,'UTF16ByteArrayToString'
	,'stringToByteArray'
	,'stringToUTF8ByteArray'
	,'stringToUTF16ByteArray'
	,'base64ToByteArray','atob'
	,'byteArrayToBase64','btoa'
	])
	
this.addScript("zip.js","Zip"
	,["byteArrayToBase64","stringToByteArray","stringToUTF8ByteArray"])