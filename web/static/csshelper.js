var inc = 0;
var classPattern = /[\w\-_]*[^\s\-_](?=\s|$)/g
function setup(el,pseudo,property){
	//比如设置有效值才能完成覆盖。
	//el.style[property] = el.parentNode.currentStyle[property];	
	//*
	//el.runtimeStyle[property] = '';	
	
	
	var value = el.parentNode.currentStyle[property];
	el.style[property] = value;	
	
	document.title = "#"+el.tagName+(inc++)
	switch(pseudo){
	case 'focus':
		if(el.tagName != 'A'){
			init(el,'onfocus',onfocus);
			init(el,'onblur',onblur);
		}
		break;
	case 'hover':
		init(el,'onmouseenter',onmouseenter);
		init(el,'onmouseleave',onmouseleave);
		break;
	case 'active':
		if(el.tagName != 'A'){
			init(el,'onmousedown',onmousedown);
			init(el,'onmouseup',onmouseup);
			init(el,'onmouseout',onmouseup);
		}
		break;
	case 'first':
	case 'last':
		initFOE(el,1);
		break;
	case 'odd':
	case 'even':
		initFOE(el)
	}
	return '';
}


function init(el,type,fn){
	el.detachEvent(type,fn)
	el.attachEvent(type,fn)
}
function initFOE(el,first){
	var node = el.previousSibling;
	var className = el.className;
	if(first){
		className = className.replace(/\s*[\w\-_]+_(?:first|last)_(?=\s|_|$)/g,"");
		while(node){
			if(node.nodeType == 1){
				first = false;
				break;
			}
			node = node.previousSibling
		}
		if(first){
			el.className = className.replace(classPattern,"$& $&_first_");
		}
		var last = true
		node = el.nextSibling;
		while(node){
			if(node.nodeType == 1){
				last = false;
				break;
			}
			node = node.nextSibling
		}
		if(last){
			el.className = className.replace(classPattern,"$& $&_last_");
		}
	}else{
		className = className.replace(/\s*[\w\-_]+_(?:odd|even)_(?=\s|_|$)/g,"");
		var n = 1;
		while(node){
			if(node.nodeType == 1){
				n++;
			}
			node = node.previousSibling
		}
		el.className = className.replace(classPattern,n%2?"$& $&_odd_":"$& $&_even_");
	}
}

function onfocus(){
	var el = event.srcElement;
	el.className = el.className.replace(classPattern,"$& $&_focus_");
}
function onblur(){
	var el = event.srcElement;
	el.className = el.className.replace(/\s*[\w\-_]+_focus_(?=\s|$)/g,"");
}
function onmousedown(){
	var el = event.srcElement;
	el.className = el.className.replace(classPattern,"$& $&_active_");
}
function onmouseup(){
	var el = event.srcElement;
	el.className = el.className.replace(/\s*[\w\-_]+_active_(?=\s|$)/g,"");
}

function onmouseenter(){
	var el = event.srcElement;
	el.className = el.className.replace(classPattern,"$& $&_hover_");
}
function onmouseleave(){
	var el = event.srcElement;
	el.className = el.className.replace(/\s*[\w\-_]+_hover_(?=\s|$)/g,"");
	output.value="";output.value=document.body.innerHTML
}


function css(url){
	var l = document.createElement('link');
	l.setAttribute( 'rel',"stylesheet");
	l.setAttribute('type',"text/css");
	l.setAttribute('href',$JSI.realpath(url).replace(/__define__.js$/,'.css'));
	document.scripts[0].parentNode.appendChild(l);
}
exports.css = css;
exports.setup = setup;