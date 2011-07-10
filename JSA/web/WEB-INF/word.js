/**
 * 扩展xml标记语法 <ex:word>百科此条</ex:word>
 */
context.addNodeParser(function(node,context,chain){
    if(node.namespaceURI == "http://www.xidea.org/ns/lite/example"
    	 && node.localName == 'word'){
    	var text = String(node.textContent).replace(/^\s+|\s+$/g,'');
    	var word = java.net.URLEncoder.encode(text,"GBK");
    	var content = get("http://baike.baidu.com/searchword/?word="+word+"&pic=1&sug=1&rsp=-1");
    	var url = content.replace(/[\s\S]+content='0;URL=([^']+)[\s\S]*/,'$1')
    	context.append("<a href='http://baike.baidu.com"+url+"'>"+text+"</a>");
    }else{
    	chain.process(node);
    } 
});
/**
 * 扩展文本语法 $word{百科此条}
 */
context.addTextParser({
	parse:function(text,start,context){
		var start = text.indexOf('{',start);
		var end = text.indexOf('}',start);
		var text = text.substring(start+1,end).replace(/^\s+|\s+$/g,'');
	    var word = java.net.URLEncoder.encode(text,"GBK");
	    var content = get("http://baike.baidu.com/searchword/?word="+word+"&pic=1&sug=1&rsp=-1");
	    var url = content.replace(/[\s\S]+content='0;URL=([^']+)[\s\S]*/,'$1')
	    context.append("<a href='http://baike.baidu.com"+url+"'>"+text+"</a>");
	    return end+1;
	},
	findStart:function(text,start,otherStart){
		var p = text.indexOf("$word{",start);
		if(p<otherStart){
			//java.lang.System.out.println(["@",text,start,otherStart,p].join('=='))
			return p;
		}else{
			return -1;
		}
	}
});

function get(url){
	var url = new java.net.URL(url);
	var s = url.openStream();
	var result = Packages.org.xidea.jsi.impl.JSIText.loadText(s,"utf-8");
    s.close();
    return String(result);
}