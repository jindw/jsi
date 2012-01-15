var $export;
var $JSI = function(cachedMap){//path=>[impl,dependences:{path=>deps}],//只在define中初始化。存在(包括空数组)说明当前script已在装载中，不空说明已经装载完成，depengdenceMap为空，说明依赖已经装载。
	var exportMap = {}//path=>exports// 存在说明已经载入【并初始化】
	var notifyMap = {};//path=>[waitingPathMap:{path=>1}]
	var taskMap = {};//path=>[task...]
	var async;//is async load model?
	function require(path){
		if(path in exportMap){
			return exportMap[path];
		}else{
			var requireCache = {};
			var result = exportMap[path] = {}
			console.warn(path)
			cachedMap[path][0].call(this,function(path2){
				if(path2 in requireCache){
					return requireCache[path2];
				}
				return requireCache[path2] = require(normalizeModule(path2,path));
			},result);
			return result;
		}
	}
	$export = function (path,target){
		async = typeof target == 'function';
		var callback = async ?target : function(result){
			copy(result,target ||this);
		};
		if(typeof path  == 'string'){
			_export(path,callback,async);
		}else{//list
			var i = 0;
			var end = path.length;
			var end2 = end;
			var all = {};
			while(i<end){
				_export(path[i++],function(result){
					copy(result,all)
					--end2 || callback(all);
				},async);
			}
		}
	}
	function _export(path,callback,async){
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
			for(var dep in cached[1]){
				return;//task 会在 dependence 装载后自动唤醒。
			}
			onComplete(path,async);
		}else{
			load(path,async);
		}
	}

	function load(path){
		cachedMap[path] = [];//已经开始装载了，但是还没有值
		path = $JSI.realpath(path);
		if(async){
			var s = document.createElement('script');
			s.setAttribute('src',path);
			document.scripts[0].parentNode.appendElement(s);
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
				if(!cachedMap[dep]){
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
				load(dep);
			}
			onDefined(path)
		}
	}
	function onDefined(path){//只在define 原子块中被调用，重构时小心！！
		var notifySet = notifyMap[path];
		var dependenceMap = cachedMap[path][1];
		var dependenceCount=0;
		for(var p in dependenceMap){
			if(cachedMap[p]){
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
		var result = require(path);
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
	return {
		realpath:function(path){
			return '/scripts/'+path+'__define__.js';////scriptBase:/scripts/,
		},
		copy	: copy,
		require : require,
		define : define			// $JSI.define('path',['deps'],function(require,exports){...})
	}
}({});
