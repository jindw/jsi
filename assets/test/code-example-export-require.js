~function(){
	var impls = arguments;
	var idIndex = [].pop.call(impls);
	var cached = {};
	var previous_require = this.require;
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
			return  exports;
		}else{
			return require(i) ;
		}
	}
	
	function external_require(path){
		var id = typeof path == 'number'?path:idIndex.indexOf(path);
		if(id>=0){
			return internal_require(id);
		}else{
			return external_require;
		}
	}
	if(previous_require && previous_require.backup){
		previous_require.backup.push(external_require)
	}else{
		this.require = function(pc){
			if(pc instanceof Function){
				var list = arguments;
				var i = list.length;
				var o = {};
				while(--i){
					copy(require(list[i]),o);
				}
				pc(o);
				return o;
			}else{
				var list = require.backup;
				var i = list.length;
				while(i--){
					var exports = list[i](pc);
					if(exports != list[i]){
						return exports
					}
				}
				return previous_require?previous_require.apply(this,arguments):{}
			}
		}
		this.require.backup = [external_require];
	}
	
	function copy(src,dest){
		for(var n in src){
			dest[n] = src[n];
		}
	}
}