var syntaxTestPostfix = '/*/\n\'\'""*/' ;
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
function buildDependence(id,text,deps){
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