try{
	var jstoken = require('./js-token');
	var lite = require('lite');
}catch(e){
}
function buildDefine(root,file,id,realId,data,idIndex,mainMap){
	var text = String(data).replace(/\r\n?/g,'\n');
	var deps = [];
	text  = buildDependence(root,file,id,realId,text,deps,idIndex,mainMap)
	
	var map = {};
	var i = deps.length;
	while(i-->0){
		var p = absoluteModule(String(deps[i]),id);
		if(map[p] === true){
			deps.splice(i,1);
		}
		map[p] = true;
	}
	var result = ["$JSI.define('",id,"',["];
	if(deps.length){
		result.push('"',deps.join('","'),'"')
	}
	if( data instanceof Array){
		result.push('],',text,');');
		return result.join('');
	}
	result.push('],function(exports,require');
	var hitfilename = false;
	if(/\b__(?:file|dir)name\b/.test(text)){
		result.push(',module,__filename');
		hitfilename = true;
	}else if(/\bmodule\b/.test(text)){
		result.push(',module');
	}
	result.push('){');
	//if(id!=realId){
	//	console.log("$$$$",id,realId,hitfilename,idIndex)
	//	}
	if(hitfilename && realId && realId !=id){
		var p = realId.indexOf('/');
		if(p >0){
			var pathname = realId.substring(p)+'.js';
		}else{
			var pathname = '/';
		}
		var rel = relative(realId,id).replace(/^\.\//,'');
		var postfix = rel.replace(/^(\.+\/)+/,'');
		var perfix = rel.substr(0,rel.length-postfix.length);
		var c  = perfix.length/3 ;
		var reg = c ? "/[^\\/]+(?:[^\\/]+\\/){"+c+"}$/":"/[^\\/]+$/"
		
		result.push('__filename = __filename.replace('+ reg+',"'+postfix+'");')
	}
	if(/\b__dirname\b/.test(text)){
		result.push('var __dirname= __filename.replace(/[^/]+$/,"");');
	}
	result.push(text,'\n});');
	//console.log('##complete',id)
	return result.join('');
}
function absoluteModule(url,parentModule){
	//console.log('absModule:',url,'|',parentModule)
	if(url.charAt(0) == '.'){
		url = url.replace(/\.js$/i,'');
		url = parentModule.replace(/([^\/]*)?$/,'')+url
		while(url != (url =url.replace( /[^\/]+\/\.\.\/|(\/)?\.\//,'$1')));
	}
	//console.log(url)
	return url;
}
function buildDependence(root,file,id,realId,text,deps,idIndex,mainMap){
	var module = id.replace(/\/.*/,'')
	var redirect = id.replace(/[^\/]+$/,'') != realId.replace(/[^\/]+$/,'');
	
	//console.log("###",id,realId)
	var tokens = jstoken.partitionJavaScript(text);
	var result = [];
	//console.log("###",id,realId)
	for(var i =0;i<tokens.length;i++){
		var item = tokens[i];
		switch(item.charAt()){
		case '\'':
		case '\"':
			//console.log(item);
			var prev = tokens[i-1];
			var next = tokens[i+1];
			if(prev && /^\s*[\)\+]/.test(next)){
				//var tpl1 = new XML("<div></div>");
				//function tpl2(a,b){return new XML("<div style='display:${a}'>${b}</div>");}
				var match = prev.match(/\b(?:(require)|\b(?:new\s+XML|liteXML))\s*\(\s*$/)
				if(match){
					if(match[1] == 'require'){
						if(item!= (item=item.replace(/^(['"])([\w\.\/-]+)\1/,'$2'))){
							
							var absItem = absoluteModule(item,realId);
							//
							var depModule = absItem.replace(/\/.*/ );
							var hits = (idIndex?[id].concat(idIndex) : [id])
									.filter(function(id){return id == depModule || id.charAt(depModule.length)=='/' && !id.indexOf(depModule)}).length
							//console.log("%%%",tokens[i],'|',item,'|',absItem,'|',module,'|',realId,'>',hits)
							if(hits){
								//相对路径替换成数字
								//console.log(id,realId,item)
								
								//console.log(id,realId,item,relative(item,id))
								if(idIndex){
									var index = idIndex.indexOf(absItem);
									if(index>=0){
										tokens[i] = index;
										deps.push(index);
									}else{
										index = idIndex.push(absItem)-1;
										tokens[i] = index;
										deps.push(index);
										//deps['$'+index]+absItem
									}
								}else if(redirect){
									tokens[i] = '"'+absItem+'"';
									//torelative
									//console.log(id,realId,item,relative(item,id))
									
									deps.push(absItem);//relative(item,id));
								}else{
									deps.push(absItem);
								}
							}else if(absItem.indexOf('/')>0){
								console.error("do not use internal file name of the other modules!!"+item);
								deps.push(absItem);
								if(redirect)tokens[i] = '"'+absItem+'"';
							}else{
								//require('name')
								if(redirect)tokens[i] = '"'+absItem+'"';
								deps.push(absItem);
							}
						}
						//console.log(item,deps)
					}else if(lite){//xml
						var j = i;
						while(/^\s*[+'"]/.test(tokens[++j]) && j<tokens.length);
						if(/^\s*\)/.test(tokens[j])){
							item = tokens.splice(i,j-i).join('');
							var match = prev.match(/(?:(\([\w\s,]*\))\{\s*(?:return\s*)?)?(new\s+XML|liteXML)\s*\(\s*$/);
							//console.log("###",match)
							prev = prev.slice(0,prev.length-match[0].length);
							if(match[1]){//has params
								var args = match[1].replace(/[\s()]/g,'').split(',');
								//console.log(item)
								//console.log(match)
								var fn = parseTemplate(root,file,eval(item),args);
								tokens[i-1] = prev+match[1]+"{"+String(fn).replace(/^.*?\{|}\s*$/g,'')+"";
								tokens[i] = tokens[i].replace(/^\s*\)/,'')
								//console.log(tokens[i-1])
							}else{
								var fn = parseTemplate(root,file,eval(item))
								tokens[i-1] = prev+String(fn).replace(/^\s+|\s+$/g,'');
								tokens[i] = tokens[i].replace(/^\s*\)/,'')
							}
						}
					}
				}
			}
		}
	}
	return tokens.join('');
}
function relative(item,base){
	var sp1 = base.split('/');
	var sp2 = item.split('/');
	var leaf = sp2.pop();
	sp1.pop();
	for(var i=0;i<sp1.length;i++){
		if(sp1[i] == sp2[i]){
			sp1.shift();
			sp2.shift();
			i--;
		}else{
			break;
		}
	}
	return ((sp1.join('/').replace(/[^\/]+/g,'..') || '.')+'/'+sp2.join('/').replace(/.+/,'$&/')).replace(/^\.\/\./,'')+leaf
}
function parseTemplate(root,file,xml,args){
	//console.log("parseTemplate:",root,file,xml)
	if(typeof xml == 'string'){
		var m = xml.match(/^([\w\-\/\.]+)(#.*)?$/)
		if(m){
			var attr = m[2];
			var buf =["<c:include path='",m[1],"' "];
			if(attr && attr.length>1){
				buf.push('selector="',attr.substr(1).replace(/["]/g,'&#34;'),'"/>')
			}else{
				buf.push('/>')
			}
			xml = buf.join('')
		}
		//console.log(xml)
		var parser = new (require('xmldom').DOMParser)();
		parser.options.locator.systemId = file;
		xml = parser.parseFromString(xml,'text/html');
		xml.root = root;
	}
	return lite.parseLite(xml,args);
}


exports.buildDefine = buildDefine;