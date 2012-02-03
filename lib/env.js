var VFS = new require('./vfs').VFS;
var Crypto = require('crypto');
var Path = require('path');
var FS = require('fs');

var BINARY_BUILDER = 0;
var TEXT_BUILDER = 1;
var TEXT_FILTER = 2;
var DOM_BUILDER = 3;
var DOM_FILTER = 4;
var SERIALIZER = 5;
function Resource(env,instance,path,prefix,postfix){
	this.env = env;
	this.instance = instance;
	this.path = path;
	this.content = [];
	this.cache = [];
	this.relation = [];
	this.time = +new Date();
	this.prefix = prefix;
	this.postfix = postfix;
}

/**
 * 用于在插件运行过程中，装载指定路径下内容，并经过当前以执行的过滤器处理后的文本。
 * @public
 * @param path 文件路径（相对网站根目录）
 * @return String 文件内容文本
 */
Resource.prototype.load = function(path){
	var index = this.index;
	return env.load(path,this.prefix,this.postfix,index).content[index];
};
/**
 * 添加关联文件
 * @public
 * @param path 文件路径（相对当前处理文件）
 */
Resource.prototype.addRelation = function(relation){
	var instance = this.instance;
	var path = this.path;
	var list = instance.cleanMap[relation];
	if(list){
		list.push(path);
	}else{
		instance.cleanMap[relation] = [path];
	}
	this.relation.push(relation);
};
Resource.prototype.getExternalAsBinary = function(file,root){
	var key = this.instance.key;
	if(root){
		file = vfs.getFile(Path.resolve(root,'.'+path));
	}
	return this.env._getDataAsBinary(this.path,key,file)

}

