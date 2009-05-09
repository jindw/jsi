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
/**
 * 删除注释，长字符串=>"",正则=>/./
 */
function replaceSpecialEntry(source){
    var head = '';
    var tail = source;
    var p1
    outer:
    while(p1 = specialRegExp.exec(tail)){
    //for(;p1 = specialRegExp.exec(tail);specialRegExp.index=0,specialRegExp.lastIndex=0){
        var p2 = p1.index + p1[0].length;
        var p1 = p1.index;
        if(tail.charAt(p1) == '/'){
            switch(tail.charAt(p1 + 1)){
                case '/':
                case '*':
                    head += tail.substr(0,p1);
                    tail = tail.substr(p2);
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
            tail = tail.substr(p2);
            continue outer;
        }
    }
    return head + tail;
}
/**
 * 替换代码
 */
function replaceFunctionBody(text){
	var result = [];
	var fnExp = /\bfunction\b\s*([\w\$]+)?[^\{]+\{/;
	fnExp.test('')
	var m;
	while(m = fnExp.exec(text)) {
		result.push(text.substring(0,m.index))
		if(m[1]){
			result.push("function ",m[1],"(){}");
		}else{
			result.push("function(){}");
		}
		var begin = m.index+m[0].length-1;
		text = text.substring(begin);
		var end = 0;
		var depth=0;
		var groupExp = /[\{\}]/g;
		groupExp.test('')
		while(m = groupExp.exec(text)){
			if(m[0] == '{'){
				depth++;
			}else{
				depth--;
				if(depth == 0){
					end = m.index;
					//print("!!!"+end+"---"+text)
					break;
				}
			}
		}
		if(end>0){
			text = text.substring(end+1);
		}else{
			$log.error("function not end:"+text+"#"+depth)
			throw Error("function not end:"+text+"#"+depth);
		}
	}
	result.push(text);
	return result.join('')
}
function replaceQuteBody(text){
	var result = [];
	var m;
	while(m = /\[|(\bfor\b)?\s*\(/.exec(text)) {//if switch
		var begin = m.index+m[0].length;
		var tail = text.substring(begin);
		result.push(text.substring(0,begin)); 
		if(m[1]){
			text = tail;
			continue;
		}
		var end =0;
		var depth=0;
		while(m = /([\[\(])|[\]\)]/g.exec(tail)){
			if(m[1]){
				depth++;
			}else{
				depth--;
				if(depth == -1){
					end = m.index;
					break;
				}
			}
		}
		var value = tail.substring(0,end)
		if(/[^\s\t\u3000]/.test(value)){
			result.push(0);//函数申明一起替换了，安全的用0吧
		}
		text = tail.substring(end);
		
	}
	result.push(text);
	return result.join('')
}
/**
 * 补全全部缺少的；
 */
function formualSource(source){
	var lines = source.replace(/^\s*$/mg,'').split(/[\r\n]/);
	while(lines.length>1){
		var tail2 = lines.pop().replace(/\s+$/,'');
		var tail1 = lines.pop();
		lines.push(tail2+tail1);
		if(tail1.charCodeAt(tail1.length-1) != ';'){
			try{
				//$log.error(lines.join('\n'))
				new Function(lines.join('\n'),"xx")
				//$log.info(2222)
			}catch(e){
				lines.pop();
				lines.push(tail2+';\n'+tail1);
			}
		}
		
		
	}
	return lines[0];
}
function findGlobals(source){
	if(source instanceof Function){
		source = (''+source).replace(/^\s*function[^\}]*?\{|\}\s*$/g,'');
	}
	var source1 = replaceSpecialEntry(source.replace(/^\s*#.*/,''));
    source2 = replaceFunctionBody(source1);
    source3 = replaceQuteBody(source2);
    //找到办法不用判断了，省心了。。。。
    //var objectPattern = /\{\s*(?:[\$\w\d]+|"")\:/mg
    //吧object 空白连接起来
    source = source3.replace(/\s+\:/g,':');
    try{
    	new Function(source);
    }catch(e){
    	$log.error("全局变量探测异常警告",[source1,source2,source3,source].join("\n=====\n"),e);
    }
    
    source1=source2=source3=0;
    source = formualSource(source);
    //简单的实现，还以为考虑的问题很多很多：
    var varFlagMap = {};
    var varPattern = /\b(var|function)\b\s*([\w\$]+)\s*/mg;
    var begin = 0;
    var match;
    while(match = varPattern.exec(source)){
        switch(match[1]){
        case 'var':
            begin = match.index;
            varFlagMap[match[2]] = 1;
            var temp = source.indexOf(';',begin);
            try{
                temp = source.substring(begin,temp);
                //确保，函数，数组，函数调用，都清理完毕
                var subVarReg = /,\s*([\w\$]+)/g;
                while(match=subVarReg.exec(temp)){
		            var next = source.charAt(match.index + match[0].length);
		            switch(next){
		            	case ',':
		            	case ';':
		            	case '=':
	                    varFlagMap[match[1]] = 1;
		            }
                }
            }catch(e){
                continue;
            }
            break;
        case 'function':
            varFlagMap[match[2]] = 1;
        }
    }
    var result = [];
    for(match in varFlagMap){
        result.push(match)
    }
    //alert(result.join('\n'))
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