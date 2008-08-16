/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: template.js,v 1.4 2008/02/28 14:39:06 jindw Exp $
 */
//parse

//add as default
function TextParser(){
    this.parserList = this.parserList.concat([]);
    this.result = [];
}





/**
 * 解析函数集
 * @private
 */
TextParser.prototype = new TemplateParser();
TextParser.prototype.parse = function(url){
    var xhr = new XMLHttpRequest();
    xhr.open("get",url,true)
    xhr.send('');
    this.parseNode(xhr.responseText);
    return this.reuslt;
}
//parse text
TextParser.prototype.addParser(function(node,context){
    if(node.constructor == String){
        context.append.apply(context,parseText(node,true));
        return true;
    }
})




function parseText(text,unescape){
    if(!text){
        return [];
    }
    var buf = [];
    var pattern = new RegExp(/(\\*)\$([a-zA-Z!]{0,5}\{|end)/g)  //允许$for{} $if{} $end ...  see CT????
    //var pattern = /(\\*)\$\{/g
    var match ;
    //seach:
    while(match = pattern && pattern.exec(text)){
        var begin = match.index;
        var expressionBegin = begin + match[0].length;
        var expressionEnd = expressionBegin;
        var fn = match[2];
        
        begin && buf.push(text.substr(0,begin));
        
        if(match[1].length & 1){//转义后，打印转义结果，跳过
            buf.push(match[1].substr(0,parseInt(match[1].length / 2)))
            begin = expressionBegin;
        }else if(fn == 'end'){//结束标签
            buf.push([]);
            begin = expressionBegin;
        }else{
            fn = fn.substr(0,fn.length-1);
            //expression:
            while((expressionEnd = text.indexOf("}",expressionEnd+1))>0){
                try{
                    var expression = text.substring(expressionBegin ,expressionEnd );
                    expression = parseEL(fn,expression);
                    buf.push([0,expression,unescape])
                    text = text.substr(expressionEnd+1);
                    pattern = text && new RegExp(pattern);
                    //continue seach;
                    break;
                }catch(e){}
            }
        }
    }
    text && buf.push(text);
    //hack reuse begin as index
    if(!unescape){
        var begin = buf.length;
        while(begin--){
            //hack match reuse match as item
            var match = buf[begin];
            if(match.constructor == String){
                buf[begin] = match.replace(/[<>&'"]/g,xmlReplacer);
            }
        }
    }
    return buf;
}




function parseEL(fn,expression){
    if(fn){
        switch(fn){
            case 'for':
            //parseFor();
        }
        throw new Error("不支持指令："+fn);
    }else{
        var el2 = expression.replace(/for\s*\./g,"_.");
        new Function(el2);
        if(expression != el2){
            expression = compileEL(expression)
        }
        return expression;
    }
}

function compileEL(el){
    if(!/'"\//.test(el)){
        return el.replace(/\bfor\s*\./g,"this.$for.")
    }
    //好复杂的一段，这里还不够完善
    el.replace(/(\bfor\s*\.)||'[\\.]*?'|"[\\.]*?"|[\s\S]*?/g,function(code,_for){
        if(_for){
            return "this.$for."
        }else{
            return code;
        }
    })
}

function parseFor(){
    //与CT相差太远
    try{
        new Function(el);
        el = '{'+el+'}'
    }catch(e){
        new Function(el = '['+el+']');
    }
}



//html
var specialRegExp = [
            //'/(?:\\\\.|[^/\\n\\r])+/',     //regexp 有bug   /\/(?:\\.|[^/\n\r])+\//
            '/(?:\\\\.|(?:\\[\\\\.|[^\\n\\r]\\])|[^/\\n\\r])+/[gim]*',     //regexp 好复杂啊   /\/(?:\\.|(?:\[\\.|[^\n\r]\])|[^/\n\r])+\/[gim]*/
            '"(?:\\\\(?:.|\n|\r\n?)|[^"\\n\\r])*"',
            "'(?:\\\\(?:.|\n|\r\n?)|[^'\\n\\r])*'"    //string
            ].join('|'); 



function xmlReplacer(c){
    switch(c){
        case '<':
          return '&lt;';
        case '>':
          return '&gt;';
        case '&':
          return '&amp;';
        case "'":
          return '&#39;';
        case '"':
          return '&#34;';
    }
}