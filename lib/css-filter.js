var FS = require('fs');
var CSSOM = require('cssom')
/**
var s = document.body.style
s.textIndent = 20;
s.text = body.parentNode.style.getAttribute('text');
alert(s.text)
alert(s.textIndent)
 */
function setupCSS(resourceManager, prefix){
	prefix = prefix.replace(/[\\\/]?$/,'/');
	var pseudo = /\:((?:hover|focus|active|(?:first|last)\-child)\b|nth\-child\((?:odd|even)\))/;  
	var pattern = new RegExp('^'+toRegSource(prefix)+'(.+)\.css$');
	var pseudoMap = {
		/**

0,1,textKashida,0pt
;0,1,textKashidaSpace,0pt
;0,1,layoutFlow,horizontal
;0,1,pageBreakAfter,auto
;0,1,pageBreakBefore,auto

;0,1,textJustify,auto
;0,1,tableLayout,auto
;0,1,layoutGridChar,none
;0,1,layoutGridLine,none
;0,1,layoutGridMode,both
;0,1,layoutGridType,loose
*0,2,textUnderlinePosition auto
;0,1,scrollbarArrowColor,#000000
;0,1,scrollbarBaseColor,#000000
;0,1,scrollbarFaceColor,#ece9d8
;0,1,scrollbarHighlightColor,#ffffff
;0,1,scrollbarShadowColor,#aca899
;0,1,lineBreak,normal
;0,1,unicodeBidi,normal
;0,1,whiteSpace,normal
;0,1,wordBreak,normal
;0,1,wordSpacing,normal
;0,1,writingMode,lr-tb
		 
		 */
		first:['text-kashida','textKashida'],//last
		nth:['text-kashida-space','textKashidaSpace'],
		active:['layout-flow','layoutFlow'],
		hover:['page-break-before','pageBreakBefore'],
		focus:['page-break-after','pageBreakAfter']//zIndex, 
		
	}
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
		var rules = data.cssRules;
		var i = rules.length;
		while(i--){
			var rule = rules[i];
			var selector = rule.selectorText;
			if(selector &&  pseudo.test(selector)){
				var psname;  
				var buf = [];
				var selector2 = selector.replace(pseudo,function(a,ps,i){
					buf.push(selector.substr(0,i))
					console.log(buf)
					if(ps == 'first-child'){
						ps = 'first';
					}else if(ps == 'last-child'){
						ps = 'last';
					}else if(ps == 'nth-child(odd)'){
						ps = 'odd';
					}else if(ps == 'nth-child(even)'){
						ps = 'even';
					}
					psname = ps;
					return '_'+ps+'_';
				});
				if(psname == 'odd' || psname == 'even'){
					var props = pseudoMap.nth;
				}else{
					props = pseudoMap[psname == 'last'?'first':psname]
				}
				if(props){
					data.insertRule(selector2+'{'+rule.style.cssText+'}',i+1)
					selector2 = buf[0].replace(pseudo,'');
					data.insertRule(
						selector2+'{'+props[0]+
						':expression(require("csshelper").setup(this,"'+psname+'","'+props[1]+'"))}',i+1)
				}
			}
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