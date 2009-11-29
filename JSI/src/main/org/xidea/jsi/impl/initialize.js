/*
 * JSI 初始化的附加脚本
 * 1.补充window，print 变量
 * 2.补充$JSI.scriptBase值
 * 3.返回loadTextByURL
 */
var window =this;
var JSI = this.$JSI;
this.print = this.print || function(arg){
    java.lang.System.out.print(String(arg))
};
(function (bootArgs){
    $JSI.scriptBase= "classpath:///";
    bootArgs[0] = function(code){
        var path = this.scriptBase + this.name;
        var evaler =  Packages.org.xidea.jsi.impl.RhinoSupport.createEvaler(
            this,
            path
        )
        return evaler.call(this,code);
    }
    return function loadText(url){
    	var value =JSI.loadText&&JSI.loadText(url)
    	if(value){
    		return value;
    	}
        url = url.replace(/^\w+:(\/)+(?:\?.*=)?/,'');
        return Packages.org.xidea.jsi.impl.RhinoSupport.loadText(url)+'';
    }
});
