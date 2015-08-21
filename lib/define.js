try{
	var jstoken = require('./js-token');
	var lite = require('lite');
}catch(e){}
function buildDefine(id,text){
	text = text.replace(/\r\n?/g,'\n');
	var result = ["$JSI.define('",id,"',["];
	var deps = [];
	text  = buildDependence(id,text,deps)
	if(deps.length){
		result.push('"',deps.join('","'),'"')
	}
	
	result.push('],function(exports,require,module,__filename,__dirname){',text,'\n});');
	return result.join('');
}

function buildDependenceAndXML(id,text,deps){
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
					deps.push(item.replace(/['"]|(\\\/?)/g,'$1'));
				}else{//xml
					var j = i;
					while(/^\s*[+'"]/.test(tokens[++j]) && j<tokens.length);
					if(/^\s*\)/.test(tokens[j])){
						item = tokens.splice(i,j-i).join('');
						var match = prev.match(/(?:(\([\w\s,]*\))\{\s*(?:return\s*)?)?new\s+XML\s*\(\s*$/);
						prev = prev.slice(0,prev.length-match[0].length);
						if(match[1]){//has params
							var args = match[1].replace(/[\s()]/g,'').split(',');
							console.log(item)
							console.log(match)
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
function buildDependence(id,text,deps){
	if(lite){
		return buildDependenceAndXML(id,text,deps);
	}
	var sourceToken = [];
//	var targetTokens = [];
	var end=0;
	var test = text.replace(/\brequire\(\s*([^)\s]+)\s*\)/g,function(a,dep,start){
		if(dep == (dep = dep.replace(/^(['"])([^'"]+)\1$/,'$2'))){
			return a
		}
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
	//deps.sort();//TODO:灵异现象
	//console.log(deps)
	var end = deps.length-1;
	while(end>0){
		var dep = deps[end];
		var i = end--;
		while(i--){
			if(dep == deps[i]){
				deps.splice(i,1)
				break;
			}
		}
		
	}
	return test;
}
exports.buildDefine = buildDefine;