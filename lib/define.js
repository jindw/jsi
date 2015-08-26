try{
	var jstoken = require('./js-token');
	var lite = require('lite');
}catch(e){}
function buildDefine(id,text,pathIndex){
	text = text.replace(/\r\n?/g,'\n');
	var deps = [];
	text  = buildDependence(id,text,deps,pathIndex)
	
	var map = {};
	var i = deps.length;
	while(i-->0){
		var p = normalizeModule(String(deps[i]),id);
		if(map[p] === true){
			deps.splice(i,1);
		}
		map[p] = true;
	}
	var result = ["$JSI.define('",id,"',["];
	if(deps.length){
		result.push('"',deps.join('","'),'"')
	}
	result.push('],function(exports,require');
	if(/\b__(?:file|dir)name\b/.test(text)){
		result.push(',module,__filename');
	}else if(/\bmodule\b/.test(text)){
		result.push(',module');
	}
	result.push('){');
	if(/\b__filename\b/.test(text)){
		result.push('var __dirname= __filename.replace(/[^\\\/]+$/,"");');
	}
	result.push(text,'\n});');
	return result.join('');
}

function buildDependenceAndXML(id,text,deps,pathIndex){
	var tokens = jstoken.partitionJavaScript(text);
	var result = [];
	for(var i =0;i<tokens.length;i++){
		var item = tokens[i];
		switch(item.charAt()){
		case '\'':
		case '\"':
			//console.log(item);
			var prev = tokens[i-1];
			var next = tokens[i+1];
			if(prev && /^\s*[\)\+]/.test(next)){
				//var tpl1 = new XML("<div></div>");
				//function tpl2(a,b){return new XML("<div style='display:${a}'>${b}</div>");}
				var match = prev.match(/\b(?:(require)|\bnew\s+XML)\s*\(\s*$/)
				if(match){
					if(match[1] == 'require'){
						
						if(item!= (item=item.replace(/^(['"])([\w\.\/]+)\1/,'$2'))){
							if(pathIndex && item.charAt(0) == '.'){
								//替换成数字
								item = normalizeModule(item,id)
								var index = pathIndex.indexOf(item);
								if(index>=0){
									tokens[i] = index;
									deps.push(index);
								}else{
									index = pathIndex.push(item)-1;
									tokens[i] = index;
									deps.push(index);
								}
							}else{
								deps.push(item);
							}
						}
						//console.log(item,deps)
					}else if(lite){//xml
						var j = i;
						while(/^\s*[+'"]/.test(tokens[++j]) && j<tokens.length);
						if(/^\s*\)/.test(tokens[j])){
							item = tokens.splice(i,j-i).join('');
							var match = prev.match(/(?:(\([\w\s,]*\))\{\s*(?:return\s*)?)?new\s+XML\s*\(\s*$/);
							prev = prev.slice(0,prev.length-match[0].length);
							if(match[1]){//has params
								var args = match[1].replace(/[\s()]/g,'').split(',');
								//console.log(item)
								//console.log(match)
								var fn = lite.parseLite(eval(item),args);
								tokens[i-1] = prev+match[1]+"{"+String(fn).replace(/^.*?\{|}\s*$/g,'')+"";
								tokens[i] = tokens[i].replace(/^\s*\)/,'')
								//console.log(tokens[i-1])
							}else{
								var fn = lite.parseLite(eval(item))
								tokens[i-1] = prev+String(fn).replace(/^\s+|\s+$/g,'');
								tokens[i] = tokens[i].replace(/^\s*\)/,'')
							}
						}
					}
				}
			}
		}
	}
	return tokens.join('');
}
function buildDependence(id,text,deps,pathIndex){
	if(lite || pathIndex){
		return buildDependenceAndXML(id,text,deps,pathIndex);
	}
	var sourceToken = [];
//	var targetTokens = [];
	var end=0;
	var test = text.replace(/\brequire\(\s*(['"])([\w\.\-]+)\1\s*\)/g,function(a,qute,dep,start){
		end = text.substring(end,start);
		sourceToken.push(end);
//		targetTokens.push(end);
		
		end = start+a.length;
		deps.push(dep);
		
		sourceToken.push(a);
//		targetTokens.push( a = 'require("'+dep+'")');//e,
		return a+syntaxTestPostfix;
	});
	//targetTokens.push(text.substr(end));
	sourceToken.push(text.substr(end));
	test = buildDefineFromTokens(test,deps,sourceToken);
	return test;
}
var syntaxTestPostfix = '/*/\n\'\'""*/' ;
function buildDefineFromTokens(test,deps,sourceTokens){
	try{
		new Function(test);
		test = sourceTokens.join('');
	}catch(e){
		//TODO,先对源码监察一边
		//console.log("Error",e)
		//var dest = sourceTokens.concat();
		var end = sourceTokens.length-2;
		var i=end;
		while(i>0){
			try{
				var token = sourceTokens[i];
				sourceTokens[i] += syntaxTestPostfix;
				new Function(sourceTokens.join(''))
			}catch(e){
				deps.splice((i-1)/2,1)
			}
			sourceTokens[i] = token;
			i-=2;
		}
		test = sourceTokens.join('');
	}
	return test;
}

	function normalizeModule(url,base){
        if(url.charAt(0) == '.'){
        	url = base.replace(/[^\/]+$/,'')+url
        	while(url != (url =url.replace( /[^\/]+\/\.\.\/|(\/)?\.\//,'$1')));
        }
        return url;
    }
exports.buildDefine = buildDefine;