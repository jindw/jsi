/*
XMLHttpRequest.prototype.open = function(oldOpen){
	return function(method,url,asyn){
		url = url.replace(/^(classpath:\/\/\/)\?path=/,"$1");
		this.url = url;
		this.asyn - asyn;
		if(!/^classpath:\/\//.test(url)){
			oldOpen.apply(this,arguments);
	    }
    };
}(XMLHttpRequest.prototype.open);

XMLHttpRequest.prototype.send=function(oldSend){
    return function(param){
	    if(this.url && /^classpath:\/\//.test(this.url)){
            var reader = new java.io.InputStreamReader(
                new java.lang.String().getClass().getResourceAsStream(this.url.substr(12)),"utf-8");
	        var cbuf = java.nio.CharBuffer.allocate(1024);
	        var sb = new java.lang.StringBuffer();
	        var count = 0;
	        while((count = reader.read(cbuf))>0){
	            sb.append(cbuf.array(),0,count);
	            cbuf.clear();
	        }
			this.responseText = new String(sb.toString()).toString();
    	    //alert(String(sb.toString()))
	    }else{
	    	oldSend.apply(this,arguments);
	    }
    }
}(XMLHttpRequest.prototype.send);
*/
$import("org.xidea.jsidoc.export:Exporter");
var exporter = new Exporter();
exporter.addFeatrue("mixTemplate");
exporter.addImport("org.xidea.jsidoc:JSIDoc");
alert (exporter.getXMLContent())

