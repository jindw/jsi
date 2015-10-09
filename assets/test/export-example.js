var test = function(){
	var impls = arguments;
	var cached = {};
	function internal_require(i,o){
		if(typeof i=='number'){
			var exports = cached[i];
			if(!exports){
				cached[i] = exports = {};
				var id = __dirname+i;
				var module = {exports:exports,id:id}
				impls[i](cached[i],internal_require,module,id);
				cached[i] = exports = module.exports;
			}
			if(o){
				for(i in exports){
					o[i] = exports[i];
				}
			}
			return o || exports;
			
		}else{
			return this.require ? require(i) : {}
		}
	}
	
	return internal_require(0);
}(
function(exports,require,module,__filename){
	var m = require(1)
	console.log(m.x+123)
},
function(exports,require,module){
	exports.x = 123;
}
)