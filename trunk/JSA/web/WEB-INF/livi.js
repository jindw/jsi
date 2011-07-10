//{{if test}}....{{/if}}
var $if = {//...
  parse : function(text,start,context){
    var end = text.indexOf("}}",start);
    var el = text.substring(start+5,end);
    context.appendIf(el);
    return end+2;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{{if",start);
  }
};
//{{for item:items}}....{{/for}}
var $for = {
  parse : function(text,start,context){
    var end = text.indexOf("}}",start);
    var el = text.substring(start+6,end);
    el = el.replace(/^\s+/,'');
    var split = el.indexOf(':')
    var varName = el.substring(0,split).replace(/\s+$/,'');
    var items = el.substring(split+1);
    context.appendFor(varName,items,null);
    return end+2;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{{for",start);
  }
};
//{{if test}}....{{else test2}}{{/else}}
var $else = {
  parse : function(text,start,context){
    var end = text.indexOf("}}",start);
    var el = text.substring(start+6,end);
    if(/^\s*$/.test(el)){
      context.appendElse(null);
    }else{
      context.appendElse(el);
    }
    return end+2;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{{else",start);
  }
};
//{{var name= value}}
//{{var name}}...{{/var}}
var $var = {
  parse : function(text,start,context){
    var end = text.indexOf("}}",start);
    var el = text.substring(start+5,end);
    el = el.replace(/^\s+|\s+$/,'');
    var split = el.indexOf('=')
    if(split>0){
      var value = el.substring(split+1);
      var varName = el.substring(0,split).replace(/\s+$/,'');
      context.appendVar(varName,value);
    }else{
      var varName = el;
      context.appendCaptrue(varName);
    }
    return end+2;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{{var",start);
  }
};
var $end = {
  parse : function(text,start,context){
    context.appendEnd();
    return text.indexOf("}}",start)+2;
  },
  findStart : function(text,start,otherStart){
    var begin = text.indexOf("{{/",start);
    if(begin>0){
        var end = text.indexOf("}}",begin);
        switch(text.substring(begin+3,end)){
          case 'if':
          case 'for':
          case 'else':
          case 'var':
          case 'end':
          return begin;
        }
    }
    return -1;
  }
}
//{{el}}
var $el = {
  priority:-1,//默认优先级是1，这个最低，所以设置为0
  parse : function(text,start,context){
    var end = text.indexOf("}}",start);
    var el = text.substring(start+2,end);
    context.appendEL(el);
    return end+2;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{{",start);
  }
}

//{{client fn}}{{/client}}
var clientParser = new Packages.org.xidea.lite.parser.impl.ClientParser();
var $client = {
  parse : function(text,start,context){
    var text2 = text.substring(start);
    var match = text.match(/{{client\s*([^\s}]+)}}([\s\S]+){{\/client}}/);
    var id = match[1];
    var client = match[2];
    //java.lang.System.out.println(match.join('\n====\n'))
    clientParser.parse(id,client,context);
    return start +match[0].length;
  },
  findStart : function(text,start,otherStart){
    return text.indexOf("{{client",start);
  }
}
context.addTextParser($el);
context.addTextParser($if);
context.addTextParser($for);
context.addTextParser($else);
context.addTextParser($var);
context.addTextParser($end);
context.addTextParser($client);