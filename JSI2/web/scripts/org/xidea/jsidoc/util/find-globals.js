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
            '/.*/'
          ].join('|'),'m');

function replaceSpecialEntry(source){
    var head = '';
    var tail = source;
    var p1
    outer:
    while(p1 = specialRegExp.exec(tail)){
        var p2 = p1.index + p1[0].length;
        var p1 = p1.index;
        if(tail.charAt(p1) == '/'){
            switch(tail.charAt(p1 + 1)){
                case '/':
                case '*':
                    head += tail.substr(0,p1);
                    tail = tail.substr(p2+1);
                    continue outer;
            }
            try{//试探正则
                new Function(head+tail.replace(specialRegExp,"/\\$&"));
                //是正则
                p2 = p1;
                while((p2 = tail.indexOf('/',p2)+1)>p1){
                    //println([p1,p2]);//,tail.substring(p1,p2)
                    try{
                        var text = tail.substring(p1,p2);
                        if(/.*/.test(text)){//有效正则
                            new Function(text);
                        }
                        head += tail.substr(0,p1)+"/./";
                        tail = tail.substr(p2);
                        continue outer;
                    }catch(e){
                        //无效，继续探测
                    }
                }
                throw new Error("怎么可能？？^_^");
            }catch(e){
                //只是一个除号：（
                head += tail.substr(0,p1+1);
                tail = tail.substr(p1+1);
                continue outer;
            }
        }else{
            head += tail.substr(0,p1)+'""';
            tail = tail.substr(p2+1);
            continue outer;
        }
    }
    return head + tail;
}

function findGlobals(source){
    source = replaceSpecialEntry(source.replace(/^\s*#.*/,''));
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
/**
 * java 接口
 * @param <String>source 脚本源码
 * @return java.util.Collection 返回全局id集合
 */
function findGlobalsAsList(source){
    var result = findGlobals(source)
    var list = new java.util.ArrayList();
    for (var i = 0; i < result.length; i++) {
        list.add(result[i]);
    }
    return list;
}