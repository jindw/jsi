function encodeHTML(text){
	return text.constructor == String ? text.replace(/&<>/g,function(t){
		return "&#"+t.charCodeAt()+';'
	}):text;
}
