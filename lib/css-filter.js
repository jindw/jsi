var FS = require('fs');
var CSSOM = require('cssom');
var optimizeForIE6 = require("./css-ie6-filter.js").optimizeForIE6
/**
var s = document.body.style
s.textIndent = 20;
s.text = body.parentNode.style.getAttribute('text');
alert(s.text)
alert(s.textIndent)
 */
function setupCSS(resourceManager, prefix){
	prefix = prefix.replace(/[\\\/]?$/,'/');
	var pattern = new RegExp('^'+toRegSource(prefix)+'(.+)\.css$');
	resourceManager.addTextFilter(pattern,function(resource,text){
		var path = (resource.sourcePath || resource.path).substr(prefix.length).replace(/\.js$/,'');
		if(!text){
			try{
				var file = require.resolve(path);
			}catch(e){}
			if(file ){
				text = resource.getExternalAsBinary(file).toString('utf-8');
			}
		}
		console.log(JSON.stringify(text))
		return text;
	})
	resourceManager.addDOMBuilder(pattern,function(resource,data){
		if(data){
			data = CSSOM.parse(data);
			//bug fix for @import
			var rules = data.cssRules;
			var i = rules.length;
			while(i--){
				var rule = rules[i];
				if(rule.type == 3){//IMPORT_RULE  
					//TODO:
				}
			}
		}
		return data;
	});
	resourceManager.addDOMFilter(pattern,function(resource,data){
		if(!data){
			return data;
		}
		if(/^\-ie6-$/.test(resource.prefix)){
			return optimizeForIE6(data);
		}
		return data;
	})
	resourceManager.addSerializer(pattern,function(resource,data){
		return data && data.toString();
	});
}


function toRegSource(s,c){
	return c? '\\u'+(0x10000+c.charCodeAt()).toString(16).substr(1)
			: s.replace(/([^w_-])/g,toRegSource);
}
exports.setupCSS = setupCSS;