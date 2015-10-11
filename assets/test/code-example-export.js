~function(){
	var impls = arguments;
	var cached = {};
	function internal_require(i){
		if(typeof i=='number'){
			var exports = cached[i];
			if(!exports){
				cached[i] = exports = {};
				var id = __dirname+i;
				var module = {exports:exports,id:id}
				impls[i](cached[i],internal_require,module,id);
				cached[i] = exports = module.exports;
			}
			
			return exports;
			
		}else{
			return this.require ? require(i) : {}
		}
	}
	function copy(src,o){
		for(i in src){
			o[i] = src[i];
		}
	}
}