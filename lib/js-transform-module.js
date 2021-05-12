partitionJavaScript(source,file,root)
var partitionJavaScript =require('./js-token').partitionJavaScript0
var TYPE_COMMENT=0, TYPE_STRING =1, TYPE_REGEXP=2;

var TYPE_SOURCE = 3;
var TYPE_NAME = 4;
var TYPE_OP = 5;

var TYPE_SOURCE_VAR = 4;
var TYPE_SOURCE_ATTR = 5;
var TYPE_SOURCE_OP =6;
var TYPE_SOURCE_KEYWORD =6;
var TYPE_SOURCE_SCOPE_START =6;
var TYPE_SOURCE_SCOPE_END =6;


function transform(source){
	var parts = partitionJavaScript(source);
	var result = [];
	var scopeDepth = 0;
	var parseDepth = 0;
	function append(code,type){
		result.push([code,type,scopeDepth,parseDepth])
	}
	for(var i=0;i<parts.length;i++){
		var part = parts[i];
		if(part.charAt()){
			case '/':
				switch(part.charAt(1)){
					case '*':
					case '/':
						append(part,TYPE_COMMENT);
						break;
					default:
						append(part,TYPE_REGEXP);

				}
			case '`':
				
			case '\'':
			case '"':

		}
	}
	
}