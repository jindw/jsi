/**
 * @jsiparser org.xidea.jsi.parse
 * @import org.xidea.jsidoc.util.loadText
 * @export parse
 * 
 * @return [[objectNames, beforeLoadDependences, afterLoadDependences]]
 */
 
function parse(pkg,scriptPath){
	var source = loadText($JSI.scriptBase+pkg.replace(/\.|$/g,'/')+scriptPath);
	var exp = /^\s*\/\*[\s\S]+?\*\//gm;
	var match;
	var result = [];
	while(match = exp.exec(source)){
		var result1 = parseEntry(source,exp.lastIndex,exp.lastIndex+match[0].length);
		if(result1){
			result.push(result1);
		}
	}
}


function parseEntry(source,start,end){
	var doc = source.substring(start,end);
	var exp = /\s*\*\s*@(\w+)[ \t]*(.*)/g
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
		var imports = doclets['import'];
		var require = doclets['require'];//import别名
		if(exports){
			var i = exports.length;
			while(i--){
				var item = exports[i];
				if(!item ){
					var id = source.substring(end);
					id = /^\s*(?:var\s|function\s)?([\w\.\$\s]+)/.match(id)[1];
					exports[i] = id.replace(/^\s+|\s+$/g,'');
				}
			}
		}else{
			exports = [];
		}
		return [exports,[].concat(imports||[],require||[])]
	}else if(jsiparser){
		return $import(jsiparser).apply(this,arguments);
	}
}



