/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: tween-rule.js,v 1.2 2008/03/03 12:40:50 jindw Exp $
 */


/*
 * @internal
 * n次转换曲线批量构建函数
 */
function buildPowRule(exponent){
    var rate = 1/Math.pow(0.5,exponent-1);
    return [
        /*
         * easeIn
         */
        function(x){
            return Math.pow(x,exponent);
        },
        /*
         * easeOut
         */
        function(x){
            return 1-Math.pow(1-x,exponent);
        },
        /*
         * easeBoth
         */
        function(x){
            if(x>0.5){
                return 1-rate* Math.pow(1-x,exponent);
            }else{
                return Math.pow(x,exponent)*rate;
            }
        }
    ]
}
/*
 * @internal
 */
var Rule2= buildPowRule(2);
/*
 * @internal
 */
var Rule3= buildPowRule(3);


/**
 * 转换函数集合
 * 标准转换函数定义：
 * 函数x坐标输入为[0-1]。
 * 输入前可以将真实数据线性应射到[0-1]区间内数据。
 * 如[2000-8000]  我们可以 
 * x/8000 -> [0.25-1]
 * x/8000-0.25 -> [0-0.75]
 * (x-2000)/4000 -> [0-1]
 * @public
 */
var TweenRuleMap ={

    /**
     * 匀速运动曲线
     * @owner TweenRuleMap
     */
    none : function(x){return x},
    
    /**
     * 加速曲线
     * @owner TweenRuleMap
     */
    easeIn : Rule2[0],//(x == t/d        [b+c)
    
    /**
     * 减速曲线
     * @owner TweenRuleMap
     */
    easeOut : Rule2[1],
    /**
     * 加速减速曲线
     * @owner TweenRuleMap
     */
    easeBoth : Rule2[2],
    /**
     * 加加速曲线
     * @owner TweenRuleMap
     */
    easeInStrong : Rule3[0],
    /**
     * 减减速曲线
     * @owner TweenRuleMap
     */
    easeOutStrong : Rule3[1],
    /**
     * 加加速减减速曲线
     * @owner TweenRuleMap
     */
    easeBothStrong : Rule3[2],
    /**
     * 正弦衰减曲线（弹动渐入）
     * @owner TweenRuleMap
     */
    elasticIn : function(x,period){
        return Math.pow(1024,x-1)*Math.sin(x*((2*(period||2)+0.5)*Math.PI));
    },

    /**
     * 正弦增强曲线（弹动渐出）
     * @owner TweenRuleMap
     */
    elasticOut : function(x,period){
        return 1-Math.pow(1024,-x)*Math.cos(x*((2*(period||2)+0.5)*Math.PI));
    },

    /**
     * 回退加速（回退渐入）
     * @owner TweenRuleMap
     */
    backIn : function(x,backDistance){
        return x*(x-(backDistance||0.1)*4)
    },

    /**
     * 弹球减振（弹球渐出）
     * @owner TweenRuleMap
     */
    bounceOut : function (x) {
        if (x < (1/2.75)) {
            return x*x;
        } else if (x < (2/2.75)) {
            return (x-=(1.5/2.75))*x + .75/7.5625;
        } else if (x < (2.5/2.75)) {
            return (x-=(2.25/2.75))*x + .9375/7.5625;
        }
        return (x-=(2.625/2.75))*x + .984375/7.5625;
    }

};

/*
 * @internal
 */
function toOut(eIn){
    var max = eIn(1);
    return function(x,param){
        return max-eIn(1-x,param);
    }
}
/*
 * @internal
 */
function toBoth(eIn){
    var max = eIn(1);
    return function(x,param){
        if(x<0.5){
            return eIn(x*2,param)/2;
        }
        return max-eIn(2-x*2,param)/2;
    }
}

/**
 * 正弦衰减-增强曲线（弹动渐入渐出）
 */
TweenRuleMap.elasticBoth = toBoth(TweenRuleMap.elasticIn,TweenRuleMap.elasticOut);
/**
 * 弹球起振（弹球渐入）
 */
TweenRuleMap.bounceIn = toOut(TweenRuleMap.bounceOut);
/**
 * 弹球起振-弹球减振（弹球渐入-弹球渐出）
 */
TweenRuleMap.bounceBoth = toBoth(TweenRuleMap.bounceIn);
/**
 * 减速过界（渐出过界）
 */
TweenRuleMap.backOut = toOut(TweenRuleMap.backIn);
/**
 * 回退加速-减速过界（回退渐入-渐出过界）
 */
TweenRuleMap.backBoth = toBoth(TweenRuleMap.backIn);
