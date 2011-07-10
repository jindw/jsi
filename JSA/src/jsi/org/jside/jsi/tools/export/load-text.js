(
{
    setup:function(root){
    	$JSI.loadText = function(path){
    		//java.lang.System.out.println("@@@@@"+path);
    		path = path.replace(/^(\w+)\:\/+/,'');
    		var exp = /(\w+[\\\/]\.)?\.[\\\/]/;
    		do{
    			var path2 = path;
    			path = path2.replace(exp,'')
    		}while(path2!=path)
    		var scriptName = path.replace(/^.*\//,'');
    		var packageName = path.slice(0,-1-scriptName.length).replace(/[\\\/]/g,'.');
    		return root.loadText(packageName,scriptName);
    	};
	 	$import('org.xidea.jsidoc.export:Exporter');
	 	document = window;
		this.exporter = new Exporter();
		this.exporter.addFeature('mixTemplate');
		this.filter = this.exporter.buildSourceFilter();
    },
    loadText:function(path){
    	$import('org.xidea.jsi:$log');
    	//$log.error(path,this.filter,this.exporter.getSource(path,this.filter))
    	var source = this.exporter.getSource(path,this.filter);
    	//$log.info(source);
		return source;
	}
}
)

