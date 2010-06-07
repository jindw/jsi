/*
 * @author 金大为
 * @from JSON.org(http://www.json.org/)
 * @version $Id: event-util.js,v 1.5 2008/02/25 01:55:59 jindw Exp $
 */

/**
 * @public
 * @param data
 * @return JSON
 */
 
var JSON = {
    /**
     * 解析JSON文本
     * @public 解析
     * @owner JSON
     */
    parse :parse, 
    /**
     * 以JSON格式，系列化javascript对象
     * @public
     * @owner JSON
     * @param <Object> value
     * @return <String> json 表达式
     */
    stringify : serialize,
    format : function(obj){
    	return serialize(parse(obj),'','  ',32)
    }
}
function parse(data){
    return window.eval("("+data+")")
};
/**
 * IE 好像容易出问题，可能是线程不安全导致。
 * @internal
 */
var stringRegexp = /["\\\x00-\x1f\x7f-\x9f]/g;
/**
 * 转义替换字符
 * @internal
 */
var charMap = {
    '\b': '\\b',
    '\t': '\\t',
    '\n': '\\n',
    '\f': '\\f',
    '\r': '\\r',
    '"' : '\\"',
    '\\': '\\\\'
};
/**
 * 转义替换函数
 * @internal
 */
function charReplacer(item) {
    var c = charMap[item];
    if (c) {
        return c;
    }
    c = item.charCodeAt().toString(16);
    return '\\u00' + (c.length>1?c:'0'+c);
}
/**
 * JSON 串行化实现
 * @internal
 */
function serialize(value,prefix,indent,maxLength) {
    switch (typeof value) {
        case 'string':
            stringRegexp.lastIndex = 0;
            return '"' + (stringRegexp.test(value) ?
                            value.replace(stringRegexp,charReplacer) :
                            value)
                       + '"';
        case 'function':
            return value.toString();
        case 'object':
            if (!value) {
                return 'null';
            }
            if(indent){
            	prefix+=indent;
            }
            var buf = [];
            if (value instanceof Array) {
                var i = value.length;
                while (i--) {
                	var v = serialize(value[i],prefix,indent,maxLength) || 'null';
                    buf[i] = v;
                }
                v = buf.join(',');
                if(indent && v.length>maxLength){
                	var k = prefix+indent
                	v = '\n'+ k+ buf.join(",\n"+k)+"\n"+prefix;
                }
                return '[' + v + ']';
            }
            for (var k in value) {
                var v = serialize(value[k],prefix,indent,maxLength);
                if (v) {
                    buf.push(serialize(k) + ':' + v);
                }
            }
            v = buf.join(',');
            if(indent && v.length>maxLength){
            	var k = prefix+indent
            	v = '\n'+ k+ buf.join(",\n"+k)+"\n"+prefix;
            }
            return '{' + v + '}';
        case 'number':
            if(!isFinite(value)){
                value = 'null';
            }
        default:
            return String(value);
    }
}