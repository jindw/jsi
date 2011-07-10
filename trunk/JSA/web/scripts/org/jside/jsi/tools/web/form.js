var DEFAULT_VALUE = "d:default-value";
var TYPE = "d:data-type";
var REQUIRED = "d:required";
var MIX_VALUE = "d:min-value";
var MAX_VALUE = "d:max-value";
var MIX_LENGTH = "d:min-length";
var MAX_LENGTH = "d:max-length";
var ERROR = "d:error";

var MAX_SHORT = 0x7fff;
var MAX_INT =0x7fffffff;
var MAX_LONG = 0x7fffffffffffffff;//!=9223372036854776000

function ValidateForm(){
}
function E(id){
	return document.getElementById(id)
}

ValidateForm.prototype = {
	decorate:function(){
		var form = E(this.id);
		var elements = form.elements;
		var len = elements.length;
		for(var i =0;i<len;i++){
			var e = elements[i];
			if(e.name){
			   doDecorate(e);
			}
		}
		form.submit = submit;
	}
}
function submit(){
	var form = E(this.id);
	var form2 = document.createElement("form");
	form2.action = form.action;
	form2.method = form.method;
	form2.target = form.target;
	var elements = form.elements;
	var len = elements.length;
	form.parentNode.insertBefore(form2,form);
	for(var i =0;i<len;i++){
	    var e = elements[i];
	    switch(e.tagName.toLowerCase()){
        case 'input':
        case 'select':
    	    if(e.name &&!e.disabled){
    	        switch(String(e.type).toLowerCase()){
    	        case 'checkbox':
    	        case 'radio':
    	            if(!e.checked){
    		            var ucv = e.getAttribute(DEFAULT_VALUE);
    		            if(ucv){
    		                e = e.cloneNode(true);
    		                e.value = ucv;
    		                e.checked = true;
    		                form2.appendChild(e);
    		            }
    		            break;
    	            }
    	        default:
    	          form2.appendChild(e.cloneNode(true));
    	        }
    	    }
        }
	}
	form2.submit();
	form2.parentNode.removeChild(form2);
    return false;
}
function doDecorate(e){
	initValidate(e);
}
function initValidate(e){
	var type = e.getAttribute(TYPE);
	var min = e.getAttribute(MIX_VALUE);
	var max = e.getAttribute(MAX_VALUE);
	var required = e.getAttribute(REQUIRED);
	var minlen = e.getAttribute(MIX_LENGTH);
	var maxlen = e.getAttribute(MAX_LENGTH);
	var error = e.getAttribute(ERROR)||'';
	if(type || min || max){
		//TODO:fix leak
		e.onblur = buildValidationListener(type,min,max,required,minlen,maxlen,error);
		e.onblur();
	}
}

function buildValidationListener(type,min,max,required,minlen,maxlen,msg){
	return function(){
		var text = this.value;
		var len = text.length;
		if(!text && !required){
			return;//passed
		}
		if(minlen!=null && minlen>len){
			showError(this,text,[msg,"长度不能小于:"+minlen],true);
		}else if(maxlen!=null && maxlen<len){
			showError(this,text,[msg,"长度不能大于:"+maxlen],true);
		}else{
			switch(type){
			case 'float':
			case 'double':
			    var value = parseFloat(text)
			    if(value != text){
			    	showError(this,text,[msg,"非有效小数"],true);
			    }
			    if(!checkNumberRange(value,min,max)){
			    	showError(this,value,[msg,"数值范围越界:"+value+"->["+min+','+max+"]"],true);
			    }
				break;
			case 'long':
			case 'int':
			case 'integer':
			case 'short':
			   	value = parseInt(text)
			    if(value != text){
			    	showError(this,text,[msg,"非有效整数"],true);
			    }
			    var rang = getRange(type,max,min)
			    if(!checkNumberRange(value,rang[0],rang[1])){
			    	showError(this,value,[msg,"数值范围越界:["+rang+"]"],true);
			    }
			    break;
			}
		}
	}
}
var checkTime = new Date();
function showError(e,value,msgs,check){
    var now =  new Date()
	confirm("验证出错,("+e.name+"="+value+"):\n"+msgs.join("\n"))
	if(check && now - checkTime>10){
        e.focus();
        e.select();
	}
	checkTime = new Date();
    return false;
}
function getRange(type,min,max){
	switch(type){
	case 'long':
		type= MAX_LONG;
		break;
	case 'int':
	case 'integer':
		type= MAX_INT;
		break;
	case 'short':
		type= MAX_SHORT;
		break;
	default:
	    type = null;
	}
	if(min==null && type){
		min = -1-type;
	}
	if(max == null && type){
		max = type;
	}
	return [min,max];
}
function checkNumberRange(value,min,max){
	return (min ==null|| value>=min) && (max == null || value<=max);
}