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
    	try{
    		var value =$JSI.loadText&&$JSI.loadText(url)
    		if(value){
    			return value;
    		}
    	}catch(e){};
        return impl.loadText(String(url).replace(/\w+\:\/*/,''))+'';
    }
});
