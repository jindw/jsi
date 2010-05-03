/**
 * @jsiparser org.xidea.jsi.parse
 * @export 
 * @return [[objectNames, beforeLoadDependences, afterLoadDependences]]
 */
function parse(pkgName,scriptPath,resourceLoader){
	if(scriptPath.indexOf('*') >=0 ){
		var result = [];
		var temp = resourceLoader($JSI.scriptBase + "?service=list&path="+pkgName.replace(/\./g,'/'));
		var list = parseJSON(temp);
		var i = list.length;
		while(i--){
			var temp = list[i];
			if(temp instanceof Array){
				result.push(temp);
			}else if('__package__.js'!=temp){
				result.push([temp]);
			}
		}
		return result;
	}else{
		var source = resourceLoader($JSI.scriptBase+pkgName.replace(/\.|$/g,'/')+scriptPath);
		var exp = /^\s*\/\*[\s\S]+?\*\//gm;
		var match;
		var result = [];
		while(match = exp.exec(source)){
			var result1 = parseEntry(source,match.index ,exp.lastIndex);
			if(result1){
				result.push([scriptPath].concat(result1));
			}
		}
		return result;
	}
}


function parseJSON(){
	return window.eval('('+arguments[0]+')');
}
function parseEntry(source,start,end){
	var doc = source.substring(start,end);
	var exp = /\s*\*\s*@([\w\:]+)[ \t]*(.*)/g
	var match;
	var doclets = {}
	while(match = exp.exec(doc)){
		var key = match[1];
		var value = match[2].replace(/^\s+|\s+$/g,'');
		var list = doclets[key] || (doclets[key] = []);
		list.push(value);
	}
	
	var jsiparser = doclets.jsiparser && doclets.jsiparser[0];
	if(jsiparser === '' || jsiparser  == 'org.xidea.jsi.parse' || jsiparser == 'org.xidea.jsi:parse'){
		var exports = doclets['export'];
		var dependenceBefore = [].concat(
			doclets['import']||[],doclets['require']||[]);
		var dependenceAfter = [].concat(
			doclets['import:after']||[],doclets['require:after']||[]);
		if(exports){
			var i = exports.length;
			while(i--){
				var item = exports[i];
				if(!item ){
					var id = source.substring(end);
					id = id.match(/^\s*(?:var\s|function\s)?([\w\.\$\s]+)/)[1];
					exports[i] = id.replace(/^\s+|\s+$/g,'');
				}
			}
		}else{
			exports = [];
		}
		return [exports,dependenceBefore,dependenceAfter]
	}else if(jsiparser){
		return $import(jsiparser).apply(this,arguments);
	}
}



