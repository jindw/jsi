var require;
var $JSI = function(cachedMap){//path=>[impl,dependences...],//只在define中初始化。存在(包括空数组)说明当前script已在装载中，不空说明已经装载完成，depengdences为空，说明依赖已经装载。
	var exportMap = {}//path=>exports// 存在说明已经载入【并初始化】
	var taskMap = {};//path=>[task...]
	var notifyMap = {};//dep=>[waitingList]
	var loading = 0;
	var async;//is async load model?
	var script = document.scripts[document.scripts.length-1];
	var scriptBase = script.src.replace(/[^\/]+$/,'');	
	var asyncInterval;
	var asyncWaitList = [];
	var syncWaitList = [];
	var syncWaitInc = 0;
	function addWait(path,callback,async){
		if(async){
			asyncWaitList.push(arguments);
			asyncInterval = asyncInterval || setInterval(asyncWait,300)
		}else{
			if(syncWaitList.push(arguments)<2){
				document.write('<script src="'+scriptBase+'block.js"><\/script>')
			}
		}
	}
	function asyncWait(args){
		if(loading == 0){
			clearInterval(asyncInterval);
			while(args = asyncWaitList.pop()){
				_load.apply(this,args)
			}
		}
	}
	/**
	 * @param path
	 * @param target||callback (optional)
	 * @param nextTagLazySync (optional)
	 */
	function load(path){
		var end = arguments.length-2;
		if(end>1){
			while(typeof arguments[end] == 'string'){end++};
		}else{
			end = 1;
		}
		var target = arguments[end];
		var callback =  function(result){
			copy(result,target||this);
		}
		if(typeof target == 'boolean'){
			var async = !target
			target = this;
		}else{
			var async = !arguments[end+1];
			if('function' == typeof target){
				callback = target;
			}
		}
		if(end>1){
			var i = 0;
			var all = {};
			var end2 = end;
			while(i<end){
				_load(arguments[i++],function(result){
					copy(result,all)
					--end2 || callback(all);
				},async);
			}
		}else{
			_load(path,callback,async);
		}
	}
	function _load(path,callback,thisAsync){
		path = path.replace(/\\/g,'/')
		if(path in exportMap){
			return callback(exportMap[path])
		}
		var cached = cachedMap[path];
		if(cached){
			push(taskMap,path,callback)
			if(cached.length === 1){//only impl no dependence
				onComplete(path);
				async = thisAsync;
			}else{
				if(async !== thisAsync && loading){// assert(sync!=null)
					addWait(path,taskMap[path].pop(),thisAsync);
				}//else{fired by previous loading}
			}
		}else{
			if(loading==0 || async === thisAsync ){//if(sync===null) assert(inc ==0) 
				async = thisAsync;
				taskMap[path] = [callback];
				loadScript(path);
			}else{
				addWait(path,callback,thisAsync);
			}
		}
	}
	function loadScript(path){//call by _load and onDefine
		//console.assert(cachedMap[path] == null,'redefine error')
		loading++;
		cachedMap[path] = [];//已经开始装载了，但是还没有值
		path = $JSI.realpath(path);//.replace(/[^\/]+$/,uar));
		if(async){
			var s = document.createElement('script');
			s.setAttribute('src',path);
			script.parentNode.appendChild(s);
		}else{
			document.write('<script src="'+path+'"><\/script>');
		}
	}
	/**
	 * @arguments implMap
	 * 		允许外部调用，缓存装载单元（该调用方式下不触发计数器）。
	 * @arguments path,dependences,impl
	 * 		添加缓存,计数器的因素，只能通过 loadScript 触发，禁止外部调用。
	 */
	function define(path,dependences,impl){
		if(impl){
			var implAndDependence = cachedMap[path];
			var i = dependences.length;
			var newScripts = [];
			//console.assert(implAndDependence.length==0,'redefine error')}
			implAndDependence.push(impl);
			while(i--){
				var dep = normalizeModule(dependences[i],path);
				var depCache = cachedMap[dep];;
				//>1:self loaded but dependence not loaded
				//=1:self and dependence loaded
				//=0:script added but not load
				//=undefined: not added
				if(depCache){
					if(depCache.length==1){
						continue;
					}
				}else{
					newScripts.push(dep)
				}
				push(notifyMap,dep,path)
				implAndDependence.push(dep);
			}
			if(implAndDependence.length == 1){
				onComplete(path);
			}else{
				while(dep = newScripts.pop()){
					loadScript(dep);
				}
			}
			if(--loading<1){
				onComplete()
			}
			//else{//loaded before}
		}else{
			for(i in path){
				cachedMap[i] = cachedMap[i] || path[i]
			}
		}
	}

	function onComplete(path){//逻辑上不应该被多次调用【除非有bug】
		if(path){
			var task = taskMap[path];
			if(task && task.length){
				var result = _require(path);
				var item;
				while(item = task.pop()){//每个task只能被调用一次！！！
					item.call(this,result)
				}
			}
			var targets = notifyMap[path]
			if(targets){
				var i = targets.length;
				while(i--){
					var target = cachedMap[targets[i]];
					var j = target.length;
					//if(j){}//没必要了，j必然>=1
					while(--j){
						if(target[j] === path){
							target.splice(j,1)
						}
					}
					if(target.length == 1){
						//console.info('immediate trigger:',targets[i])
						onComplete(targets[i])
					}
				}
			}
			//console.info('trigger:',path)
		}else{
			for(path in taskMap){
				//if(!exportMap[path]){console.info('complete trigger:',path);	}
				onComplete(path)
			}
		}
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
	//utils...
	function copy(src,dest){
		for(var p in src){
			dest[p] = src[p]
		}
	}
	function push(map,key,value){
		if(key in map){
			map[key].push(value)
		}else{
			map[key] = [value]
		}
	}
	function normalizeModule(url,base){
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
		/**
		 * @param path...
		 * @param callback
		 * @param block
		 */
		load : load,
		block : function(current){
			if(loading == 0){
				while(current = syncWaitList.pop()){
					_load.apply(this,current)
				}
			}else{
				current = document.scripts[document.scripts.length-1];
				//console.log(current.src,this._last - (this._last = +new Date()))
				document.write('<script src="'+current.src.replace(/\?token=.*$|$/,'?token='+ +new Date)+'&inc='+ ++syncWaitInc+'"><\/script>');
				//current.parentNode.removeChild(current);
			}
			//notify sync task 
		},
		define : define			// $JSI.define('path',['deps'],function(require,exports){...}) || $JSI.define({path:impl})
	}
}({});