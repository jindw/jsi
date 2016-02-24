exports.updateExportMap = updateExportMap
var UglifyJS = require('uglifyjs');
var TreeWalker = UglifyJS.TreeWalker;
var analyse = require('./uglify-analyse')
var isExportProperty = analyse.isExportProperty;
var getAccessorProperty = analyse.getAccessorProperty;
var encodeRequireVariable = analyse.encodeRequireVariable;
/**
 * 结构分析补刀
 * 1. 将require 系列，直接替换成目标模块的exports 引用形式
 * 2. 讲exports 别名映射到exportMap中。
 * 3. 将exports 申明改成有效的变量声明
 */
function updateExportMap(info){
	var topLevel = info.ast,exportVars = info.exportVars;
	var exportMap = info.exportMap;
	var exportsRefs = [];
	return topLevel.transform(new UglifyJS.TreeTransformer(beforeReplacer,afterReplacer));
	function beforeReplacer(node,descend){
		if(node instanceof UglifyJS.AST_Var){
			//删除临时的 require 申明对象, 改为直接的目标模块变量替换
			var defs = node.definitions;
			var i = defs.length;
			while(i--){
				var def = defs[i];
				if(def.requireToken){
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
		}else if(isExportProperty(node)){
			//如果放after里面，parentNode.left == thisnode 会失败， 因为已经被拷贝了
			var parentNode = this.parent(0);
			var isSetExportValue = (parentNode instanceof UglifyJS.AST_Assign) && parentNode.left == node
			//console.log("!!!!!!!"+isSetExportValue)
			if(!isSetExportValue){
				//吧exports 当普通变量使用
				//TODO:bug
				var exportName = getAccessorProperty(node)
				
				var symbolRef = new UglifyJS.AST_SymbolRef({
					scope:topLevel,
					//thedef:def,
					name  : exportName,
					start : node.start,
					end   : node.end
				});
				exportsRefs.push(symbolRef)
				//console.error(exportsRefs.map(function(a){return a.name}))
				return symbolRef;
			}
		}else if(node.requireToken){
			//吧所有标记为需要替换成 外部require引用的地方替换成外部引用
			//因为放在了Var 节点处理之后， 所以不需要考虑 requireToken.length == 1的情况
			var n = encodeRequireVariable.apply(this,node.requireToken);
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
				def.global = true;
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
		}
		//return node;
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
				}else{
					symbolRef.thedef = symbolDef;
					symbolRef.name = symbolDef.name;
				}
				//console.warn("add export name vars:"+exportName)
			}
		}
		var scope = this.find_parent(UglifyJS.AST_Scope);
		var nodeLeft = getAssignLeft(node);
		var topScope = !scope.parent_scope;
		if(nodeLeft){
			if(isExportProperty(nodeLeft)){
				var nodeRight = node.right;
				var exportName = getAccessorProperty(nodeLeft)
				var onlyExport = exportVars.indexOf(exportName) == exportVars.lastIndexOf(exportName);
				var parentNode = this.parent(0);
				var superLeft = getAssignLeft(parentNode);
				//if(exportName.indexOf('nativeHost') >=0 ){console.log(parentNode.TYPE);console.log(require('util').inspect(parentNode,true))}
				if(!(exportName in exportMap)){//防冲突
					if(parentNode instanceof UglifyJS.AST_VarDef//var xx = exports.xx = yy;
						||superLeft instanceof UglifyJS.AST_SymbolRef){//xx = exports.xx = yy;
						var varName = parentNode.name;
						if(topScope){
							exportMap[exportName] = varName.name;
							return node.right;
						}else{
							exportMap[exportName] = createExportVar(scope,exportName);
						}
					}else{
						if(nodeRight instanceof UglifyJS.AST_SymbolRef){
							//do renamed declare!!!
							//很可能重复申明，交给后期压缩器优化
							if(topScope){
								exportMap[exportName] = nodeRight.name;
								return new UglifyJS.AST_EmptyStatement({start:node.start,end:node.end});
							}else{
								exportMap[exportName] = createExportVar(scope,exportName);
							}
						}else{
							exportMap[exportName] = createExportVar(scope,exportName);
						}
					}
				}
				var exportVarName = exportMap[exportName] ;
				return replaceExport(exportVarName,topScope,node);
			}
		}
	}
	function createExportVar(scope,exportName){
		var n = exportName;
		while(scope.find_variable(n)){
			n+='_'
		}
		return n
	}
	function replaceExport(exportVarName,topScope,node){
		var symbolDef = topLevel.variables.get(exportVarName);
		if(symbolDef == null){
			var symbolVar = new UglifyJS.AST_SymbolVar({ 
				//thedef:symbolDef,
				name: exportVarName,
				start : node.right.start,
				end   : node.right.end
			})
			symbolDef = new UglifyJS.SymbolDef(topLevel, 
				topLevel.variables.size(), 
				symbolVar);
			symbolVar.thedef = symbolDef;
			symbolDef.undeclared = false;
			symbolDef.global = true;
			symbolDef.name = exportVarName;
			topLevel.variables.set(exportVarName, symbolDef);
			topLevel.directives.push(exportVarName)
			if(!topScope){
				//add globals def
				var topBody = topLevel.body;
				var i = topBody.length;
				var astVar = new UglifyJS.AST_Var({
					definitions: [new UglifyJS.AST_VarDef({
							name  : symbolVar,
							value : null, // the original AST_String
							//start : nodeRight.start,
							//end   : nodeRight.end
						})]
				});
				while(i--){
					if(topBody[i] instanceof UglifyJS.AST_Var){
						//console.log(Object.keys(topBody[i]))
						break;
					}
				}
				console.log('append globals:'+exportVarName)
				topBody.splice(i,0,astVar)
			}
			//console.log('%%%%',exportName,topLevel.variables.get(exportName) == null)
		}else{
			symbolVar = symbolDef.orig[0];
			//console.dir(symbolDef.orig[0].TYPE);
		}
		if(topScope){
			//console.warn('@@ append variable',exportName)
			//
			return new UglifyJS.AST_Var({
				definitions: [new UglifyJS.AST_VarDef({
						name  : symbolVar,
						value : node.right, 
						start : node.right.start,
						end   : node.right.end
					})]
			});;
		}else {
			node.left = new UglifyJS.AST_SymbolRef({ 
				name: exportVarName,
				thedef:symbolDef,
				start : node.left.start,
				end   : node.left.end
			})
		}
	}
}

function getAssignLeft(node){
	if(node instanceof UglifyJS.AST_Assign && node.operator == '='){
		return node.left;
	}
}
function addOnce(list,o){
	var i = list.indexOf(o);
	if(i<0){
		list.push(o);
	}
}