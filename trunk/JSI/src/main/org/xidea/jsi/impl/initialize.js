/*
 * JSI 初始化的附加脚本
 */
var window =this;
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
    return function loadTextByURL(url){
        url = url.replace(/^\w+:(\/)+(?:\?.*=)?/,'');
        return Packages.org.xidea.jsi.impl.RhinoSupport.loadText(url)+'';
    }
});
