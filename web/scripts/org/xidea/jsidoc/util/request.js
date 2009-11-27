/*
 * License 未定
 * @author 金大为
 * @from JSI(http://www.xidea.org/project/jsi/)
 * @version $Id: event-util.js,v 1.5 2008/02/25 01:55:59 jindw Exp $
 */


/**
 * XMLHttpRequest请求包装
 * <p>XMLHttpRequest请求包装类，默认为异步方式。
 * 若希望使用同步方式,在调用send方法时指定第二个参数synchronous = true
 * 支持的事件有 [step,success,failure]</p>
 * eg:
 * <pre>
 * var messageAppendService = new Request(url,"post");
 * //设置成功回调函数(也可以直接在参数终设置)
 * messageAppendService.onComplete = function(){alert('operation success')};
 * //传送 参数表：param1=1,指定请求方式为异步
 * messageAppendService.send({content:"测试留言"});
 * </pre>
 * @constructor
 * @param <string>url 请求地址
 * @param <string|object>options 请求选项[可选]
 * @param <function>onComplete 完成后的回调函数[可选]，回调时，参数为 <boolean> 是否成功
 * @param <function>onStep 单步事件的回调函数,传入当前的xhr.readyState值[可选]
 * @author 金大为
 */
function Request(url,options,onComplete,onStep) {
    this.xhr = new XMLHttpRequest();
    this.onComplete = onComplete;
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
          var success = _this.isSuccess();
          _this.onComplete && _this.onComplete(success);
          if(success){
              _this.onSuccess && _this.onSuccess();
          }else{
              _this.onFailure && _this.onFailure();
          }
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
        this.xhr.send(params);
        return this;
    },
    /**
     * 判断请求是否成功
     * @public
     * @owner Request.prototype
     * @return <boolean || null> 成败或未知(null)
     */
    isSuccess : function() {
      var status = this.getStatus();
      return status ?status >= 200 && status < 300 : null;
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
    getJSON : function() {
      if(this.xhr.readyState == 4){
        return window.eval('('+this.xhr.responseText+')');
      }else{
        $log.debug("response not complete");
      }
    },
    /**
     * 将当前请求返回数据
     * @public
     * @return 文本 或 XMLDocument
     */
    getResult : function(){
        if(/\/xml/.test(this.getHeader("Content-Type"))){//text/xml,application/xml...
            if(this.xhr.readyState == 4){
                return this.xhr.responseXML;
            }
        }else{
            if(this.xhr.readyState >= 3){
                return this.xhr.responseText;
            }
        }
        $log.error("response not complete");
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
    if(options.constructor == String){
        this.method = options;
    }else{
        for(var n in options){
            this[n] = options[n];
        }
    }
}
RequestOptions.prototype =    {
    method:        'post',
    contentType:  'application/x-www-form-urlencoded',
    encoding:     'UTF-8'
}

/**
 * 请求完成的回调事件
 * @public
 * @id Request.prototype.onComplete
 * @param <boolean> success 请求成功（200<=state<300）
 * @typeof function
 */

/**
 * 请求成功的回调事件
 * @public
 * @id Request.prototype.onSuccess
 * @typeof function
 */

/**
 * 请求失败的回调事件
 * @public
 * @id Request.prototype.onFailure
 * @typeof function
 */

/**
 * 每次状态事件的回调函数（xhr.onreadystatechange）
 * @public
 * @id Request.prototype.onStep
 * @typeof function
 */