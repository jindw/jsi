var require;
var $JSI = function(cachedMap){//path=>[impl,dependences:{path=>deps}],//只在define中初始化。存在(包括空数组)说明当前script已在装载中，不空说明已经装载完成，depengdenceMap为空，说明依赖已经装载。
	var exportMap = {}//path=>exports// 存在说明已经载入【并初始化】
	var notifyMap = {};//path=>[waitingPathMap:{path=>1}]
	var taskMap = {};//path=>[task...]
	var async;//is async load model?
	var script = document.scripts[document.scripts.length-1];
	var scriptBase = script.src.replace(/[^\/]+$/,'');	
	//Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11
	//Opera/9.80 (Windows NT 5.1; U; Edition IBIS; zh-cn) Presto/2.10.229 Version/11.60
	//Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; InfoPath.2)
	//Mozilla/5.0 (Windows NT 5.1; rv:10.0.2) Gecko/20100101 Firefox/10.0.2
	//webkit,moz,ie,o
	var uar = /^o(?=pera)|msie [6-8]|ms(?=ie \d+)|webkit|^moz(?=.+firefox)|khtml/.exec(navigator.userAgent.toLowerCase());
	if(uar){
		uar = '-'+uar[0].replace(/msie (\d)/,'ie$1')+'-$&';
	}else{
		uar = '-ie-$&';
	}
	function _require(path){
		try{
			if(path in exportMap){
				return exportMap[path];
			}else{
				var requireCache = {};
				var result = exportMap[path] = {}
				//console.warn(path)
				cachedMap[path][0].call(this,function(path2){
					if(path2 in requireCache){
						return requireCache[path2];
					}
					return requireCache[path2] = _require(normalizeModule(path2,path));
				},result);
				return result;
			}
		}catch(e){
			var buf = []
			var ss = document.scripts;
			for(var i=0;i<ss.length;i++){
				buf.push(ss[i].src);
			}
			buf.push('\n');
			for(var i in cachedMap){
				buf.push(i,!!cachedMap[i][0])
			}
			console.error('require error:',path,e.message,buf)
		}
	}
	function load(path,target,lazy){
		async = !lazy;
		function callback(result){
			if(typeof target == 'function'){
				target(result)
			}else{
				copy(result,target ||this);
			}
		};
		if(typeof path  == 'string'){
			_load(path,callback,async);
		}else{//list
			var i = 0;
			var end = path.length;
			var end2 = end;
			var all = {};
			while(i<end){
				_load(path[i++],function(result){
					copy(result,all)
					--end2 || callback(all);
				},async);
			}
		}
	}
	function _load(path,callback,async){
		if(path in exportMap){
			return callback(exportMap[path])
		}
		var task = taskMap[path];
		var cached = cachedMap[path];
		if(!task){
			task = taskMap[path] = [];
		}
		task.push(callback);
		if(cached){
			if(cached.length){
				for(var dep in cached[1]){
					return;//task 会在 dependence 装载后自动唤醒。
				}
				onComplete(path,async);
			}
			//else{fired by previous loading}
		}else{
			loadScript(path,async);
		}
	}

	function loadScript(path){
		cachedMap[path] = [];//已经开始装载了，但是还没有值
		path = $JSI.realpath(path.replace(/[^\/]+$/,uar));
		if(async){
			var s = document.createElement('script');
			s.setAttribute('src',path);
			script.parentNode.appendChild(s);
		}else{
			document.write('<script src="'+path+'"><\/script>');
		}
	}
	function define(path,dependences,impl){
		//if(path in cacheMap){return;} //异常，认为不会发生
		var dependenceMap = {};
		var loader = cachedMap[path];
		var len = dependences.length;
		if(!loader.length){//js执行机制需要确保以下行为原子性（js单线程模型确保这点，不会被中断插入其他js逻辑）
			loader.push(impl,dependenceMap);//dependenceMap 为空确保程序装载完成
			var list = [];
			while(len--){
				var dep = normalizeModule(dependences[len],path);
				loader = cachedMap[dep];//变量复用
				if(!(loader && loader.length)){//只要沒有裝載成功，就需要添加監聽，不能奢望別人監聽的及時性。
					var notifySet = notifyMap[dep];
					if(!notifySet){
						notifyMap[dep] =notifySet = {};
					}
					notifySet[path]=1;
					dependenceMap[dep] = 1;
					//console.info(path,dep)
					list.push(dep);
					
				}
			}
			while(dep = list.pop()){
				loadScript(dep);
			}
			onDefined(path)
		}
		//else{//loaded before}
	}
	function onDefined(path){//只在define 原子块中被调用，重构时小心！！
		var notifySet = notifyMap[path];
		var dependenceMap = cachedMap[path][1];
		var dependenceCount=0;
		for(var p in dependenceMap){
			if(cachedMap[p].length){
				delete dependenceMap[p];
			}else{
				dependenceCount++;
			}
		}
		outer:for(p in notifySet){//遍历所有依赖当前脚本的脚本
			var notifyDependenceMap = cachedMap[p][1];//找到依赖当前脚本的脚本的依赖【a】
			if(delete notifyDependenceMap[path]){//如果存在，删除（因为已经装）
				if(dependenceCount){
					copy(dependenceMap,notifyDependenceMap);	//并将未装载依赖移入
					_moveNodify(dependenceMap,p)
					//add nodify
				}else{
					for(p in notifyDependenceMap){ //如果还有其他未装载依赖， 跳过
						continue outer;
					}
					onComplete(p);//没有其他为装载的依赖， 完成
				}
			}
		}
		if(!dependenceCount){//直接就没有未装载依赖
			//notify
			onComplete(path);
		}
	}
	function _moveNodify(loadingMap,path){//这里关联的　notifySet　一定有值，因为曾经添加过　
		for(var p in loadingMap){
			var notifySet = notifyMap[p];
			notifySet[path] = 1;
		}
	}
	function onComplete(path){//逻辑上不应该被多次调用【除非有bug】
		var task = taskMap[path];
		var result = _require(path);
		if(task){
			var item;
			while(item = task.pop()){//每个task只能被调用一次！！！
				item.call(this,result)
			}
		}
	}
	
	//utils...
	function copy(src,dest){
		for(var p in src){
			dest[p] = src[p]
		}
	}
	function normalizeModule(url,base){
        var url = url.replace(/\\/g,'/');
        if(url.charAt(0) == '.'){
        	url = base.replace(/[^\/]+$/,'')+url
        	while(url != (url =url.replace( /[^\/]+\/\.\.\/|(\/)?\.\//,'$1')));
        }
        return url;
    }
	require = _require;

	return {
		realpath:function(path){
			return scriptBase+path+'__define__'+(this.hash[path]||'')+'.js';////scriptBase:/scripts/,
		},
		hash	: {},
		copy	: copy,
		load : load,
		define : define			// $JSI.define('path',['deps'],function(require,exports){...})
	}
}({});
