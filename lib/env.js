var VFS = require('./vfs');
var Crypto = require('crypto');
var Path = require('path');
/**
 * {
 * 	'prefix::postfix':{
 * 		cleanMap:{
 * 			'${path}' : []
 * 		}
 * 		resourceMap:{
 * 			'${path}' : {
 * 				relation:[relation1,..],
 * 				cache:[cache1,cache2...],
 * 				time:+new Date()
 * 			}
 * 		}
 * 		
 *  }
 * }
 */
var instanceMap = {};
var root = null;

var globalsFilters = [];

var BINARY_BUILDER = 0;
var TEXT_BUILDER = 1;
var TEXT_FILTER = 2;
var DOM_BUILDER = 3;
var DOM_FILTER = 4;
var SERIALIZER = 5;
var textBuilderStart = 0;
var textFilterStart = 0;
var domBuilderStart = 0;
var domFilterStart = 0;

var serializeFilter = [];
/**
 * instance
 * resource
 * prefix
 * postfix
 * index:-1
 */
var currentContext = null;
function _addFilter(pattern,impl,type){
	var item = [pattern,impl];
	switch(type){
		case BINARY_BUILDER:
			globalsFilters.splice(textBuilderStart++,0,item);
			item = null;
		case TEXT_BUILDER:
			item && globalsFilters.splice(textBuilderStart,0,item) ;
			item = null;
			textFilterStart++;
		case TEXT_FILTER:
			item && globalsFilters.splice(domBuilderStart,0,item) ;
			item = null;
			domBuilderStart++;
		case DOM_BUILDER:
			item && globalsFilters.splice(domFilterStart,0,item) ;
			item = null;
			domFilterStart++;
			break;
		case DOM_FILTER:
			globalsFilters.push(item)
			break;
		case SERIALIZER:
			serializeFilter.push(item)
	}
	
}
/**
 * 
 */
function _load(path,prefix,postfix,lastIndex){
	var instanceKey = [prefix,postfix].join('::');
	var instance = instanceMap[instanceKey] || (instanceMap[instanceKey] = {cleanMap:{},resourceMap:{}});
	var resource = instance.resourceMap[path] || 
			( instance.resourceMap[path] 
				= {path:path,
					cache:[],
					relation:[],
					time:+new Date(),
					prefix:prefix,
					postfix:postfix}) ;
	var cache = resource.cache;
	var old = currentContext;
	var data;
	if(lastIndex in cache){
		return resource;
	}
	try{
		currentContext = {
			instance : instance,
			resource:resource,
			index:-1
		}
		for(var i=0;i<=lastIndex;i++){
			if(cache[i]===undefined){
				var pf = globalsFilters[i];
				if(pf[0].test(path)){
					if(data === undefined){
						data = VFS.getDataAsBinary(resource.sourcePath || path,prefix,postfix);
					}
					if(i>=textFilterStart){
						data = _defaultTextFilter.call(resource,data);
					}
					if(i>=domFilterStart){
						data = _defaultDOMFilter.call(resource,data);
					}
					data = cache[i] = pf[1].call(resource,data);
				}else{
					cache[i] = data || null;
				}
			}else{
				data = cache[i];
			}
			currentContext.index = i;
		}
		return resource;
	}finally{
		currentContext = old;
	}
}

function _defaultDOMFilter(){
	return null;
}
function  _defaultTextFilter(buf){
	//if(Buffer.isBuffer(buf))
	return buf.toString();
}
function  _defaultSerializeFilter(data){
	if(data == null){
		return VFS.getDataAsBinary(this.path,this.prefix,this.postfix);
	}
	if(Buffer.isBuffer(data)){
		return data;
	}else{
		return new Buffer(data.toString());
	}
	
}
function _loadAll(args){
	if(args.length<3 && currentContext){
		if(args.length<2){
			args[1] = currentContext.prefix;
		}
		args[2] = currentContext.postfix
	}
	return _load(args[0],args[1],args[2],globalsFilters.length-1)
}
function _serialize(res){
	if(!('data' in res)){
		var data = res.cache[res.cache.length-1];
		var path = res.path;
		for(var i=0;i<serializeFilter.length;i++){
			var pf = serializeFilter[i];
			if(pf[0].test(path)){
				data = pf[1].apply(res,data);
			}
		}
		res.data = _defaultSerializeFilter.call(res,data);
	}
}
function _hash(res){
	if(!res.hash){
		_serialize(res);
		res.hash = Crypto.createHash('md5').update(res.data).digest('base64');
	}
}
/**
 * @public
 * @param path 文件路径
 * @param prefix such as browser info: -webkit,-moz,-o,-ms
 * @param postfix such as i18n key: zh_CN,zh_TW
 * @return Buffer cache
 */
function getContent(path,prefix,postfix){
	var cache = _loadAll(arguments).cache;
	return cache[cache.length-1]
}
function getContentAsBinary(path,prefix,postfix){
	var res = _loadAll(arguments);
	_serialize(res);
	return res.data;
}

function getContentHash(path,prefix,postfix){
	var res = _loadAll(arguments);
	_hash(res);
	return res.hash;
}
function _onChange(key,files){
	console.info("clean:",files);
	var instance = instanceMap[key];
	if(instance){
		var cleanMap = instance.cleanMap;
		var resourceMap = instance.resourceMap;
		if(cleanMap){
			if(typeof files == 'string'){
				_removeResource(cleanMap,resourceMap,files)
			}else{
				var i = files.length;
				while(i--){
					_removeResource(cleanMap,resourceMap,files[i])
				}
			}
		}
	}
}
function _removeResource(cleanMap,resourceMap,file){
	var list = cleanMap[file];
	var i = list && list.length;
	while(i--){
		delete resourceMap[list[i]];
	}
	delete cleanMap[file];
	
}
exports.getContent = getContent;
exports.getContentAsBinary = getContentAsBinary;
exports.getContentHash = getContentHash;

exports.setRoot = function(root_){
	root = Path.normalize(root_)
	VFS.initialize(root,_onChange);
}
exports.getRoot = function(){return root};

/*======================  on process time  =====================*/
/*======================  throws exception out of process =======*/ 
/**
 * 用于在插件运行过程中，装载指定路径下内容，并经过当前以执行的过滤器处理后的文本。
 * @public
 * @param path 文件路径（相对网站根目录）
 * @return String 文件内容文本
 */
exports.loadOnChain = function(path){
	currentContext.instance
}
/**
 * 添加关联文件
 * @public
 * @param path 文件路径（相对当前处理文件）
 */
exports.addRelation = function(relation){
	var instance = currentContext.instance;
	var resource = currentContext.resource;
	var path = resource.path;
	var list = instance.cleanMap[relation];
	if(list){
		list.push(path);
	}
	instance.cleanMap[relation] = [path];
	list = instance.resourceMap[path].relation.push(relation);
}



/*======================  on initialize time  =====================*/
/*======================  need reset after invoked ================*/ 
exports.addBinaryBuilder = function(pattern,impl){
	_addFilter(pattern,impl,BINARY_BUILDER);
}
exports.addTextBuilder = function(pattern,impl){
	_addFilter(pattern,impl,TEXT_BUILDER);
}
exports.addTextFilter = function(pattern,impl){
	_addFilter(pattern,impl,TEXT_FILTER);
}
exports.addDOMBuilder = function(pattern,impl){
	_addFilter(pattern,impl,DOM_BUILDER);
}
exports.addDOMFilter = function(pattern,impl){
	_addFilter(pattern,impl,DOM_FILTER);
}
exports.addSerializer = function(pattern,impl){
	_addFilter(pattern,impl,SERIALIZER);
}


