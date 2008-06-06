/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */

this.addScript = function(){
    if(arguments[1] == "*"){
        arguments[1] = findGlobals($this.getSource(arguments[0])+'');
    }
    //println([arguments[2]? arguments[2]:null,arguments[2]? 123:null,typeof arguments[2],typeof arguments[3],null,arguments[2] || null,arguments[3] || null])
    $this.addScript(arguments[0],toJavaObject(arguments[1]),toJavaObject(arguments[2]), toJavaObject(arguments[3]));
};
this.addDependence = function(){
    $this.addDependence(arguments[0],arguments[1],!!arguments[2])
};
this.setImplementation = function(){
    $this.setImplementation(arguments[0])
};

function toJavaObject(object) {
    if (object instanceof Array) {
        var result = new java.util.ArrayList();
        for (var i = 0; i < object.length; i++) {
            result.add(object[i]);
        }
        return result;
    } else if(object == null){
        return null;
    }else{
        return object;
    }
}

//以下是find-globals.js


/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */


var specialRegExp = new RegExp(['/\\*(?:[^\\*]|\\*[^/])*\\*/',//muti-comment
            '//.*$',                      //single-comment
            //'/(?:\\\\.|[^/\\n\\r])+/',     //regexp 有bug   /\/(?:\\.|[^/\n\r])+\//
            '/(?:\\\\.|(?:\\[\\\\.|[^\\n\\r]\\])|[^/\\n\\r])+/[gim]*',     //regexp 好复杂啊   /\/(?:\\.|(?:\[\\.|[^\n\r]\])|[^/\n\r])+\/[gim]*/
            '"(?:\\\\(?:.|\\r|\\n|\\r\\n)|[^"\\n\\r])*"',
            "'(?:\\\\(?:.|\\r|\\n|\\r\\n)|[^'\\n\\r])*'",    //string
            '^\\s*#.*'].join('|'),'gm');                         //process
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
    var source = source.replace(specialRegExp,specialReplacer);
    //简单的实现，为考虑的问题很多很多：
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
