try{
	var UglifyJS = require("uglify-js");
}catch(e){
	try{
		var UglifyJS = require("uglifyjs");
	}catch(e){
	}
}

if(UglifyJS){
	compressJS = function(source,id){
		var old_warn = UglifyJS.AST_Node.warn_function
		var logs = [];
		UglifyJS.AST_Node.warn_function = function(args){
			logs.push(args);
		}
		var result = UglifyJS.minify(String(source), {fromString: true});
		var code = result.code;
		
		UglifyJS.AST_Node.warn_function = old_warn;
		if(!code){
			throw new Error('invalid compress result!\n'+logs+'\n\n'+source)
		}
		if(logs.length){
			console.warn('compress warn '+id,"\n"+logs.join('\n'))
		}
		return code;
	}
}

function formatJS(js){
	if(UglifyJS){
		var ast = UglifyJS.parse(js);
		ast.figure_out_scope();
		return ast.print_to_string({beautify:true}); 
	}
	return String(new Function(js)).replace(/^[^{]+\{([\s\S]*)\}/,'$1')
}
var uncompressed = /^[\t ]{2}|^\s*\/\/|\/\**[\r\n]/m;
var compressedCache = [];
function compressJS(source,file,root){
	source = source.replace(/^\/\*[\s\S]*?\*\/\s*/g,'');
	var i = compressedCache.indexOf(source);
	if(i>=0){
		if(i%2 == 0){
			var kv = compressedCache.splice(i,2)
			compressedCache.push(kv[0],kv[1]);
			return kv[1];
		}else{
			return source;
		}
	};
	var result = source;
	var sample = source.slice(source.length/10,source.length/1.1);
	//console.log(sample.length )
	if(sample.length < 200 || sample.match(uncompressed)){
		//console.log('\n=======\n',s);
		try{
			result= tokenCompress(source,file,root);
		}catch(e){
			//console.log(e)
			result= tokenCompress('this.x='+source,file,root).replace(/^this.x=|(});$/g,'$1') ;
		}
		//console.log('\&&&&\n',s,"====");
	}
	compressedCache.push(source,result);
	if(compressedCache.length>64){
		compressedCache.shift();
		compressedCache.shift();
	}
	return result;
}
function tokenCompress(source,file,root){
	source = String(source).replace(/\r\n?/g,'\n');
	if(source.search(uncompressed)<0){
		return source;
	}
	var ps = partitionJavaScript(source,file,root);
	var result = [];
	for(var i =0;i<ps.length;i++){
		var item = ps[i];
		switch(item.charAt()){
		case '\'':
		case '\"':
		case '`':
			result.push(item);//string
			break;
		case '/':
			//skip comment && reserve condition comment and regexp
			var stat = item.match(/^\/(?:(\*\s*@)|\/|\*)/);
			if(!stat || stat[1]){
				result.push(item);//regexp or condition comment
			}
			break;
			
		default:
			//result.push(item.replace(/^[ \t]+/gm,''));//被切开的语法块，前置换行，可能上上一个语法的结束语法，不能删除
			//console.log('%%<',item.replace(/^(\r\n?|\n)+|(\s)+/gm,'$1$2'),">%%")
			result.push(item.replace(/(\n)+|([^\S\r\n])+/gm,'$1$2').replace(/(?:([\r\n])+\s*)+/g,'$1'));
		}
	}
	return result.join('');
}



