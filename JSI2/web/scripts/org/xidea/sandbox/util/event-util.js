/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: event-util.js,v 1.5 2008/02/25 01:55:59 jindw Exp $
 */


/**
 * 事件处理函数集:
 * (add|remove)(事件名称)Listener(element,listener) 系列方法；
 * 特殊的事件DOMReady的处理 addDOMReadyListener(listener,runAnyCase) 方法；
 * 其他诸如：dispatchEvent,createEvent之类的方法，以及通用的addListener，removeListener方法。
 * @public
 */
var EventUtil = {
}
var events = ['click','mousedown','mouseup','mouseover','mousemove','mouseout','load','unload','abort','error','select','change','submit','reset','focus','blur','resize','scroll'].
             concat(['DOMFocusIn','DOMFocusOut','DOMActivate']);
if(document.addEventListener){
    
    /**
     * 给指定对象添加[<b>$1</b>]事件处理函数
     * @public
     * @param <HTMLElement|document|window> el html节点或者window|document对象
     * @param <Function>fn 事件监听函数
     * @id EventUtil.add*Listener
     */
    /**
     * 给指定对象添加指定类型的事件处理函数
     * @public
     * @param <HTMLElement|document|window> el html节点或者window|document对象
     * @param type 事件类型
     * @param <Function>fn 事件监听函数
     */
    EventUtil.addListener = function(el,type,fn,cap){
        el.addEventListener(type,fn,cap);
    };
    /**
     * 删除指定对象的[<b>$1</b>]事件处理函数
     * @public
     * @param <HTMLElement|document|window> el html节点或者window|document对象
     * @param <Function>fn 事件监听函数
     * @id EventUtil.remove*Listener
     */
    /**
     *  删除指定对象的指定类型的事件处理函数
     * @public
     * @param <HTMLElement|document|window> el html节点或者window|document对象
     * @param type 事件类型
     * @param <Function>fn 事件监听函数
     */
    EventUtil.removeListener = function(el,type,fn,cap){
        el.removeEventListener(type,fn,cap);
    };
    EventUtil.dispatchEvent = function(el,event){
        el.dispatchEvent(event);
    };
    EventUtil.createEvent = function(type,canBubble,cancelable){
        var event = document.createEvent(type);
        event.initEvent(type,canBubble,cancelable);
        return event;
    }
}else{
    var functionList = [];
    var listenerSetList = [];
    function ListenerSet(fn){
        var elements = [];
        var listeners = [];
        this.get = function(element,type,create){
            for(var i = elements.length-1;i>=0;i--){
                if(elements[i] == element){
                    var listener = listeners[i];
                    if(listener.typeMap[type]){
                        return listener;
                    }else if(create){
                        listener.typeMap[type] = true;
                        return listener;
                    }else{
                        return fn;
                    }
                }
            }
            if(create){
                function listener(){
                    var event = window.event;
                    //alert(event.srcElement.tagName);
                    IEFormatEvent(event);
                    fn.call(listener.element,event);
                }
                listener.typeMap = {} 
                listener.typeMap[type] = true;
                listener.element = element;
                listener.destroy = listenerDistroy;
                elements.push(element);
                listeners.push(listener);
                //IMPORTANT
                element = null;
                return listener;
            }
        };
        this.destroy = function(){
            while(elements.pop()){
                listeners.pop().destroy();
            }
        };
    }
    function listenerDistroy(){
        for(var n in this.typeMap){
            this.element.detachEvent(n,this);
        }
        this.element=null;
    }
    window.attachEvent('onunload',function(listener){
        while(listener = listenerSetList.pop()){
            listener.destroy();
            //delete 
            functionList.pop();
            delete listener;
        }
    })
    
    function buildListenerForIE(fn,element,type,create){
        for(var i = functionList.length-1;i>=0;i--){
            if(functionList[i] == fn){
                return listenerSetList[i].get(element,type,create);
            }
        }
        if(create){
            functionList.push(fn);
            fn = new ListenerSet(fn)
            listenerSetList.push(fn); 
            return fn.get(element,type,true)
        }
    }
    //function getScrollLength(attribute){
    //    return parseInt(document.body[attribute]||document.documentElement[attribute], 10)
    //}
    function IEFormatEvent(event){
        if(!event.stopPropagation){
            event.charCode = (event.type == "keypress") ? event.keyCode : 0;
            event.eventPhase = 2;
            event.isChar = (event.charCode > 0);
            
        //alert(document.documentElement.scrollTop)
            //event.pageX = event.clientX + getScrollLength('scrollLeft');
            //event.pageY = event.clientY + getScrollLength('scrollTop');
            event.preventDefault = IEEventPreventDefault;
            if (event.type == "mouseout") {
                event.relatedTarget = event.toElement;
            } else if (event.type == "mouseover") {
                event.relatedTarget = event.fromElement;
            }
            event.stopPropagation = IEEventStopPropagation;
            event.target = event.srcElement;
            event.time = (new Date).getTime();
        }
        return event;
    };
    function IEEventStopPropagation() {
        this.cancelBubble = true;
    };
    function IEEventPreventDefault() {
        this.returnValue = false;
    };
    
    EventUtil.addListener = function(el,type,fn,cap){
        type = 'on'+type;
        var listener = buildListenerForIE(fn,el,type,true);
        el.attachEvent(type,listener,cap);
    };
    EventUtil.removeListener = function(el,type,fn,cap){
        type = 'on'+type;
        var listener = buildListenerForIE(fn,el,type,false);
        if(listener){
            el.detachEvent(type,listener,cap);
        }
    };
    EventUtil.dispatchEvent = function(el,event){
        if(event.type){
            el.fireEvent('on'+event.type,event);
        }
    };
    EventUtil.createEvent = function(type,canBubble,cancelable){
        var event = document.createEventObject();
        event.type = type;
        //event.cancelBubble
        //event.initEvent(type,canBubble,cancelable);
        return event;
    }
}