function ENV(root){
	
	/**
	 * {
	 * 	'prefix::postfix':{
	 * 		cleanMap:{
	 * 			'${path}' : []
	 * 		}
	 * 		resourceMap:{
	 * 			'${path}' : {
	 * 				relation:[relation1,..],
	 * 				content:[content1,content2...],
	 * 				time:+new Date()
	 * 			}
	 * 		}
	 * 		
	 *  }
	 * }
	 */
	var instanceMap = {};
	var globalsFilters = [];
	var textBuilderStart = 0;
	var textFilterStart = 0;
	var domBuilderStart = 0;
	var domFilterStart = 0;
	
	var serializeFilter = [];
	var translators = [];
	var env = this;
	var vfs = new VFS(_onChange);
	var binaryMap = {};
	var fileMap = {};
	this.vfs = vfs;
	root = Path.normalize(root)

	/**
	 * 
	 */
	function load(path,prefix,postfix,lastIndex){
		var instanceKey = [prefix,postfix].join('::');
		var instance = instanceMap[instanceKey] || (instanceMap[instanceKey] = {key:instanceKey,cleanMap:{},resourceMap:{}});
		var resource = instance.resourceMap[path] || 
				( instance.resourceMap[path] = new Resource(env,instance,path,prefix,postfix));
		var content = resource.content;
		var data;
		if(lastIndex in content){
			return resource;
		}
		for(var i=0;i<=lastIndex;i++){
			if(content[i]===undefined){
				var pf = globalsFilters[i];
				if(pf[0].test(path)){
					data = _require(env,resource,data,i);
					data = content[i] = pf[1].call(env,resource,data);
				}else{
					content[i] = data || null;
				}
			}else{
				data = content[i];
			}
			resource.index = i;
		}
		return resource;
	}
	function _require(env,resource,data,i){
		if(data === undefined){
			data = resource.getExternalAsBinary(resource.sourcePath || resource.path,root);
		}
		if(i>=textFilterStart){
			data = _defaultTextFilter.call(env,resource,data);
		}
		if(i>=domFilterStart){
			data = _defaultDOMFilter.call(env,resource,data);
		}
		return data;
	}
	
	function _defaultDOMFilter(){
		return null;
	}
	function  _defaultTextFilter(resource,buf){
		//if(Buffer.isBuffer(buf))
		return buf.toString();
	}
	function  _defaultSerializeFilter(resource,data){
		if(data == null){
			return resource.getExternalAsBinary(resource.sourcePath || resource.path,root);
		}
		if(Buffer.isBuffer(data)){
			return data;
		}else{
			return new Buffer(data.toString());
		}
		
	}
	function _serialize(res,data){
		var path = res.path;
		for(var i=0;i<serializeFilter.length;i++){
			var pf = serializeFilter[i];
			if(pf[0].test(path)){
				data = pf[1].call(this,res,data);
			}
		}
		return _defaultSerializeFilter.call(this,res,data);
	}
	function _hash(res,data){
		data = _serialize.call(this,res,data)
		return Crypto.createHash('md5').update(data).digest('base64');
	}
	function _translate(resource,data,translator){
		var cache = resource.cache
		var len = translators.length;
		for(var i = 0;i<len;i++){
			if(translator === translators[i]){
				break;
			}
		}
		if(i in cache){
			return cache[i]
		}
		if(i == len){
			translators[i] = translator;
		}
		return cache[i] = translator.call(env,resource,data);
	}
	/**
	 * @public
	 * @param path 文件路径
	 * @param prefix such as browser info: -webkit,-moz,-o,-ms
	 * @param postfix such as i18n key: zh_CN,zh_TW
	 * @param translator 
	 * @return Buffer cache
	 */
	function getContent(path,prefix,postfix,translator){
		var resource = load(path,prefix,postfix,globalsFilters.length-1);
		var data = resource.content;
		data = data[data.length-1];
		if(translator){
			return _translate(resource,data,translator)
		}
		return  data;
		
	}
	function getContentAsBinary(path,prefix,postfix){
		return getContent(path,prefix,postfix,_serialize);
	}
	
	function getContentHash(path,prefix,postfix){
		return getContent(path,prefix,postfix,_hash);
	}
	env._getDataAsBinary = function(path,key,file){
		//var path = Path.relative(root,file).replace(/^\w/,'/$&')
		var data = binaryMap[file];
		var map = fileMap[file];
		if(!map){
			map = fileMap[file] = {};
		}
		map[key] = path
		if(!data){
			data = binaryMap[file] = FS.readFileSync(file)
		}
		return data;
	}
	
	
	function _onChange(key,file){
		console.info("clean:",file,path);
		delete binaryMap[file];
		
		var path = Path.relative(root,file).replace(/\\/g,'/').replace(/^\w/,'/$&')
		var instance = instanceMap[key];
		var map = fileMap[file];
		if(instance){
			var cleanMap = instance.cleanMap;
			var resourceMap = instance.resourceMap;
			if(map && map[key]){
				_remove_resource_cache(resourceMap,map[key]);
			}
			if(cleanMap){
				var list = cleanMap[path];
				
				delete cleanMap[path];
			}
		}
	}
	function _remove_resource_cache(resourceMap,list){
		var i = list && list.length;
		while(i--){
			delete resourceMap[list[i]];
		}
	}
	
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
	env.load = load;
	env.getContent = getContent;
	env.getContentAsBinary = getContentAsBinary;
	env.getContentHash = getContentHash;
	
	
	/*======================  on initialize time  =====================*/
	/*======================  need reset after invoked ================*/ 
	env.addBinaryBuilder = function(pattern,impl){
		_addFilter(pattern,impl,BINARY_BUILDER);
	}
	env.addTextBuilder = function(pattern,impl){
		_addFilter(pattern,impl,TEXT_BUILDER);
	}
	env.addTextFilter = function(pattern,impl){
		_addFilter(pattern,impl,TEXT_FILTER);
	}
	env.addDOMBuilder = function(pattern,impl){
		_addFilter(pattern,impl,DOM_BUILDER);
	}
	env.addDOMFilter = function(pattern,impl){
		_addFilter(pattern,impl,DOM_FILTER);
	}
	env.addSerializer = function(pattern,impl){
		_addFilter(pattern,impl,SERIALIZER);
	}
}
exports.ENV = ENV;
