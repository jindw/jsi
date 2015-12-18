var UglifyJS = require('uglifyjs');
var TreeWalker = UglifyJS.TreeWalker;
var fs = require('fs');
var numbers = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$_0123456789";
var defaultGlobals = "Math,JSON,Number,Date,console,parseInt,"+
	"document,window,arguments,setTimeout,clearTimeout,setInterval,clearInterval," +
	"requestAnimationFrame,cancelAnimationFrame,Image," +
	"prompt,alert,confirm,"+
	"require,exports".split(/[^\w]+/);
exports.runAnalyse = function(args){
	//console.log(args)
	for(var i =0;i<args.length;i++){
		var code = fs.readFileSync(args[i].replace(/#.*/,'')).toString();
		//console.log(code)
		var result = analyse(code,args[i]);
		if(result.undeclareds.length){
			console.error('缺申明的变量:');
			console.error('\t',result.undeclareds.join(','));
		}
		if(result.unreferenceds.length){
			console.warn('未使用变量:');
			console.warn('\t',result.unreferenceds.join(','));
		}
		if(result.requires.length){
			console.log('依赖模块(require):');
			console.log('\t',result.requires.join(','));
		}
		if(result.exportVars.length){
			console.log('导出变量(exports):');
			console.log('\t',result.exportVars.join(','));
		}
		
		if(result.invalidRequires.length){
			console.warn('无法优化的 require模块:');
			console.warn('\t',result.invalidRequires.join(','));
		}
		if(false &&
		result.exportAst){
			console.info('转换后结果：')
			console.info(result.exportAst.print_to_string({ beautify: true }))
		}
	}
}


var KEYWORDS = 'break case catch const continue debugger default delete do else finally for function if in instanceof new return switch throw try typeof var void while with';
var KEYWORDS_ATOM = 'false null true';
var RESERVED_WORDS = 'abstract boolean byte char class double enum export extends final float goto implements import int interface long native package private protected public short static super synchronized this throws transient volatile yield'
    + " " + KEYWORDS_ATOM + " " + KEYWORDS;
RESERVED_WORDS = RESERVED_WORDS.split(/\s+/)
/**
 * jsi merge <path1>#exportValue1,exportValue2 <path2> ..')
 * console.log('jsi merge <path1>#* <path2> ..
 */
exports.runMerge = function(paths,opt){
	var globals = {};
	var varIndex = 0;
	function nextGlobals(size){
		if(varIndex<size){
			return numbers.charAt(varIndex++);
		}else{
			size = size || numbers.length;
			while(++varIndex%size>=size-10);
			var result = [];
			var left = varIndex;
			do{
				var c = left%size;
				result.push(numbers.charAt(c));
				left-=c;
				left/=size;
			}while(left>0)
			result = result.join('')
			if(RESERVED_WORDS.indexOf(result)>=0){
				return nextGlobals(size);
			}else{
				return result;
			}
		}
	}
	
	var cmdExportMap = {};
	var analyseMap = {};
	var prefix = null;
	
	//console.log('###########'+paths)
	var realPaths = paths.map(function(path){
		var ps = path.split('#');
		var path = fs.realpathSync(ps[0]).replace(/[\\]/g,'/');
		var hash = ps[1];
		if(prefix == null){
			prefix = path.replace(/[^\/]*$/,'')
		}else{
			while(path.indexOf(prefix)!=0){
				prefix = prefix.replace(/[^\/]*$/,'')
			}
		}
		if(ps[1]){
			cmdExportMap[ path] = hash.split(/[^\w*]+/);
			analyseMap
		}
		return path;
	});
	var mangledMap = {};
	var globalExportList =[];
	var globalExportMap =[];
	var i = realPaths.length;
	while(i--){
		var path = realPaths[i];
		var code = fs.readFileSync(path).toString();
		var id = path.substring(prefix.length).replace(/\.js$/,'');
		var info = analyseMap[id] = analyse(code,id);
		var exportVarNames = info.exportVars;
		var cmdFileExports = cmdExportMap[path];
		
		//console.error(exportVarNames,cmdFileExports)
		if(cmdFileExports == null){
			cmdFileExports = [];
		}else if(cmdFileExports.indexOf('*')>=0){
			cmdFileExports = exportVarNames.concat();
		}
		
		//console.warn(id,cmdFileExports)
		var j = cmdFileExports.length;
		while(j--){
			var v = cmdFileExports[j]
			if(exportVarNames.indexOf(v) >=0){
				if(globalExportList.indexOf(v) >=0){
					console.error('muti exports variable:',path+'#'+v+' and '+globalExportMap[v]);
				}
				globalExportList.push(v);
				globalExportMap[v] = path;
			}else{
				console.error('miss exports variable:',path+'#'+v);
				cmdFileExports.splice(j,1)
			}
		}
		cmdExportMap[path] = cmdFileExports;
		//mangle_exports
		var mangledVars = mangledMap[path] = {};
		for(var n in info.exportMap){
			var realName = info.exportMap[n];
			var v = info.exportAst.variables.get(realName);
			mangledVars[realName] = v.mangled_name = nextGlobals();
			
			//console.log('####',n,realName,v.mangled_name)
		}
	}
	//console.warn('ast end')
	
	var contents = [];
	for(var id in analyseMap){
		var info = analyseMap[id];
		var mangledName = [];
		info.exportAst.variables.each(function(v,n){
			if(!v.mangled_name){
				var requireVarInfo = decodeRequireVariable(v.name);
				if(requireVarInfo){
					var externalModule = requireVarInfo[0];
					var property = requireVarInfo[1];
					if(externalModule in analyseMap){
						var externalInfo = analyseMap[externalModule];
						var externalRealName = externalInfo.exportMap[property];
						var externalVariable = externalInfo.exportAst.variables.get(externalRealName);
						//console.log(property,externalRealName,externalInfo.exportMap)
						//if(!externalVariable.mangled_name){
						
						//	console.log('@@@@',externalInfo.exportAst.variables.get(externalRealName).managled_name)
						//}
						v.mangled_name = externalVariable.mangled_name;
					}else{
						console.error('dependenced module not fount:',externalModule);
					}
				}else if(v.undeclared){
					console.warn('foun undeclared variables:',v.name)
				}else{
					//没必要，反正后面还要做一次整体混淆
					//v.mangled_name = nextGlobals();
				}
			}
		})
		//console.warn('mangled!!')
		var compressAst = info.exportAst;
		//必要，否则可能出现重名现象
		compressAst.mangle_names();
		contents.push(compressAst.print_to_string({beautify:false}));
		
		//console.warn('mangled!!printed')
		
	}
	contents = contents.join('\n');
	
	var vars = [];
	var result =[];
	var ns = opt && opt.ns;
	for(var n in globalExportMap){
		var path = globalExportMap[n];
		var id =path.substring(prefix.length).replace(/\.js$/,'');
		var mangledVars = mangledMap[path];
		var info =  analyseMap[id];
		var realName = info.exportMap[n];
		var v = info.exportAst.variables.get(realName);
		var mangled = mangledVars[realName];
		
		//console.error('@@@@',n,realName,mangled)
		//console.dir(mangledVars)
		if(ns){
			result.push(',',n,':',mangled);
		}else{
			result.push(',',n+'=',mangled);
		}
		vars.push(n);
		//mangleds.push(mangled);
	}
	
	//console.error('@@@@@')
	//contents = "alert(123)"
	if(ns && vars.length){
		result[0] = 'return {';
		result.push('}}()');
		result.unshift(ns.replace(/^[\w\$]+$/,'var $&'),'=function(){',contents)
	}else{
		result[0] = vars.length?'\nreturn ':'';
		//console.warn(result.join(''))
		result.splice(result.length-2,1)
		result.push('}()')
		//console.warn(result.join(''))
		result.unshift(vars.join(',').replace(/.+/,'var $&=')||'+','function(){',contents);
	}
	
	//console.error('@@@@@')
	contents = result.join('');
	//console.log(contents)
	//console.error(contents)
	//console.log(contents);
	var compressor = UglifyJS.Compressor();
	//UglifyJS.AST_Node.warn_function = Function.prototype;
	var compressed = UglifyJS.parse(contents,{filename:paths.join('')+'.js'});
	compressed.figure_out_scope();
	compressed.compute_char_frequency();
	compressed.mangle_names();
	compressed = compressed.transform(compressor);
	//console.dir(opt)
	compressed = compressed.print_to_string({beautify:!!(opt.f||opt.format)});
	return (compressed)
}


function normalizeModule(url,base){
	if(url.charAt(0) == '.'){
		url = base.replace(/[^\/]+$/,'')+url
		while(url != (url =url.replace( /[^\/]+\/\.\.\/|(\/)?\.\//,'$1')));
	}
	return url;
}
function addOnce(list,o){
	var i = list.indexOf(o);
	if(i<0){
		list.push(o);
	}
}

function propertyAccessor(node){
	if(node instanceof UglifyJS.AST_PropAccess){
		var p = node.property;
		//if(typeof p == 'string'){
		if(typeof p instanceof UglifyJS.AST_String){
			p= p.value;
		}
		return p
	}
}

function decodeRequireVariable(id){
	if(id.indexOf('$$') ==0){
		var sp = id.lastIndexOf('$$');
		if(sp>0){
			return [decodeVariable(id.substring(2,sp)),
					decodeVariable(id.substring(sp+2))]
		}
	}
}
function encodeRequireVariable(moduleName,property){
	var id = '$$'+encodeVariable(moduleName)+'$$'+encodeVariable(property);
	return id;
}
function encodeVariable(variable){
	return variable.replace(/[^\w]/g,function(c){
		return '$'+(0x10000c.charCodeAt()).toString(16).substr(1)
	})
}
function decodeVariable(variable){
	return variable.replace(/\$([a-fA-F0-9]{4})/g,function(a,v){
		return String.fromCharCode(parseInt(v,16))
	})
}
/**
 * return {undeclareds: [], unreferenceds:[],requires:[]}
 */
function analyse(code,base){
	var topLevel = UglifyJS.parse(code,{filename:base+'.js'});
	var undeclareds = [];
	var unreferenceds = [];
	var exportMap = {};
	var exportVars = [];
	var requires = [];
	var requireVars = [];
	var requireVarMap = {};
	var invalidRequires = [];
	var invalidExports = [];
	var moduleConfigMap = {}//name:{properties:[],vars:[]}
	topLevel.figure_out_scope();
	topLevel.walk(new TreeWalker(function(node, descend){
		var parentNode = this.parent(0);
		if(node instanceof UglifyJS.AST_Symbol){
			var n = node.name;
			if (n != 'this' && defaultGlobals.indexOf(n) < 0) {
				if(node.unreferenced()){unreferenceds.push(n+position(node))}
				if(node.undeclared()){undeclareds.push(n+position(node))}
			}
			if(n == 'exports' && node.undeclared() ){
				var accessor = propertyAccessor(parentNode)
				var scope = this.find_parent(UglifyJS.AST_Scope);
				if(scope!=topLevel){
					addOnce(exportVars,'*')
					addOnce(invalidExports,requireModule);
					console.error('exports must declared in the top level scope!!',parentNode.print_to_string());
				}else if(typeof accessor =='string'){
					exportVars.push(accessor)
				}else{
					addOnce(exportVars,'*')
					addOnce(invalidExports,requireModule)
				}
			}
		}else if (node instanceof UglifyJS.AST_Call) {
			var callee = node.expression;
			var args = node.args;
			var isRequire = callee.name == 'require'
			 && callee instanceof UglifyJS.AST_SymbolRef
			  && callee.undeclared();
			
			if(isRequire){
				if(args.length,args[0] instanceof UglifyJS.AST_String){
					var requireModule = args[0].value;
					requireModule = normalizeModule(requireModule,base);
					if(!appendRequireInfo(node,parentNode,requireModule)){
						addOnce(invalidRequires,requireModule)
						console.warn('invalid require2:',parentNode.print_to_string());
					}
				}else{
					addOnce(invalidRequires,"*")
					console.warn('require: invalid called '+ args[0].print_to_string());
				}
				addOnce(requires,requireModule || '*')
			}
		}
	}));
	
	function appendRequireInfo(requireNode,parentNode,requireModule){
		var avaliable = false;
		var config = moduleConfigMap[requireModule] || (moduleConfigMap[requireModule] = {properties:[],vars:[]})
		var accessor = propertyAccessor(parentNode)
		
		if( accessor  && parentNode.expression == requireNode){
			var requireProperty = "*";
			if(typeof accessor == 'string'){
				requireProperty = accessor;
				avaliable = true;
				//console.error(base,requireModule,requireProperty,requireNode.print_to_string())
			}
			parentNode.replaceToken = [requireModule,requireProperty]
			addOnce(config.properties,requireProperty)
		}else  if(parentNode instanceof UglifyJS.AST_VarDef){
			//console.error(requireNode.print_to_string())
			if(parentNode.name instanceof UglifyJS.AST_SymbolVar){
				avaliable = true;
				var requireDef  = parentNode.name.thedef;
				parentNode.replaceToken = [requireModule];
				var def = requireVarMap[requireModule];
				requireVarMap[requireModule] = requireDef;
				if(!def || def == requireDef){
					addOnce(config.vars,requireDef)
					addOnce(requireVars,requireDef)
					avaliable = true;
				}
			}else{
				//console.warn('unknow assign type',base,parentNode.name.TYPE)
			}
		}else{
			//console.warn('unknow assign type',base,parentNode.name.TYPE)
		}
		return avaliable;
	}
	//console.log('@@@@@',invalidRequires.length)
	
	if(invalidRequires.length ==0 && invalidExports.length==0){
		topLevel.walk(new TreeWalker(function(node, descend){
			if(node instanceof UglifyJS.AST_Symbol){
				//收集使用了零时require变量的情况
				if(requireVars.indexOf(node.thedef) >=0){
					for(var moduleName in requireVarMap){
						if(requireVarMap[moduleName] == node.thedef){
							break;
						}
					}
					var config = moduleConfigMap[moduleName];//前面逻辑决定这一定有值
					var parentNode = this.parent(0);
					var accessor = propertyAccessor(parentNode)
					var avaliable = false;
					//console.log(moduleName,config,moduleMap)
					
					if(accessor && parentNode.expression == node){
						var requireProperty = "*";
						if(typeof accessor == 'string'){
							requireProperty = accessor;
							avaliable = true;
						}else{
							console.error('invalid require var asscess:',base,accessor.TYPE);
						}
						parentNode.replaceToken = [moduleName,requireProperty]
						addOnce(config.properties,requireProperty)
					}else if(parentNode instanceof UglifyJS.AST_VarDef){
						avaliable = true;
					}else{
						console.error('invalid require var asscess:',base,parentNode.TYPE);
					}
					if(!avaliable){
						addOnce(invalidRequires,moduleName)
					}
				}
				
			}
		}));
	}
	//console.error('debug',invalidRequires.length , invalidExports.length)
	if(invalidRequires.length ==0 && invalidExports.length ==0){//do replace!!
		var requireMap = {};

		
		
		function beforeReplacer(node,descend){
			if(node.replaceToken){
				//吧所有标记为需要替换成 外部require引用的地方替换成外部引用
				var n = encodeRequireVariable.apply(this,node.replaceToken);
				var def = topLevel.variables.get(n);
				//console.warn('def not found')
				var symbolRef = new UglifyJS.AST_SymbolRef({
					scope:topLevel,
					thedef:def,
					name  : n,
					start : node.start,
					end   : node.end
				});
				if(def == null){
					def = new UglifyJS.SymbolDef(topLevel, topLevel.variables.size(), symbolRef);
					def.undeclared = false;
					def.global = false;
					topLevel.variables.set(n, def);
				}else{
					def.references.push(symbolRef)
				}
				var scope = this.find_parent(UglifyJS.AST_Scope);
				
				//添加enclosed
				do{
					//TODO 貌似遇到同名的就应该终止冒泡了吧
					addOnce(scope.enclosed,def);
				}while(scope=scope.parent_scope)
				
				symbolRef.thedef = def;
				//console.warn('replaced token:'+node.replaceToken);
				return symbolRef;
			}else if(node instanceof UglifyJS.AST_Var){
				//删除临时的 require 申明对象
				var defs = node.definitions;
				var i = defs.length;
				while(i--){
					var def = defs[i];
					if(def.replaceToken){
						defs.splice(i,1);
						//减掉相应的symbolDef
						var scope = this.find_parent(UglifyJS.AST_Scope);
						//console.log('remove temp require variable:',def.name.name)
						scope.variables.del(def.name.name);
					}
				}
				if(defs.length == 0){
					var empty = new UglifyJS.AST_EmptyStatement({start:def.start,end:def.end});
					return empty;
				}else{
					//返回对象就是跳过批处理，不对！
					//return node;
				}
			}else if(isExportProperty(node)){//如果放after里面，parentNode.left == thisnode 会失败， 因为已经被拷贝了
				var parentNode = this.parent(0);
				var isSetExportValue = (parentNode instanceof UglifyJS.AST_Assign) && parentNode.left == node
				if(!isSetExportValue){
					//吧exports 当普通变量使用
					var exportName = propertyAccessor(left)
					
					var symbolRef = new UglifyJS.AST_SymbolRef({
						scope:topLevel,
						//thedef:def,
						name  : exportName,
						start : node.start,
						end   : node.end
					});
					exportsRefs.push(symbolRef)
					return symbolRef;
				}
			}
			//return node;
		}
		
		var exportsRefs = [];
		function isExportProperty(node){
			if(node instanceof  UglifyJS.AST_PropAccess){
				var owner = node.expression;
				if(owner.name == 'exports' && owner instanceof UglifyJS.AST_Symbol && owner.undeclared()){
					return true;
				}
			}
		}
		function afterReplacer(node){
			//console.error('debug')
			//替换掉 exports.xxx = value -> var xxx = value or <empty statements>
			//这些申明大部分会替换成空语句
			if(node instanceof UglifyJS.AST_Toplevel){
				//console.error('exportsRefs as variable count :',exportsRefs.length)
				for(var i=0;i<exportsRefs.length;i++){
					var symbolRef = exportsRefs[i];
					var exportName = symbolRef.name;
					
					var symbolDef = topLevel.variables.get(exportMap[exportName]);
					if(symbolDef == null){
						console.error('undeclared exports variables!!',symbolRef.name);
					}
					symbolRef.thedef = symbolDef;
					symbolRef.name = symbolDef.name;
				}
			}
			if(node instanceof UglifyJS.AST_Assign && node.operator == '='){
				var left = node.left;//accessor
				if(isExportProperty(left)){
					//console.error(left)
					//exports
					var exportName = propertyAccessor(left)
					var right = node.right;
					var onlyExport = exportVars.indexOf(exportName) == exportVars.lastIndexOf(exportName);
					
					
						
					if(right instanceof UglifyJS.AST_SymbolRef){
						if(exportName in exportMap){
							exportName = exportMap[exportName] ;
							//do renamed declare!!!
							//很可能重复申明，交给后期压缩器优化
						}else{
							exportMap[exportName] = right.name;
							return new UglifyJS.AST_EmptyStatement({start:node.start,end:node.end});
						}
					}else{
						exportMap[exportName] = exportName;
					}
					
					{
						var symbolVar = new UglifyJS.AST_SymbolVar({ 
							name: exportName,
							start : right.start,
							end   : right.end
						})
						var symbolDef = topLevel.variables.get(exportName);
						if(symbolDef == null){
							symbolDef = new UglifyJS.SymbolDef(topLevel, 
								topLevel.variables.size(), 
								symbolVar);
							symbolDef.undeclared = true;
							symbolDef.global = true;
							symbolDef.name = exportName;
							
							topLevel.variables.set(exportName, symbolDef);
							//console.log('%%%%',exportName,topLevel.variables.get(exportName) == null)
						}
						//console.warn('@@ append variable',exportName)
						symbolVar.thedef = symbolDef;
						return new UglifyJS.AST_Var({
							definitions: [new UglifyJS.AST_VarDef({
									name  : symbolVar,
									value : right, // the original AST_String
									start : right.start,
									end   : right.end
								})]
						});;
					}
				}
			}
		}
		var tranformed = topLevel.transform(new UglifyJS.TreeTransformer(beforeReplacer,afterReplacer));
		//tranformed = UglifyJS.parse(tranformed.print_to_string({beautify:true}),{filename:base+'.js'});;
		//console.warn('completed!')
		//tranformed.figure_out_scope();
		//console.warn(tranformed==null)
		//console.log('complete',tranformed.print_to_string({beautify:true}))
	}
	return {undeclareds: undeclareds, unreferenceds:unreferenceds,
		exportVars:exportVars,exportMap:exportMap,invalidExports:invalidExports,exportAst:tranformed,
		requires:requires,invalidRequires:invalidRequires}
}
function position(node){
	return '#'+node.start.line+' '+node.start.col
}

//ast.mangle_names();
