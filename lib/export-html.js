var fs = require('fs')
var compressJS = require('./js-process').compressJS
var xmldom = require('xmldom')
var DOMParser = xmldom.DOMParser;
var XMLSerializer = xmldom.XMLSerializer;
function exportHTML(root,path,callback){
	var path = path.replace(/[?#].*$/,'');
	var filepath = require('path').join(root,path);
	fs.stat(filepath,function(error,stats){
		if(stats && stats.isFile()){
	    	fs.readFile(filepath, "utf8", function(err, file) {    
	    		var parser = new DOMParser();
	    		var dom = parser.parseFromString(file.toString('utf-8'),'text/html');
	    		optimizeDOM(dom,filepath,function(){
	    			var ser = new XMLSerializer();
	    			//console.log(ser.serializeToString(dom))
	    			callback(ser.serializeToString(dom))
	    		});
	    	})
	    }else{
	    	callback(error|| new Error('not html file:'+filepath))  
	    }
	});
}

var liteContent = fs.readFileSync(require.resolve('../assets/lite-buildin.js')).toString();
function optimizeDOM(root,filepath,onComplete){
	var stack = []
	var asynTask = 0;
	var liteMerged = false;
	//console.log('optimize!',filepath)
	walkDOM(root,stack,function(node){
		if(node.nodeType == 1){//ELEMENT_NODE
			if(/^script$/i.test(node.tagName)){
				//console.log('script!',node+'')
				var fc = node.firstChild;
				var lc = node.lastChild;
				if(fc == lc){
					var src = node.getAttribute('src');
					var scriptRoot = require('path').join(filepath.replace(/[^\\\/]*$/,''),
											src.replace(/[^\\\/]*$/,''));
					if(src && /\bboot\.js$/.test(src)){
						//do export
						var exportSingleFile = require('./exports').exportSingleFile;
						asynTask++;
						var called = false;
						//console.log('export script:',scriptRoot,fc.data)
						exportSingleFile(scriptRoot,'#'+fc.data,1,function(content,externalDeps2err){
							//console.log('complete script:',content,externalDeps2err)
							if(!liteMerged && /\b__x__\(/.test(content)){
								liteMerged = true;
								content = compressJS(liteContent) + '\n'+content;
							}
							fc.data = content;
							node.removeAttribute('src')
							if(called){
							}else if(--asynTask <=0){
								called = true;
								onComplete();
							}
							
						})
					}else{
						fc.data = compressJS(fc.data);
					}
					//console.log(fc.data)
				}else{
					console.log('script has muti child!!'+String(fc),String(lc))
				}
			}
		}else if(node.nodeType == 3){//TEXT_NODE
			if(stack.indexOf('PRE')<0 && stack.indexOf('CODE')<0 && stack.indexOf('SCRIPT')<0 && stack.indexOf('TEXTAREA')<0){
				//trim text
				node.data = node.data.replace(/^(\s)\s*|\s*(\s)$/,'$1$2');
			}
		}else if(node.nodeType == 8){//COMMENT_NODE
			var pre  = node.previousSibling;
			var next = node.nextSibling;
			if(pre && next && pre.nodeType == 3 && next.nodeType == 3){
				pre.parentNode.removeChild(pre);
				next.insertData(0,pre.data);
			}
			node.parentNode.removeChild(node)
		}
	});
}
function walkDOM(node,stack,callback){
	if(node){
		var next = node.nextSibling;
		var skip = callback(node);
		walkDOM(next,stack,callback);
		if(!skip && node.firstChild){
			stack = stack.concat((this.tagName||'').toUpperCase())
			walkDOM(node.firstChild,stack,callback);
		}
		
	}
}
exports.exportHTML = exportHTML;