var TYPE_COMMENT=0, TYPE_SOURCE =1, TYPE_STRING =2, TYPE_REGEXP=3;
/**
* 如何token
* 如何补全; 能不补全就不补全
*/
function partitionJavaScript(source){
	/* *
	var exp1 = /\\.|[^\\\r\n\[\/]+/   // ']' is valid char
	var exp2 = /\[(?:\\.|[^\\\r\n\]])+]\]/    //'[' '/' is valid in []
	var exp = /\/(?:\\.|[^\\\r\n\[\/]+|\[(?:\\.|[^\\\r\n\]])+]\])*\/(?:[img]*\b|(?=[^\w]))/
	var xml = /<(?=(?:[A-Za-z_][\w_\-\.]*(?:\:[\w\-\.]*)?)(?:\s*\/?>|\s+\w))/i
	/* */
	var regexp = /`[^`]*`|'(?:\\.|[^'])*'|"(?:\\.|[^"])*"|\/\/.*|\/\*[\s\S]*?\*\/|\/(?=[^*\/].*\/(?:[img]*\b|[^\w]))|<(?=(?:[A-Za-z_][\w_\-\.]*(?:\:[\w\-\.]*)?)(?:\s*\/?>|\s+\w))/i;
	var m,result = [],latestType=-1;//not comment string regexp
	source = source.replace(/\r\n|\r/g,'\n');
	function append(token,type){
		//console.log([token,type])
		if(token){
			if(latestType == type){
				result[result.length-1]+=token;
			}else{
				result.push(token);
				latestType = type;
			}
		}
	}
	//console.log(source)
	regexp.lastIndex = 0;
	while(m = regexp.exec(source)){
		//console.log('line:'+m)
		if(m){
			var index = m.index;
			var m = m[0];
			var xml = m == '<'
			
			//console.log(m)
			if(m == '/' || xml){
				append(source.substring(0,index),TYPE_SOURCE)
				var m2 = (xml ?findXML:findExp)(result,source.substring(index));
				//console.log("exp:"+source.substr(index,5))
				if(m2){
					m = m2;
					if(xml){
						append('new XML(',TYPE_SOURCE)
						append('"'+m.replace(/["\r\n\\]/g,jsReplace)+'"',TYPE_STRING)
						append(')',TYPE_SOURCE)
					}else{
						append(m,TYPE_REGEXP)
					}
				}else{
					//避免误判为xml或者regexp
					append(m.replace(/^\//,'\t\/'),TYPE_SOURCE);
				}
			}else{//string,comment,other-source
				append(source.substring(0,index),TYPE_SOURCE)
				switch(m.charAt()){
				case '\'':
				case '\"':
				case '`':
					append(m,TYPE_STRING);//string
					break;
				case '/':
					var m2 = m.charAt(1);
					if(m2=='*' || m2 == '/'){//?maby code?
						append(m,TYPE_COMMENT);//string
						break;
					}
				default:
					append(m,TYPE_SOURCE);//code
				}
			}
			source = source.substring(m.length+index);
		}else{
			break;
		}
	}
	append(source,TYPE_SOURCE);
	return result;
}
/**
* 
运算符 ‘/’ 优先考虑
var i=0;		
if(i)alert(1)//...
/alert(2)/i
=> var i=0;if(i){alert(1)/alert(2)/i}
忽略 CDATA/textarea

*/
function findXML(result,source){
	var tag = source.match(/^<([a-z_][\w_\-\.]*(?:\:[a-z_][\w_\-\.]*)?)(?:\s*[\/>]|\s+[\w_])/i);
	if(tag){
		tag = tag[1];
		tag = tag.replace(/\.\-/g,'\\$&');
		var reg = new RegExp('<(/)?'+tag,'g');
		var depth = 0;
		reg.lastIndex = 0;
		while(tag = reg.exec(source)){
			if(tag[1]){
				if(--depth == 0){
					return source.substring(0,tag.index+tag[0].length+1)
				}else if(depth<0){
					return null;
				}
			}else{
				depth++;
			}
		}
	}else{
		return null;
	}
}
function jsReplace(c){
	switch(c){
		case '\r':
			return '\\r';
		case '\n':
			return '\\n\\\n';
		case '"':
			return '\\"';
		case '\\':
			return '\\\\';
		
	}
}


/**
* ''/b/ // a
* /xxx\///2; /**\/ //b=2; 
*/
function findExp(result,source){
	var i = result.length;
	while(i--){
		var line = result[i];
		//console.log(["reg check line:",line])
		if(!/^\/[\/*]|^\s+$/.test(line)){//ignore common or space
			line = line.replace(/\s+$/,'');
			if(/^['"]|^\/.+\/$/.test(line)){//regexp,string can't lead the regexp
				break;
			}else if(/\b(?:new|instanceof|typeof)$/.test(line)){//operator can lead regexp value
				return findExpSource(source);
			}else if(/(?:[)\]}]|[\w_]|--|\+\+)$/.test(line)){//value-token, postfix-operator-token,++/-- can't lead the regexp;
				break;
			}else{
				return findExpSource(source);
			}
		}
	}
}

function findExpSource(text){
	var depth=0,c,start = 1;
	while(c = text.charAt(start++)){
		if(c =='\n' || c == '\r'){
			//console.log('line end for reg search!')
			return;
		}
	    if(c=='['){
	    	depth = 1;
	    }else if(c==']'){
	    	depth = 0;
	    }else if (c == '\\') {
	        start++;
	    }else if(depth == 0 && c == '/'){
	    	outer:
	    	while(c = text.charAt(start++)){
	    		switch(c){
	    			case 'g':
	    			case 'i':
	    			case 'm':
	    			break;
	    			default:
	    			if(/\w/.test(c)){
	    				//console.log('invalid regexp flag:'+c)
	    				return null;
	    			}else{
	    				break outer;
	    			}
	    		}
	    	}
	    	//console.error(text.substring(0,start-1)+'@',text)
	    	text = text.substring(0,start-1);
	    	//console.log(text)
	    	return text;
	    	
	    }
	}
	//console.log('file end for reg search!')
}
if(typeof require == 'function'){
exports.formatJS = formatJS;
exports.partitionJavaScript=partitionJavaScript;
exports.compressJS=compressJS;
}