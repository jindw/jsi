/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General 
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) 
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 * details.
 *
 * @author jindw
 * @version $Id: boot.js,v 1.3 2008/02/25 05:21:27 jindw Exp $
 */


/**
 * 方便调试的支持
 */
if(":debug"){
    var $JSI= {
    };
    /**
     * 调试友好支持
     */
    (function(){
        //compute scriptBase
        var rootMatcher = /(^\w+:((\/\/\/\w\:)|(\/\/[^\/]*))?)/;
        //var rootMatcher = /^\w+:(?:(?:\/\/\/\w\:)|(?:\/\/[^\/]*))?/;
        var homeFormater = /(^\w+:\/\/[^\/#\?]*$)/;
        //var homeFormater = /^\w+:\/\/[^\/#\?]*$/;
        var urlTrimer = /[#\?].*$/;
        var dirTrimer = /[^\/\\]*([#\?].*)?$/;
        var forwardTrimer = /[^\/]+\/\.\.\//;
        var base = document.location.href.
                replace(homeFormater,"$1/").
                replace(dirTrimer,"");
        var baseTags = document.getElementsByTagName("base");
        var scripts = document.getElementsByTagName("script");
        /*
         * 计算绝对地址
         * @public
         * @param <string>url 原url
         * @return <string> 绝对URL
         * @static
         */
        function computeURL(url){
            var purl = url.replace(urlTrimer,'').replace(/\\/g,'/');
            var surl = url.substr(purl.length);
            //prompt(rootMatcher.test(purl),[purl , surl])
            if(rootMatcher.test(purl)){
                return purl + surl;
            }else if(purl.charAt(0) == '/'){
                return rootMatcher.exec(base)[0]+purl + surl;
            }
            purl = base + purl;
            while(purl.length >(purl = purl.replace(forwardTrimer,'')).length){
                //alert(purl)
            }
            return purl + surl;
        }
        //处理HTML BASE 标记
        if(baseTags){
            for(var i=baseTags.length-1;i>=0;i--){
                var href = baseTags[i].href;
                if(href){
                    base = computeURL(href.replace(homeFormater,"$1/").replace(dirTrimer,""));
                    break;
                }
            }
        }

        //IE7 XHR 强制ActiveX支持
        if(this.ActiveXObject && this.XMLHttpRequest && location.protocol=="file:"){
            this.XMLHttpRequest = null;
        }
        
        var script = scripts[scripts.length-1];
        //mozilla bug
        while(script.nextSibling && script.nextSibling.nodeName.toUpperCase() == 'SCRIPT'){
            script = script.nextSibling;
        }
        var scriptBase = (script.getAttribute('src')||"/scripts/boot.js").replace(/[^\/\\]+$/,'');
        $JSI.scriptBase = computeURL(scriptBase);


        var impls = ['boot-core.js','boot-log.js'];
        for(var j=0;j<impls.length;j++){
            //document.write('<script onreadystatechange="if(this.readyState == \'loaded\'){if(/__preload__.js$/.test(this.src)){this.src=\'//:\'}}" type="text/javascript" src="'+src+impls[j]+'"></script>');
            document.write('<script src="'+scriptBase+impls[j]+'"></script>');
        }
    })();
}else{
    /*
     * JSI2.5 起，为了避免scriptBase 探测。节省代码量，我们使用写死的方法。
     * 如果您的网页上使用了如base之类的标签，那么，为了摸平浏览器的差异，你可能需要再这里明确指定scriptBase的准确路径。
     */
    /**
     * JSI对象
     * @public
     */
    var $JSI= {
        /**
         * 包加载的根路径，根据启动脚本文件名自动探测。
         * @public
         * @id $JSI.scriptBase
         * @typeof string
         * @static
         */
         //scriptBase : "http://localhost:8080/script2/"
    };
}

