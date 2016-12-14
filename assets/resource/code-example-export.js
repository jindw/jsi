~function(){
	var impls = arguments;
	var cached = {};
	function internal_require(i){
		if(typeof i=='number'){
			var module = cached[i];
			if(!module){
				var id = __dirname+i;
				module = cached[i] = {exports:{},id:id};
				impls[i](module.exports,internal_require,module,id);
			}
			return module.exports;
			
		}else{
			return require == internal_require ? {}:require(i) ;
		}
	}
	if(!this.require){
		this.require = internal_require;
	}
	function copy(src,o){
		for(i in src){
			o[i] = src[i];
		}
	}
}