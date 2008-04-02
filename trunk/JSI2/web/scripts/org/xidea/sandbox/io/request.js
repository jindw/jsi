/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: request.js,v 1.5 2008/03/15 14:05:10 jindw Exp $
 */

/**
 * <p>XMLHttpRequest请求包装类，默认为异步方式。
 * 支持的事件有 [onStep,onSuccess,onFailure]
 * 以添加自定义行为</p>
 * eg:
 * <code><pre>
 * new Request(url,{username:xxx,password:yyy},
 *                      function(){
 *                          //若成功，提示success
 *                          alert('onSuccess')
 *                      },
 *                      function(){
 *                          //若失败，提示fail
 *                          alert('onFailure')
 *                      },
 *                      function(){
 *                           //每一步事件都会触发该函数
 *                           alert('onStep')
 *                      }
 *            )
 *   
 *   //发送请求, 传送参数：param1=value1同事  指定请求方式为异步(true)
 *   .send({param1:'value1'},true);</pre>
 * </code>
 * @constructor
 * @param <string>url 请求地址
 * @param <object|string>options 请求选项,当是字符串时,指定的是请求方法(GET|POST|DELETE|PUT|  HEAD|OPTIONS)
 * @param <Function>onSuccess 请求选项
 * @param <Function>onFailure 请求选项
 * @param <Function>onStep 单步事件,每次onreadystatechange都会触发单步处理函数;
 *                readyState == 4时,也将触发,且发生载onSuccess或者onFailure之前
 */
function Request(url,options,onFinish,onStep) {
    this.xhr = new XMLHttpRequest();
    this.onFinish = onFinish;
    //this.onSuccess = onSuccess;
    //this.onFailure = onFailure;
    this.onStep = onStep;
    this.options = options = new RequestOptions(url,options);
    this.headers = {
                      "Accept":"'text/javascript, text/html, application/xml, text/xml, */*'",
                      "Content-Type":options.contentType
                   };
    var _this = this;
    this.onreadystatechange = function(){
        var state = _this.xhr.readyState;
        _this.onStep && _this.onStep(state);
        if(state == 4){
            //  break;
            //case 4: // (完成) 数据接收完毕,此时可以通过通过responseBody和responseText获取完整的回应数据
            //判断请求是否成功
            var success = _this.getStatus();
            success = success ?success >= 200 && success < 300 : success;
            if(success){
                _this.onSuccess && _this.onSuccess();
            }else{
                _this.onFailure && _this.onFailure();
            }
            _this.onFinish && _this.onFinish(success);
            _this.free = true;
            _this.xhr.onreadystatechange = Function.prototype;
        }
        //case 0: //(未初始化)  对象已建立，但是尚未初始化（尚未调用open方法）
        //case 1: // (初始化)  对象已建立，尚未调用send方法
        //case 2: // (发送数据) send方法已调用，但是当前的状态及http头未知
        //case 3: // (数据传送中)  已接收部分数据，因为响应及http头不全，这时通过responseBody和responseText获取部分数据会出现错误，
    };
    this.free = true;
};


Request.prototype = {
    /**
     * 发送请求
     * @public
     * @owner Request.prototype
     * @param params
     * @sync 同步请求，默认为false，即默认为异步请求
     * @return void 因为无法判断异步请求何时完成，所有。不要在send请求发送后在做设置操作。
     */
    send :  function(params,sync){
        this.free = false;
        
        var headers = this.headers;
        var options = this.options;
        //params = buildQueryString(params);
        sync = sync || options.sync;
        if (/post/i.test(options.method)) {
            //headers['Content-type'] = this.options.contentType;
            /* Force "Connection: close" for Mozilla browsers to work around
             * a bug where XMLHttpReqeuest sends an incorrect Content-length
             * header. See Mozilla Bugzilla #246651.
             */
            if (this.xhr.overrideMimeType){
                headers['Connection'] = 'close';
            }
        }
        this.xhr.open(options.method, options.url,!sync);
        this.xhr.onreadystatechange = this.onreadystatechange;
        for(var n in headers){
            //$log.debug(n,headers[n]);
            this.xhr.setRequestHeader(n,headers[n]);
        }
        this.xhr.send(params || '');
        return this;
    },

    getStatus:function(){
        var xhr = this.xhr;
        //xml 呢？
        return xhr.readyState  == 4 &&((xhr.responseText || xhr.responseXML) && xhr.status);
    },
    /**
     * 设置请求http头。（在每次send调用之前有效）
     * @public
     * @owner Request.prototype
     * @param key 
     * @param value
     * @return <Request> request 本身，以便继续操作
     */
    putHeader : function(key,value){
        this.headers[key] = value;
        return this;
    },
    /**
     * 当前请求响应头
     * @public
     * @owner Request.prototype
     * @return <String>
     */
    getHeader : function(name) {
        if(this.xhr.readyState >= 3){
            return this.xhr.getResponseHeader(name);
        }else{
            $log.debug("response not complete");
        }
    },
    /**
     * 将当前请求返回文本当脚本程序执行
     * @public
     * @owner Request.prototype
     * @return 执行结果
     */
    evalResult : function() {
        if(this.xhr.readyState == 4){
            return window.eval(this.xhr.responseText);
        }else{
            $log.debug("response not complete");
        }
    },
    /**
     * 将当前请求返回XMLDocument
     * @public
     * @owner Request.prototype
     * @return XMLDocument
     */
    getXML : function() {
        if(this.xhr.readyState == 4){
            return this.xhr.responseXML;
        }else{
            $log.debug("response not complete");
        }
    },
    /**
     * 将当前请求返回XMLDocument 或者文本数据
     * @public
     * @owner Request.prototype
     * @return XMLDocument
     */
    getResult : function() {
        if(this.xhr.readyState == 4){
            var xhr = this.xhr;
            return /\bxml\b/.test(xhr.getResponseHeader("Content-Type"))?xhr.responseXML:xhr.responseText;
        }else{
            $log.debug("response not complete");
        }
    },
    
    /**
     * 将当前请求返回XMLDocument文本
     * @public
     * @owner Request.prototype
     * @return String
     */
    getText : function() {
        if(this.xhr.readyState >= 3){
            return this.xhr.responseText;
        }else{
            $log.debug("response not complete");
        }
    }

};

function RequestOptions(url,options){
    this.url = url;
    if(options){
        if(options.constructor == String){
            this.method = options;
        }else{
            for(var n in options){
                this[n] = options[n];
            }
        }
    }
}
RequestOptions.prototype =    {
    method:       'get',
    contentType:  'application/x-www-form-urlencoded',
    encoding:     'UTF-8'
}

