var Compressor = {
	connect:connect
}

var DEBUG_CALL_INDEX = ",$log.trace,$log.debug,alert,console.log,prompt,confirm,"
function connect(form){
	var bindKey1 = 'config.syntaxCompression';
	var bindKey2 = 'config.textCompression';
	var bindFn1 = bindAvaliable(form,bindKey1,['config.trimBracket','internalPrefix']);
	var bindFn2 = bindAvaliable(form,bindKey2,['config.compatible','config.sizeCondition',"config.ratioCondition"]);
	var value = '';
	var updateInterval = window.setInterval(function(){
	 	if(form.source.value!=value){
	 		value = form.source.value;
	 		var error;
	 		try{
	 			new Function(value);
	 		    updateSource(value);
	 			error = '';
	 		}catch(e){
	 			error = e.message;
	 		}
	 		E("error").innerHTML = error;
	 		var compress = E("compress");
	 		var analyse = E("analyse");
	 		analyse.disabled = compress.disabled = !!error || !value;
	 		compress.onclick = compressListener;
	 		analyse.onclick = analyseListener;
	 	}
	},1000)
	function resetListener(){
	 	bindFn1.apply(form[bindKey1]);
	 	bindFn2.apply(form[bindKey2]);
	}
	E("reset").onclick = function(){
		setTimeout(resetListener,1000);
	}
	setTimeout(resetListener,500);
	 
	var vf = new ValidateForm();
	vf.id = form.id;
	vf.decorate();
}
function analyseListener(){
	submit(this.form,"0");
}
function compressListener(){
	submit(this.form,"3");
}
function E(id){
	return document.getElementById(id)
}
function submit(form,type){
	var xmlContent = form.content;
	var data = ['<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">',
	    "<properties>"];
	var exportList = getKeys(getCheckedMap("internalList"),false);
	appendEntry(data,'default/__package__.js',"this.addScript('default.js',['"+exportList.join("','")+"'])");
	appendEntry(data,"default/default.js",form.source.value);
	data.push("</properties>");
	xmlContent.value = data.join('');
	var win = open('about:blank','result','top=220px,left=220px,width=680px,height=240px,scrollbars=yes');
	win.focus();
	form.level.value = type;
	form.source.disabled= true;
	//
	form.submit();
	setTimeout(function(){
		form.source.disabled= false;
	},1000)
}
function appendEntry(data,path,text){
	data.push("<entry key='",path,"'>") ;
    data.push(/[<>&]/.test(text) && text.indexOf(']]>')<0?
	        "<![CDATA["+text + ']]>'
	        :text.replace(/[<>&]/g,xmlReplacer));
    data.push("</entry>\n");
}
var keywords = {"for":true,"while":true,"if":true,"function":true}
function updateSource(value){
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
		featureMap[match[1]] = featureMap[match[1]]?featureMap[match[1]]+1 : 1;
	}
	updateList("debugCallList","config.debugCalls",debugCallMap);
	updateList("featureList","config.features",featureMap);
	updateList("internalList",null,findGlobals(value))
}
function updateList(containerId,inputName,keyMap){
	var container = E(containerId);
	var checkedMap = getCheckedMap(containerId);
	var keys = keyMap instanceof Array?keyMap:getKeys(keyMap);
	if(containerId == "debugCallList"){
		keys.sort(function(a,b){
			var off = DEBUG_CALL_INDEX.indexOf(b)-DEBUG_CALL_INDEX.indexOf(a);
			if(off){
				return off;
			}
			return a>b?1:a==b?0:-1;
		});
	}
	var buf = [];
	for(var i=0;i<keys.length;i++){
		var key = keys[i];
		var id = container.id+"_" + key;
		buf.push("<label><input type='checkbox'")
		if(inputName){
			buf.push(" name='",inputName,"'");
		}
		buf.push(" value='",key,"'");
		checkedMap[key] && buf.push("checked");
		buf.push("/>");
		buf.push(key);
		if(keyMap[key]){
			buf.push("("+keyMap[key]+")");
		}
		buf.push("</label><br/>")
	}
	container.innerHTML = buf.join('\n');
}
function getCheckedMap(containerId){
	var container = E(containerId);
	var checkedMap = {};
	var selected = container.getElementsByTagName("input");
	var start = containerId.length+1;
	var i= selected.length
	while(i--){
		checkedMap[selected[i].value] = selected[i].checked;
	}
	return checkedMap;
}
function getKeys(data,requiredValue){
	var result = [];
	for(var k in data){
		if(requiredValue == undefined || requiredValue == data[k]){
		    result.push(k);
		}
	}
	return result;
}
function bindAvaliable(form,key,group){
	return form[key].onclick=function(){
		var form = this.form;
		var node = this.type == 'hidden'? this.nextSibling:this;
		var disabled = !node.checked;
		var i = group.length;
		while(i--){
			try{
			    node = form[group[i]];
			    node.disabled = disabled;
			    if(node.type == 'hidden'){
			    	node = node.nextSibling;
			    	node.disabled = disabled;
			    }
			    
			}catch(e){
				alert([group,i,e])
			}
		}
	}
}