function buildEventFunctions(type){
    var stuf = type.substr(0,1).toUpperCase()+type.substr(1)+'Listener';
    EventUtil['add'+stuf] = function(element,listener,captrue){
        this.addListener(element,type,listener,captrue)
    }
    EventUtil['remove'+stuf] = function(element,listener,captrue){
        this.removeListener(element,type,listener,captrue)
    }
}
for(var i = events.length-1 ;i>=0;i--){
    buildEventFunctions(events[i]);
}
//var domevents = ['DOMFocusIn','DOMFocusOut','DOMActivate'];
if(!document.implementation || !document.implementation.hasFeature('UIEvents', "2.0")){
    EventUtil.addDOMFocusInListener = function(element,listener,captrue){this.addListener(element,'focus',listener,captrue)}
    EventUtil.removeDOMFocusInListener = function(element,listener,captrue){this.removeListener(element,'focus',listener,captrue)}
    EventUtil.addDOMFocusOutListener = function(element,listener,captrue){this.addListener(element,'blur',listener,captrue)}
    EventUtil.removeDOMFocusOutListener = function(element,listener,captrue){this.removeListener(element,'blur',listener,captrue)}
    EventUtil.addDOMActivateListener = function(element,listener,captrue){this.addListener(element,'activate',listener,captrue)}
    EventUtil.removeDOMActivateListener = function(element,listener,captrue){this.removeListener(element,'activate',listener,captrue)}
}
//EventUtil.addDOMReadyListener = $JSI.getAttribute('EventUtil.addDOMReadyListener');

var domReadylisteners = [];
function callDOMReadyListeners(){
    if(domReadylisteners){
        for(var i = 0;i<domReadylisteners.length;i++){
            domReadylisteners[i].apply(document);
        }
    }
    domReadylisteners = null;
}
/**
 * 设置当网页DOM数据装载完毕，但是图片等资源可能未装载完成时触发的的监听器。
 * 这是唯一的一个非W3C标准事件的处理函数。
 * 这个函数也只有一个函数名与W3C事件处理函数风格一致，<b>注意：参数表不同</b>。
 * @param listener
 * @param runAnyCase 确保listener 指定在任何时候都会被调用（若值为真，DOM装载完毕后，设置时即被调用，FF DOMContentLoad事件如果在事件发生之后设置listener是无效的）
 * @public
 */
EventUtil.addDOMReadyListener = function(listener,runAnyCase){
    if(domReadylisteners){
        domReadylisteners.push(listener);
    }else if(runAnyCase){
        listener.apply(document);
    }
};
if(BrowserInfo.isIE()){
    //document.write("<script id=__$JSI_IE_DOMReady defer=true src=//:><\/script>");
    // Use the defer script hack
    var script = document.getElementById("__$JSI_IE_DOMReady");
    // script.removeAttribute('id');
    // script does not exist if jQuery is loaded dynamically
    if(script){
        if(script.readyState == "complete"){
            callDOMReadyListeners()
            domReadylisteners = null;
            script.parentNode.removeChild( script);
        }else{
            script.onreadystatechange = function() {
                if(this.readyState == "complete"){
                    this.parentNode.removeChild( this );
                    callDOMReadyListeners();
                }
            };
        }
    }else{
        callDOMReadyListeners();
    }
    delete script;
}else if(BrowserInfo.isGecko(20020826)
        //this.addListener(document,'DOMContentLoaded',listener);
    || BrowserInfo.isOpera(9)){//Mozilla 1.0.1 支持 DOMContentLoaded
    EventUtil.addListener(document,'DOMContentLoaded',callDOMReadyListeners);
}else {//alert(document.readyState)
    if(document.readyState){
        var timer = setInterval(function(){
            //alert(document.readyState)
            if (/complete|loaded/.test(document.readyState)) {
                window.clearInterval(timer);
                if(callDOMReadyListeners){
                    callDOMReadyListeners(); // call the onload handler
                }
            }
        }, 100);
    }
    EventUtil.addListener(window,"load",function(){
                if(callDOMReadyListeners){
                    callDOMReadyListeners(); // call the onload handler
                }
            });
}
