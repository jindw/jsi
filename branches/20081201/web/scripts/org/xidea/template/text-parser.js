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
TextParser.prototype = new Parser();
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
        context.append.apply(context,parseText(node,false));
        return true;
    }
})




function parseText(text,xmlText,xmlAttr){
    if(!text){
        return [];
    }
    var buf = [];
    var pattern = new RegExp(/(\\*)\$([a-zA-Z!]{0,5}\{)/g)  //允许$for{} $if{} $end ...  see CT????
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
            buf.push(match[1].substr(0,parseInt(match[1].length / 2)) + '$')
            text = text.substr(expressionBegin+1);
        }else{
            fn = fn.substr(0,fn.length-1);
            //expression:
            while((expressionEnd = text.indexOf("}",expressionEnd+1))>0){
                try{
                    var expression = text.substring(expressionBegin ,expressionEnd );
                    expression = parseEL(expression);
                    if(xmlAttr){
                    	buf.push([ATTRIBUTE_TYPE,expression]);
                    }else{
                    	buf.push([xmlText ? EL_TYPE_XML_TEXT : EL_TYPE,expression]);
                    }
                    
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
    if(xmlText||xmlAttr){
        var begin = buf.length;
        while(begin--){
            //hack match reuse match as item
            var match = buf[begin];
            if(match == ''){
            	buf.splice(begin,1);
            }
            if(match.constructor == String){
                buf[begin] = match.replace(xmlAttr?/[<>&'"]/g:/[<>&]/g,xmlReplacer);
            }
        }
    }
    return buf;
}

function parseFN(fn,expression){
    if(fn){
        switch(fn){
            case 'for':
            //parseFor();
        }
        throw new Error("不支持指令："+fn);
    }
}

/**
 * 异常一定要抛出去，让parseText做回退处理
 */
function parseEL(expression){
    var el2 = expression.replace(/for\s*\./g,"_.");
    new Function(el2);
    
    if(expression != el2){
        expression = compileEL(expression)
    }
    expression = expression.replace(/^\s+|[\s]+$/g,'');//trim
    if(/^(?:true|false|[\d\.]+|(?:"[^"]*?"|'[^']*?'))$/.test(expression)){//true,false,number,string
        return [window.eval(expression)];
    } else if(/^[_$a-zA-Z](?:[\.\s\w\_]|\[(?:"[^"]*?"|'[^']*?'|\d+)\])*$/.test(expression)){//array[1.2];这类格式不处理
        var tokens = expression.match(/[\w_\$]+|"[^"]*?"|'[^']*?'/g);//[\d\.]+|
        var i = tokens.length;
        while(i--){
        	var item = tokens[i];
        	if(/['"]/.test(item)){
        		item = window.eval(item);
        		if(item.indexOf('.')>=0){
        			tokens = null;
        			break;
        		}
        	}
            tokens[i] = item;
        }
        if(tokens){
            if(tokens[0]=='this' && tokens[1]=='for'){
                tokens.shift();
            }
            return tokens.reverse().join('.');
        }
    }
    expression = expression.replace(/[\s;]+$/g,'');//
    var pos = expression.length;
    while((pos = expression.lastIndexOf(';',pos-1))>0){
        try{
            new Function(expression.substr(0,pos));
            new Function(expression.substr(pos));
            break;
        }catch(e){}
    }
    pos++;
    expression = "with(this){"+expression.substr(0,pos)+"return "+expression.substr(pos)+"}";
    return buildFunction(expression);
}
function buildFunction(source){
    var expression = new Function(source);
    //$log.trace(expression)
    expression.toString = function(){
        return "function(){"+source+"}";
    }
    return expression;
}


function compileEL(el){
    if(!/'"\//.test(el)){
        return el.replace(/\bfor\s*\./g,FOR_KEY+'.')
    }
    //好复杂的一段，这里还不够完善
    el.replace(/(\bfor\s*\.)||'[\\.]*?'|"[\\.]*?"|[\s\S]*?/g,function(code,_for){
        if(_for){
            return FOR_KEY+'.';
        }else{
            return code;
        }
    })
}

function parseFor(el){
    //与CT相差太远
    try{
        new Function(el);
        el = '{'+el+'}'
    }catch(e){
        new Function(el = '['+el+']');
    }
}



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