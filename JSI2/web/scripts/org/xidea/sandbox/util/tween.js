/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: tween.js,v 1.3 2008/03/03 12:40:50 jindw Exp $
 */


/**
 * 渐变运动处理类
 * @param second 运动总秒数
 * @param transform 渐变函数
 * @param interval 间隔时间 
 */
function Tween(second,transform,interval){
    if(second){
        this.time = parseInt(second*1000);
    }
    if(transform){
        this.transform = transform;
    }
    if(interval){
        this.interval = interval;
    }
}

Tween.prototype = {
    interval:40,
    transform:function(x){return 1-Math.pow(1-x,3)},
    time:2000,
    start : function(onStep,onComplete){
        var interval = this.interval;
        var time = this.time;
        var transform = this.transform;
        var end = transform(1);
        var t = 0;
        function callback(){
            t+=interval;
            var x = t/time;
            if(x>=1){
                onStep(1);
                onComplete();
                clearInterval(task);
            }else{
                onStep(transform(x)/end);
            }
        }
        var task = setInterval(callback,interval);
        return task;
    },
    moveBy : function(el,offsetX,offsetY,onFinish){
        var x = el.offsetLeft;//+margin
        var y = el.offsetTop;//+margin
        var style = el.style;
//        var bak = [style.position,style.left,style.top];
        style.position = 'absolute';
        function onStep(rate){
            style.left = parseInt(x+rate*offsetX)+'px';
            style.top = parseInt(y+rate*offsetY)+'px';
        }
        function onComplete(){
//            style.position = bak[0];
//            style.left = bak[1];
//            style.top = bak[2];
            el =style=null;
            onFinish && onFinish()
        }
        return this.start(onStep,onComplete);
    },
    opacity : function(el,begin,end,onFinish){
        el = document.getElementById(el);
        var inc = end - begin;
        var first = true;
        function onStep(rate){
            rate = begin + inc*rate;
            if(el.style.filter != null){
                //如果是浮点型是否有异常
                el.style.filter = (opacity == 1) ? '' : "alpha(opacity=" + opacity * 100 + ")";
            }else{
                el.style.opacity = rate;
            }
            if(first){
                first = false;
                el.style.display = 'block';
            }
        }
        function onComplete(){
            if(end == 0){
                el.style.display = 'none';
            }
            el =null;
            onFinish && onFinish()
        }
        return this.start(onStep,onComplete);
    }
}

