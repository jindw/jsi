/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */


/**
 * bytes(String) to base64
 * @public
 */
var btoa = window.btoa;//tobase64
/**
 * base64 to bytes(String)
 * @public
 */
var atob = window.atob;//base64tostring
var b64chars
    = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';

var b64codes = [];
var b64map = {};
var i = 65;
while (i--){
	b64codes[i] = b64chars.charCodeAt(i);
	b64map[b64chars.charAt(i)] = i;
}
/**
 * 字节数组（小整数数组）转化成Base64字符串
 * @pulic
 */
function byteArrayToBase64(bs) {
    var b64 = [];
    var bi = 0;
    while (bi < bs.length) {
        var b0 = bs[bi++];
        var b1 = bs[bi++];
        var b2 = bs[bi++];
        var data = (b0 << 16) + (b1 << 8) + (b2||0);
        b64.push(
        	b64codes[(data >> 18) & 0x3F ],
        	b64codes[(data >> 12) & 0x3F],
        	b64codes[isNaN(b1) ? 64 : (data >> 6) & 0x3F],
        	b64codes[isNaN(b2) ? 64 : data & 0x3F]) ;
    }
    return String.fromCharCode.apply(String, b64);
}