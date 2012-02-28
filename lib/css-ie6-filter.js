
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
var pseudoMap = {
	hover:['text-kashida','textKashida'], 
	focus:['layout-flow','layoutFlow'],
	active:['layout-flow','layoutFlow',true],
	first:['text-kashida-space','textKashidaSpace'],//['page-break-before','pageBreakBefore'],//last
	nth:['text-kashida-space','textKashidaSpace',true]
	
}
var pseudo = /\:((?:hover|focus|active|(?:first|last)\-child)\b|nth\-child\((?:odd|even)\))/;  

function optimizeForIE6(cssom){
	if(!cssom){
		return cssom;
	}
	var rules = cssom.cssRules;
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
				cssom.insertRule(selector2+'{'+rule.style.cssText+'}',i+1)
				selector2 = buf[0].replace(pseudo,'');
				cssom.insertRule(
					selector2+'{'+props[0]+
					':expression(require("csshelper").setup(this,"'+psname+'","'+props[1]+'"))'+(props[2]?'!important':'')+'}',i+1)
			}
		}
	}
	return cssom;
}
exports.optimizeForIE6 = optimizeForIE6;