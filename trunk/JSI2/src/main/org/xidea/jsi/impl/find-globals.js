/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */
var specialRegExp = new RegExp([
            //muti-comment
            '/\\*(?:[^\\*]|\\*[^/])*\\*/',
            //single-comment
            '//.*$',
            //string
            '"(?:\\\\(?:.|\\r|\\n|\\r\\n)|[^"\\n\\r])*"',
            "'(?:\\\\(?:.|\\r|\\n|\\r\\n)|[^'\\n\\r])*'",      
            //process                                          
            //regexp 有bug   /\/(?:\\.|[^/\n\r])+\//
            //'/(?:\\\\.|[^/\\n\\r])+/',                       
            //regexp 好复杂啊   
            // /\/(?:
            //      \\.|
            //      (?:
            //        \[(?:
            //            \\.|
            //            [^\n\r])*
            //        \]
            //      )|
            //      [^/\n\r\[\]]
            //    )+\/[gim]*/
            //'/(?:\\\\.|(?:\\[\\\\.|[^\\n\\r]\\])|[^/\\n\\r])+/[gim]*'
            
            //算了，我还是用预处理后的结果吧：（
            '/\\./[gim]*'
          ].join('|'),'gm');

function replaceRegExp(source){
    var pattern = /\/[^\/\*\r\n].*\//;
    var head = '';
    var tail = source;
    while((p = tail.search(pattern))>=0){
        try{
            new Function(head+tail.replace(pattern,"/\\$&"));
            //是正则
            var p2 = p+1;
            while((p2 = tail.indexOf('/',p2))>p){
                try{
                    new Function(tail.substring(p,p2));
                    //有效正则
                    head += tail.substr(0,p)+"/./";
                    tail = tail.substr(p2);
                    continue;
                }catch(e){
                    //无效，继续探测
                }
            }
            throw new Error("怎么可能？？^_^");
        }catch(e){
            //只是一个除号：（
            head += tail.substr(0,p+2);
            tail = tail.substr(p+2);
        }
    }
    return head + tail;
}



function specialReplacer(text){
    if(text.charAt(0) == '/'){
        switch(text.charAt(1)){
            case '/':
            case '*':
              return '';
        }
    }
    return '""';
}
function findGlobals(source){
    source = replaceRegExp(source.replace(/^\s*#.*/,''));
    source = source.replace(specialRegExp,specialReplacer);
    //简单的实现，还以为考虑的问题很多很多：
    var varFlagMap = {};
    var scopePattern = /\b(function\b[^\(]*)[^{]+\{|\{|\}|\[|\]/mg;//|{\s*(?:[\$\w\d]+\s*\:\s*(?:for|while|do)\b|""\:)
    //找到办法不用判断了，省心了。。。。
    //var objectPattern = /\{\s*(?:[\$\w\d]+|"")\:/mg
    var varPattern = /\b(var|function|,)\b\s*([\w\$]+)\s*/mg;
    //var lineParrern = /([\$\w]+|[^\$\w])\s*[\r\n]+\s*([\$\w]+|[^\$\w])/g
    var buf = [];
    var fnDepth = 0;
    var arrayDepth = 0;
    var begin = 0;
    var match;
    while(match = scopePattern.exec(source)){
        switch(match[0] ){
        //array
        case '[':
            if(!fnDepth){
                if(!arrayDepth){
                    buf.push(source.substring(begin,match.index),'[]');
                }
                arrayDepth ++;
            }
            break;
        case ']':
            if(!fnDepth){
                arrayDepth --;
                if(!arrayDepth){
                    begin = match.index+1;
                }
            }
            break;
        //function
        case '{':
            if(!arrayDepth && fnDepth){//in function
                fnDepth++;
            }
            break;
        case '}':
            if(!arrayDepth && fnDepth){//in function
                fnDepth--;
                if(fnDepth == 0){
                    begin = match.index+1;
                }
            }
            break;
        default://function.. 
            if(!arrayDepth){
                if(!fnDepth){
                    buf.push(source.substring(begin,match.index),match[1],'}');
                }
                fnDepth++;
            }
            break;
        }
    }
    buf.push(source.substr(begin))
    source=buf.join('');
    source = source.replace(/([\w\$\]])\s*\([\w\$\d,]*\)/m,'$1()');
    begin = 0;
    while(match = varPattern.exec(source)){
        switch(match[1]){
        case 'var':
            begin = match.index;
        case 'function':
            varFlagMap[match[2]] = 1;
        default://,
            var next = source.charAt(match.index + match[0].length);
            if(next!=':'){
                var temp = source.indexOf(';',begin);
                if(temp>0 && temp<match.index){
                    continue;
                }
                try{
                    //不知道是不是还有什么问题
                    temp = source.substring(begin,match.index);
                    //if(/var|if|else/.test(temp)){continue;}
                    temp = temp.replace(/[\r\n]/g,' ');
                    new Function(temp+',a;')
                }catch(e){
                    continue;
                }
                varFlagMap[match[2]] = 1;
            }
        }
    }
    var result = [];
    for(match in varFlagMap){
        result.push(match)
    }
    return result;
}