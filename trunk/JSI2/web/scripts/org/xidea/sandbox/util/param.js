/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: param.js,v 1.2 2008/02/19 13:30:13 jindw Exp $
 */
function URI(){
    var exp = /([^=]*)=([^&])/g;
    var match;
    var param = {};
    var params = {};
    while(match = exp.exec(extpath)){
        var name = decodeURIComponent(match[1]);
        var value = decodeURIComponent(match[2]);
        if(param[name] instanceof String){
            //param[name] = value; sevlet api 中规定的是取第一个值
            params[name].push(value);
        }else{
            param[name] = value;
            params[name] = [value];
        }
    }
    this.param = param;
    this.params = params;
}