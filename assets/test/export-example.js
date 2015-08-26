var test = function(){
	var dir = '';
	var impls = arguments;
	var cached = {};
	function internal_require(i,dest){
		if(typeof i=='number'){
			if(i in cached){return cached[i];}
			var id = dir+i;
			var module = {exports:cached[i] = {},id:id}
			impls[i](cached[i],internal_require,module,id);

			return cached[i] = module.exports;
		}else{
			return require(i);
		}
	}
	return internal_require(0);
}(
function(exports,require,module,__filename){
	var x = require(1).x;
	console.log(__filename,x)
},
function(exports,require,module){
	exports.x = 123;
}
)