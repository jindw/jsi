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
		var id = idIndex.indexOf(path);
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
					var result = require(list[i]);
					for(i in exports){
						o[i] = exports[i];
					}
				}
				pc(o);
				return o;
			}else{
				var list = require.backup;
				var i = list.length;
				while(i--){
					var result = list[i](pc);
					if(result != list[i]){
						return result
					}
				}
				return previous_require?previous_require.apply(this,arguments):{}
			}
		}
		this.require.backup = [external_require];
	}
}