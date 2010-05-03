/*
 * JSI 初始化的附加脚本
 * 1.补充window，print 变量
 * 2.补充$JSI.scriptBase值
 * 3.返回loadTextByURL
 */
var window =this;
this.print = this.print || function(arg){
    java.lang.System.out.print(String(arg))
};
(function (bootArgs){
    $JSI.scriptBase= "classpath:///";
    var impl = this;
    bootArgs[0] = function(code){
        var path = this.scriptBase + this.name;
        var evaler =  impl.eval("(function(){eval(arguments[0])})", path,null)
        return evaler.call(this,code);
    }
    return function loadText(url){
    	var value =$JSI.loadText&&$JSI.loadText(url)
    	if(value){
    		return value;
    	}
    	var service = url.match(/^.*[?&]service=([\w\-_]+).*$/);
    	service = service && service[1];
    	var path = url.match(/^.*[?&]path=([\w\-_]+).*$/);
    	path = path && path[1] || url.replace(/^\w+:(\/)+/,'');
    	if(service == 'list'){
    		return impl.list(path);
    	}
        return impl.loadText(path)+'';
    }
});
