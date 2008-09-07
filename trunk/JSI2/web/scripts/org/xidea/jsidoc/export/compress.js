function CompressForm(form,submit){
	 form.size.onblur=sizeValidateListener;
	 form.ratio.onblur=ratioValidateListener;
	 bindAvaliable(form,['syntaxCompression','trimBracket']);
	 bindAvaliable(form,['textCompression','compatible','size',"ratio"]);
	 updateSource(form.ownerDocument,form.source.value)
}
var keywords = {"for":true,"while":true,"if":true,"function":true}
CompressForm.prototype.updateSource = function(ownerDocument,value){
	var debugCallRegExp = /(\bfunction\s+[\w\$\s]+)|([\w\$\s\.]+)\(/g;
	var debugCallMap = {};
	var match;
	while(match = debugCallRegExp.exec(value)){
		if(!match[1]){
			match = match[2].replace(/\s*\.\s*/g,'.').replace(/^[\s\S]*\s+([\$\w])/,'$1');
			if(/^[\w\$]+(?:[\w\$\.]+)+$/.test(match) && keywords[match]!=true){
			    debugCallMap[match] = debugCallMap[match]? debugCallMap[match]+1: 1;
			}
		}
	}
	var featureRegExp = /\bif\s*\(\s*!*\s*['"]([\w\$\.\:]+)['"]\s*\)/g;
	var featureMap = {};
	while(match = featureRegExp.exec(value)){
		featureMap[matownerDocumentch[1]] = featureMap[match[1]]?featureMap[match[1]]+1 : 1;
	}
	updateList(ownerDocument.getElementById("debugCallList"),debugCallMap);
	updateList(ownerDocument.getElementById("featrueList"),featureMap);
}
function updateList(container,keyMap){
	var checkedMap = getCheckedMap(container);
	var keys = keyMap instanceof Array?keyMap:getKeys(keyMap);
	var buf = [];
	for(var i=0;i<keys.length;i++){
		var key = keys[i];
		var id = container.id+"_" + key;
		buf.push("<input type='checkbox' id='"+id+"' "+(checkedMap[key]?"checked":"")+"/>");
		buf.push("<label for="+id+">"+key);
		if(keyMap[key]){
			buf.push("("+keyMap[key]+")");
		}
		buf.push("</label><br/>")
	}
	container.innerHTML = buf.join('\n');
}
function getCheckedMap(container){
	var checkedMap = {};
	var selected = container.getElementsByTagName("input");
	var start = container.id.length+1;
	var i= selected.length
	while(i--){
		checkedMap[selected[i].id.substr(start)] = selected[i].checked;
	}
	return checkedMap;
}
var debugCalls = ",$log.trace,$log.debug,alert,console.log,prompt,confirm,"
function getKeys(value){
	var result = [];
	for(var k in value){
		result.push(k);
	}
	result.sort(function(a,b){
		var off = debugCalls.indexOf(b)-debugCalls.indexOf(a);
		if(off){
			return off;
		}
		return a>b?1:a==b?0:-1;
	});
	return result;
}
function bindAvaliable(form,group){
	form[group.shift()].onclick=function(){
		var disabled = !this.checked;
		var i = group.length;
		while(i--){
			form[group[i]].disabled = disabled;
		}
	}
}

function sizeValidateListener(){
	return checkField(this,/^\d+$/,'size must be a int value');
}
function ratioValidateListener(){
	return checkField(this,/^\d+(\.\d+)?$/,'ratio must be a float value')
}

function checkField(input,pattern,msg){
	if(pattern.test(input.value)){
	    return true;
	}else{
	    alert(msg)
	    input.focus();
	    input.select();
	    return false;
	}
}