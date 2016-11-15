var UglifyJS = require('uglifyjs');
var TreeWalker = UglifyJS.TreeWalker;
var fs = require('fs');
//var numbers = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$_0123456789";
var defaultGlobals = ("Math,JSON,String,Number,Date,console,parseInt,Function,Array,Object,Error,RegExp,undefined,"+
	"document,window,arguments,setTimeout,clearTimeout,setInterval,clearInterval,localStorage," +
	"requestAnimationFrame,cancelAnimationFrame,Image,XMLHttpRequest," +
	"prompt,alert,confirm,"+
	"navigator,btoa,atob,devicePixelRatio,"+
	"require,exports").split(/[^\w]+/);

exports.decodeRequireVariable = decodeRequireVariable;
exports.encodeRequireVariable = encodeRequireVariable;
exports.analyse =analyse;


exports. getAccessorProperty=getAccessorProperty;
exports.isExportProperty = isExportProperty;
exports.runAnalyse = function(args){
	//console.log(args)
	for(var i =0;i<args.length;i++){
		var code = fs.readFileSync(args[i].replace(/#.*/,'')).toString();
		//console.log(code)
		var result = analyse(code,args[i]);
		var reportfile = args[i];
		if(result.undeclareds.length){
			reportfile && (reportfile = console.info(reportfile+':'))
			console.error('缺申明的变量:');
			console.error('\t',result.undeclareds.join(','));
		}
		if(result.unreferenceds.length){
			reportfile && (reportfile = console.info(reportfile+':'))
			console.warn('未使用变量:');
			console.warn('\t',result.unreferenceds.join(','));
		}
		if(result.requires.length){
			reportfile && (reportfile = console.info(reportfile+':'))
			console.log('依赖模块(require):');
			console.log('\t',result.requires.join(','));
		}
		if(result.exportVars.length){
			reportfile && (reportfile = console.info(reportfile+':'))
			console.log('导出变量(exports):');
			console.log('\t',result.exportVars.join(','));
		}
		
		if(result.invalidRequires.length){
			reportfile && (reportfile = console.info(reportfile+':'))
			console.warn('无法优化的 require模块:');
			console.warn('\t',result.invalidRequires.join(','));
		}
		if(false &&
		result.ast){
			reportfile && (reportfile = console.info(reportfile+':'))
			console.info('转换后结果：')
			console.info(result.ast.print_to_string({ beautify: true }))
		}
	}
}



function checkRequireExport(topLevel,baseModuleName,requires,exportVars,invalidRequires,invalidExports){
	var requireDefNodes = [];
	var requireDefMap = {};
	var moduleConfigMap = {}//name:{properties:[],vars:[]}
	topLevel.walk(new TreeWalker(function(node, descend){
		var parentNode = this.parent(0);
		if (node instanceof UglifyJS.AST_Call) {
			var callee = node.expression;
			var args = node.args;
			var isRequire = callee.name == 'require'
				&& callee instanceof UglifyJS.AST_SymbolRef
				&& callee.undeclared();
			
			if(isRequire){
				if(args.length,args[0] instanceof UglifyJS.AST_String){
					var requireModuleId = args[0].value;
					requireModuleId = normalizeModule(requireModuleId,baseModuleName);
					if(!checkRequireInfo(node,parentNode,requireModuleId)){
						addOnce(invalidRequires,requireModuleId)
						console.warn('invalid require2:',parentNode.print_to_string());
					}
				}else{
					addOnce(invalidRequires,"*")
					console.warn('require: invalid called '+ args[0].print_to_string());
				}
				addOnce(requires,requireModuleId || '*')
			}
		}else if(node instanceof UglifyJS.AST_Symbol){
			var n = node.name;
			if(n == 'exports' && node.undeclared() ){
				var exportVar = getAccessorProperty(parentNode)
				var scope = this.find_parent(UglifyJS.AST_Scope);
				if(scope!=topLevel){
					//addOnce(exportVars,'*')
					//addOnce(invalidExports,exportVar);
					console.error('exports must declared in the top level scope!!',parentNode.print_to_string());
				}
				//else 
				if(typeof exportVar =='string'){
					exportVars.push(exportVar)
				}else{
					addOnce(exportVars,'*')
					addOnce(invalidExports,exportVar)
				}
			}
		}
	}));
	if(invalidRequires.length ==0 && invalidExports.length==0){
		topLevel.walk(new TreeWalker(function(node, descend){
			if(node instanceof UglifyJS.AST_Symbol){
				//收集使用了require 模块  零时变量的情况
				if(requireDefNodes.indexOf(node.thedef) >=0){
					//引用了临时require 变量
					for(var moduleName in requireDefMap){
						//查找def 定义的 别名： moduleName
						if(requireDefMap[moduleName] == node.thedef){
							break;
						}
					}
					//前面逻辑决定这一定有值（但可能指向错误，checkRequireInfo一定药小心处理）
					//但是如果多个模块使用了相同的零时变量名，就可能找不到正确的moduleName
					//所以，requireDefMap 生成的时候一定要排除这问题（））
					var config = moduleConfigMap[moduleName];
					var parentNode = this.parent(0);
					var accessor = getAccessorProperty(parentNode)
					var valid = false;
					//console.log(moduleName,config,moduleMap)
					
					if(accessor && parentNode.expression == node){
						var requireProperty = "*";
						if(typeof accessor == 'string'){
							requireProperty = accessor;
							valid = true;
						}else{
							console.error('invalid require var asscess:',baseModuleName,accessor.TYPE);
						}
						parentNode.requireToken = [moduleName,requireProperty]
						addOnce(config.properties,requireProperty)
					}else if(parentNode instanceof UglifyJS.AST_VarDef){
						valid = true;
					}else{
						console.error('invalid require var access:',baseModuleName,parentNode.TYPE);
					}
					if(!valid){
						addOnce(invalidRequires,moduleName)
					}
				}
				
			}
		}));
	}
	function checkRequireInfo(requireNode,parentNode,moduleName){
		var valid = false;//许可的可优化 require 调用
		var accessor = getAccessorProperty(parentNode)
		var config = moduleConfigMap[moduleName] || (moduleConfigMap[moduleName] = {properties:[],aliasDefs:[]})
					
		if( accessor  && parentNode.expression == requireNode){
			//匿名require模块的直接属性访问
			var requireProperty = "*";
			if(typeof accessor == 'string'){
				//静态属性:require('moduleid').accessor
				requireProperty = accessor;
				valid = true;
				//console.error(base,requireModule,requireProperty,requireNode.print_to_string())
			}else{
				//动态属性， 无法优化
			}
			parentNode.requireToken = [moduleName,requireProperty]
			addOnce(config.properties,requireProperty)
		}else  if(parentNode instanceof UglifyJS.AST_VarDef){
			//require模块零时变量： var xxx = require('moduleid')
			//console.error(requireNode.print_to_string())
			if(parentNode.name instanceof UglifyJS.AST_SymbolVar){
				var requireDef  = parentNode.name.thedef;
				var oldDef = requireDefMap[moduleName];
				parentNode.requireToken = [moduleName];
				requireDefMap[moduleName] = requireDef;
				if(!oldDef || oldDef == requireDef){
					addOnce(config.aliasDefs,requireDef)
					addOnce(requireDefNodes,requireDef)
					valid = true;
				}else{
					//不允许两个模块先后定义了同一个变量名！
				}
			}else{
				//
				//console.warn('unknow assign type',base,parentNode.name.TYPE)
			}
		}else{
			//require 模块当做自由变量使用， 无法优化
			//console.warn('unknow assign type',base,parentNode.name.TYPE)
		}
		return valid;
	}
}
/**
 * return {undeclareds: [], unreferenceds:[],requires:[]}
 */
function analyse(code,baseModuleName){
	var topLevel = UglifyJS.parse(code,{filename:baseModuleName+'.js'});
	topLevel.figure_out_scope();
	var undeclareds = [];
	var unreferenceds = [];
	topLevel.walk(new TreeWalker(function(node, descend){
		var parentNode = this.parent(0);
		if(node instanceof UglifyJS.AST_Symbol){
			var n = node.name;
			if (n != 'this' && defaultGlobals.indexOf(n) < 0) {
				if(node.unreferenced()){unreferenceds.push(n+position(node))}
				if(node.undeclared()){undeclareds.push(n+position(node))}
			}
		}
	}));
	
	var requires = [];
	var exportVars = [];
	var invalidRequires = [];
	var invalidExports = [];
	checkRequireExport(topLevel,baseModuleName,requires,exportVars,invalidRequires,invalidExports)
	
	//console.error('debug',invalidRequires.length , invalidExports.length)

	return {undeclareds: undeclareds, unreferenceds:unreferenceds,
		invalidExports:invalidExports,invalidRequires:invalidRequires,
		requires:requires,exportVars:exportVars,
		ast:topLevel}
}











































/* utils **/


function position(node){
	return '#'+node.start.line+' '+node.start.col
}

function getAccessorProperty(node){
	if(node instanceof UglifyJS.AST_PropAccess){
		var p = node.property;
		//if(typeof p == 'string'){
		if(typeof p instanceof UglifyJS.AST_String){
			p= p.value;
		}
		return p
	}
}
function isExportProperty(node){
	if(node instanceof  UglifyJS.AST_PropAccess){
		var owner = node.expression;
		if(owner.name == 'exports' && owner instanceof UglifyJS.AST_Symbol && owner.undeclared()){
			return true;
		}
	}
}

function normalizeModule(url,baseModuleName){
	if(url.charAt(0) == '.'){
		url = baseModuleName.replace(/[^\/]+$/,'')+url
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


function decodeRequireVariable(id){
	if(id.indexOf('$$') ==0){
		var sp = id.lastIndexOf('$$');
		if(sp>0){
			return [decodeName(id.substring(2,sp)),
					decodeName(id.substring(sp+2))]
		}
	}
}
function encodeRequireVariable(moduleName,property){
	var id = '$$'+encodeName(moduleName)+'$$'+encodeName(property);
	return id;
}
function encodeName(variable){
	return variable.replace(/[^\w]/g,function(c){
		return '$'+(0x10000+c.charCodeAt()).toString(16).substr(1)
	})
}
function decodeName(variable){
	return variable.replace(/\$([a-fA-F0-9]{4})/g,function(a,v){
		return String.fromCharCode(parseInt(v,16))
	})
}