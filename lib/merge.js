function merge(rbs,resource,sourcePath,text,prefix){
	var path = resource.path;
	return text.replace(/^\/\/\s*#include\b\s*[('"]+([\/\w\.\-]+?)[)"']+/mg,function(a,inc){
		var source = resource.load(inc).toString('utf-8');
		if(sourcePath == path && inc == '/static/require.js'){
			//build in source;
			return mergeSource(rbs,path.substr(prefix.length),prefix,source)
		}
		return source;
	})
}
function mergeSource(rbs,path,prefix,bootSource){
	var sourceMap = {};
	var buf = [bootSource.replace(/\}(\);?\s*)$/,''),''];
	while(true){
		for(var n in sourceMap){
			if(!sourceMap[n]){
				path = n;
				break;
			}
		}
		if(path){
			var file = prefix+path.replace(/.js$/,'__define__.js');
			try{
				var data = rbs.getContentAsBinary(file).toString('utf-8');
			}catch(e){
				console.error('merge file not found:',file)
				data = "$JSI.define('"+path+"',[],function(){/* file not found: "+file+"*/})"
			}
			var deps = JSON.parse(data.substring(data.indexOf('['),data.indexOf(']')+1));
			var impl = data.substring(data.indexOf('function('),data.lastIndexOf('}')+1);
			if(path in sourceMap){
				sourceMap[path] = true;
				buf.push('"',path.replace(/\.js$/,'')+'":[',impl,']',',')
			}
//			console.log('deps',path,deps,data)
			for(var i = 0;i<deps.length;i++){
				var path2 = normalizeModule(deps[i],path)+'.js';
				if(!(path2 in sourceMap)){
					sourceMap[path2] = null;
				}
			}
			var path = null;
		}else{
			break;
		}
	}
	buf.pop();
	buf.push('});')
	return buf.join('');
}

function normalizeModule(url,base){
    var url = url.replace(/\\/g,'/');
    if(url.charAt(0) == '.'){
    	url = base.replace(/[^\/]+$/,'')+url
    	while(url != (url =url.replace( /[^\/]+\/\.\.\/|(\/)?\.\//,'$1')));
    }
    return url;
}


exports.merge = merge;