/*
 * @author 金大为
 * @from JSON.org(http://www.json.org/)
 * @version $Id: event-util.js,v 1.5 2008/02/25 01:55:59 jindw Exp $
 */

function __compile_EL__(el,forKey){
    if(!/'"\//.test(el)){
        el = el.replace(/\bfor\s*\./g,forKey+'.')
    }else{
	    //好复杂的一段，这里还不够完善
	    el = el.replace(/(\bfor\s*\.)||'[\\.]*?'|"[\\.]*?"|[\s\S]*?/g,function(code,_for){
	        if(_for){
	            return forKey+'.';
	        }else{
	            return code;
	        }
	    });
    }
    return el;
}
/**
 * JS to Java
 * @internal
 */
function __JS2JAVA__(value) {
    switch (typeof value) {
        case 'object':
            if (value instanceof Array) {
                var v = new java.util.ArrayList();
                for (var i = 0;i<value.length;i++) {
                    v.add(__JS2JAVA__(value[i]));
                }
                return v;
            }
            var v = new java.util.HashMap();
            for (var k in value) {
                v.put(k,__JS2JAVA__(value[k]));
            }
            return v;
        case 'number'://可行，但是，不能与其他解析器混用
           if(parseInt(value) == value){
           	    return new java.lang.Long(value);
           }
        default:
            return value;
    }
}