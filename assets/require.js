var $JSI,require; 
~function(cachedMap){//path=>[impl,dependences...],//只在define中初始化。存在(包括空数组)说明当前script已在装载中，不空说明已经装载完成，depengdences为空，说明依赖已经装载。
	var script = document.scripts[document.scripts.length-1];
	var scriptBase = script.src.replace(/[^\/]+(?:[#?].*)?$/,'');	
	var bootSources = (script.text || script.textContent ||'' ).match(/^\s*([\s\S]*?\$JSI\s*\.\s*init\([^()]*\))?((?:'[^']*'|"[^"]*"|[^'"]+)*)$/);
	var exportMap = {}//path=>exports// 存在说明已经载入【并初始化】
	var taskMap = {};//path=>[task...]
	
	var loading = 0;//load task list size
	var notifyMap = {};//dep=>[waitingList]
	var globalAsync;//is async load model?

	var asyncInterval;//async monitor 
	var asyncWaitList = [];//async task list
	
	var syncBlockList = [];//block task list,remove?
	
	var moduleMap = {};//module=>version
	
	function realpath(scriptBase,path){
		if(moduleMap && path in moduleMap){
			return scriptBase + 'o/'+path+'/'+moduleMap[path]+'/'+ +/\bJSI_DEBUG=true\b/.test(document.cookie)+'.js';
		}
		return scriptBase+path.replace(/.*(?:[^c]..|c[^s].|cc[^s])$/,'$&__define__.js');
	}
	$JSI = {
		init:function(config){
			config = config||'main'
			if(typeof config == 'string'){
				if(!/\.js$|\//.test(config)){ config = scriptBase+'config/'+config+'.js' }
				write('<script src="'+config+'"></script>');
			}else{
				$JSI.init = console.error;//no not init muti times
				copy(config , moduleMap);
				write(bootSources[2].replace(/\s*(\S[\s\S]*)/,'<script>$&</script>'));
				bootSources[2] = '';
			}
		},
		define: define,
		require: function(callback){
			var rtv = {};
			var async =  (callback instanceof Function);
			var args = asyncWaitList.splice.call(arguments,+async);
			var count = args.length;
			var end = count;
			var i = -1;
			while(++i<end){//exit: i == end
				_load(dec, async ,args[i]);
			}
			function dec(result){
				if(--count){
					copy(result,rtv);
				}else{
					while(end--){
						args[end] = _require(args[end]);
					}
					callback.apply(this,args)
				}
			}
			return rtv;
		},
		block: function(current){
			if(loading == 0){
				while(current = syncBlockList.pop()){_load.apply(this,current);}
			}else{
				write('<script src="'+scriptBase+'block.js?t='+  +new Date+'"><\/script>');
			}
		}
	};
	require = function(path){
		if(arguments.length>1){console.info('redirect to $JSI.require!!');return $JSI.require.apply($JSI,arguments)}
		var rtv = function(){return rtv.apply(this,arguments)};
		_load(function(result){
			copy(result,rtv);
			rtv.prototype = result.prototype;
			rtv = result;
		},false , path);
		return rtv;
	}
	//first init in require.js
	this.eval(bootSources[1]||"$JSI.init()");
	
	/* implements function define */
	function _require(path){
		if(path in exportMap){
			return exportMap[path];
		}else{
			var requireCache = {};
			var exports = exportMap[path] = {}
			var module = {exports:exports,id:path}
			var url = realpath(scriptBase,path);
			//try{
				cachedMap[path][0](exports,function(path2){
					if(path2 in requireCache){
						return requireCache[path2];
					}
					return requireCache[path2] = _require(normalizeModule(path2,path));
				},module,url);
			//}catch(e){//console error for debug:
			//	error('require error:'+path,e)
			//}
			return exportMap[path] = module.exports;
		}
		
	}
	function _load(callback,thisAsync,path){
		path = path.replace(/\\/g,'/')
		if(path in exportMap){
			return callback(exportMap[path])
		}
		var cached = cachedMap[path];
		if(path.match(/\.css(?:[#?].*)?$/)){
			if(!cached){
				if(thisAsync){
					cached = document.createElement('link');
					copy({rel:'stylesheet',type:'text/css',href:realpath(scriptBase,path)+'#test'},cached)
					script.parentNode.appendChild(cached);
				}else{
					write('<link rel="stylesheet" href="'+realpath(scriptBase,path)+'" type="text/css"> ')
				}
				cached = cachedMap[path] = Function.prototype
			}
		}
		
		if(cached){
			push(taskMap,path,callback)
			if(cached.length === 1){//only impl no dependence
				onComplete(path);
				globalAsync = thisAsync;
			}else{
				if(globalAsync !== thisAsync && loading){// assert(sync!=null)
					addWait(taskMap[path].pop(),thisAsync,path);
				}//else{fired by previous loading}
			}
		}else{
			if(loading==0 || globalAsync === thisAsync ){//if(sync===null) assert(inc ==0) 
				globalAsync = thisAsync;
				taskMap[path] = [callback];
				loadScript(path);
			}else{
				addWait(callback,thisAsync,path);
			}
		}
	}
	function loadScript(path){//call by _load and onDefine
		loading++;
		cachedMap[path] = [];//已经开始装载了，但是还没有值
		path = realpath(scriptBase,path);//.replace(/[^\/]+$/,uar));
		if(globalAsync){
			var s = document.createElement('script');
			s.setAttribute('src',path);
			script.parentNode.appendChild(s);
		}else{
			write('<script src="'+path+'"><\/script>');
		}
	}
	
	function addWait(callback,async,path){
		if(async){
			asyncWaitList.push(arguments);
			asyncInterval = asyncInterval || setInterval(intervalWait,300)
		}else{
			if(syncBlockList.push(arguments)<2){
				write('<script src="'+scriptBase+'block.js"><\/script>')
			}
		}
	}
	function intervalWait(args){
		if(loading == 0){
			clearInterval(asyncInterval);
			while(args = asyncWaitList.pop()){
				_load.apply(this,args)
			}
		}
	}
	/**
	 * 添加缓存,计数器的因素，只能通过 loadScript 触发，禁止外部调用。
	 * $JSI.define('path',['deps'],function(exports,require,module,__filename,__dirname){...}) 
	 * 允许外部调用，缓存装载单元（该调用方式下不触发计数器）。需要确保implMap 形成闭包（不能有不在集合中的依赖库）
	 * $JSI.define({path:impl})
	 */
	function define(path,dependences,impl){
		if(impl){
			var implAndDependence = cachedMap[path];
			var i = dependences.length;
			var newScripts = [];
			var impls = newScripts.slice.call(arguments,2);
			if(impls.length>1){
				impl = function(exports,require,module){
					var cached = {};
					function internal_require(i,o){
						if(typeof i=='number'){
							if(i in cached){return cached[i];}
							var id = path+'/'+i;
							var module = {exports:cached[i] = o||{},id:id}
							impls[i](cached[i],internal_require,module,id);
				
							return cached[i] = module.exports;
						}else{
							return require(i);
						}
					}
					
					internal_require(0,exports);
				}
			}
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
			}//else{//loaded before}
		}else{
			for(i in path){
				cachedMap[i] = cachedMap[i] || path[i]
			}
		}
	}

	function onComplete(path){//逻辑上不应该被多次调用【除非有bug】
		if(path){
			var task = taskMap[path];
			var targets = notifyMap[path];
			if(task && task.length){
				var i, target = _require(path);
				while(i = task.pop()){//每个task只能被调用一次！！！
					i.call(this,target)
				}
			}
			if(targets){
				i = targets.length;
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
	
	/* utils... */
	function error(title,e){
		var buf = [],ss = document.scripts,i=ss.length;
		while(i--){
			buf.push(String(ss[i].src).replace(/.*\//,' '));
		}
		buf.push('\n');
		for(i in cachedMap){
			buf.push(i.replace(/.*\//,' '+!!cachedMap[i][0]+':'))
		}
		console.error(title,e,buf)
	}
	function write(h){
		/\S/.test(h) && document.write(h);
	}
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
}({});