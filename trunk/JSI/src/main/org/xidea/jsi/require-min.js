var $JSI = function(loaderMap){//path=>[exports,loader,dependences]//只在define中初始化。存在说明当前script已经装载，depengdenceMap为空，说明依赖已经装载。
	function require(path){
		var entry = loaderMap[path];
		var cache = {};
		if(!entry){
			//TODO:...
			entry = [,load(path)]
		}
		return entry[0] || entry[1].call(this,function(path2){
			if(path2 in cache){
				return cache[path2];
			}
			return cache[path2] = require(normalizeURI(path2,path));
		},entry[0]={}),entry[0];
	}
	function load(path){
		path = $JSI.realpath(path);
	}
	function copy(src,dest){
		for(var p in src){
			dest[p] = src[p]
		}
	}
	function normalizeURI(url,base){
        var url = url.replace(/\\/g,'/');
        if(url.charAt(0) == '.'){
        	url = base.replace(/[^\/]+$/,'')+url
        	while(url != (url =url.replace( /[^\/]+\/\.\.\/|(\/)?\.\//,'$1')));
        }
        return url;
    }
	return {
		realpath:function(path){
			return '/scripts/'+path+'__define__.js';////scriptBase:/scripts/,
		},
		copy	: copy,
		require : require,
		define : define			// $JSI.define('path',['deps'],function(require,exports){...})
	}
}({});
