$if = {
  parse : function(text,start,context){
    var end = text.indexOf("}",start);
    var el = text.substring(start+4,end);
    el = el.replace(/^\s*not\b/,'!');
    context.appendIf(el);
    return end+2;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{if",start);
  }
}
$else = {
  parse : function(text,start,context){
    context.appendEnd();
    context.appendElse(null);
    return start+7;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{else}",start);
  }
}
$elseif = {
  parse : function(text,start,context){
    context.appendEnd();
    var end = text.indexOf("}",start);
    var el = text.substring(start+8,end);
    el = el.replace(/^\s*not\b/,'!');
    context.appendElse(el);
    return end+2;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{elseif",start);
  }
}

$end = {
  parse : function(text,start,context){
    var end = text.indexOf("}",start);
    var word = text.substring(start+2,end);
    switch(word){
        case 'if':
        case 'foreach':
        context.appendEnd();
        //java.lang.System.out.println([context.toCode()].join('\n====\n'))
    
        break;
        default:
        return start;
    }
    return end+1;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{/",start);
  }
}
//  {foreach $additionalName in $additionalNames}
var $for = {
  parse : function(text,start,context){
    var end = text.indexOf("}",start);
    var el = text.substring(start+9,end);
    el = el.replace(/^\s+/,'');
    var split = el.indexOf('in')
    var varName = el.substring(0,split).replace(/\s+$/,'');
    var items = el.substring(split+3);
    context.appendFor(varName,items,null);
    //java.lang.System.out.println([varName,items].join('\n====\n'))
    return end+2;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{foreach",start);
  }
};
//{ifempty}
var $ifempty = {
  priority:100,//默认优先级是1，这个最低，所以设置为0
  parse : function(text,start,context){
    context.appendEnd();
    context.appendElse(null);
    return start+10;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{ifempty}",start);
  }
};
$el = {
  priority:-1,//默认优先级是1，这个最低，所以设置为0
  parse : function(text,start,context){
    var end = text.indexOf("}",start);
    var el = text.substring(start+1,end);
    context.appendEL(el);
    return end+1;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{",start);
  }
}
//{namespace examples.simple}
var $namespace = {
  priority:100,//默认优先级是1，这个最低，所以设置为0
  parse : function(text,start,context){
    var match = text.match(/\{namespace\s+([\w\.]+)\}/);
    context.setAttribute('com.google.closure:namespace',match[1]);
    return start+match[0].length;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{namespace",start);
  }
};


//{template fn}{/template}
var clientParser = new Packages.org.xidea.lite.parser.impl.ClientParser();
var $template = {
  parse : function(text,start,context){
    var text2 = text.substring(start);
    var match = text.match(/{template\s*([^\s}]+)}([\s\S]+){\/template}/);
    var id = match[1];
    var client = match[2];
    var ns = context.getAttribute('com.google.closure:namespace');
    if(ns){
        print(ns+'/'+id)
        id = ns + id;
        ns = ns.split('.');
        //window.n1 || (window.n1={}) && 
        var buf = ["window.",ns[0],"||(window.",ns[0],"={});"]
        for(var i = 1;i<ns.length;i++){
            var prefix = ns.slice(0,i+1).join('.');
            buf.push(prefix ,'||(',prefix,'={});' )
        }
        context.append("<!--//--><script>"+buf.join('')+"//</script>");
    }
    clientParser.parse(id,client,context);
    return start +match[0].length;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{template",start);
  }
}
context.addTextParser($el);
context.addTextParser($if);
context.addTextParser($else);
context.addTextParser($elseif);
context.addTextParser($end);
context.addTextParser($for);
context.addTextParser($ifempty);
context.addTextParser($namespace);
context.addTextParser($template);