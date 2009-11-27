/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */
 
/*
 * zip 官方规范见：http://www.pkware.com/documents/casestudies/APPNOTE.TXT
 * 部分代码来自：http://svn.coderepos.org/share/lang/javascript/Zip/base64.js
 * 后续维护：请参看:http://jsiside.googlecode.com
 */
// This source code is in the public domain.

/**
 * 创建一个Zip档案
 * 只支持IE8，FF，WebKit....
 */
function Zip(comment) {
    this.members = [];
    /**
     * 档案文件注释
     */
    this.comment = comment ||''
}
/**
 * JSIDoc 专用的 Zip打包脚本。
 * 完整版本请参看：http://jsiside.googlecode.com
 * @see http://jsiside.googlecode.com
 */
Zip.prototype = {
	/**
	 * 档案文件MimeType
	 */
    mimeType: 'application/zip',

    /**
     * 添加纯文本内容（utf8）
     */
    addText: function(path,text) {
        var stream = stringToUTF8ByteArray(text);
        var compressStream = this.compressImpl?this.compressImpl(stream):stream
        var method = this.compressMethod || 0;
        var member = new StreamMember(stringToUTF8ByteArray(path),stream);
        return appendMember(this,member);
    },
    /**
     * 获取文件内容（byte数组）
     */
    toByteArray: function() {
        var members = this.members;
        var bin = [];
        var offsets = [];

        for (var i = 0; i < members.length; i++) {
            offsets.push(bin.length);
            appendLocalFileHeader(bin, members[i])
            appendLocalFileData(bin,members[i])
            //arrayPush.apply(bin, members[i].getData());
        }

        var centralDirectoryOffset = bin.length;

        for (var i = 0; i < members.length; i++) {
            appendCentralDirectoryFileHeader(bin,members[i],offsets[i]);
        }

        var endOfCentralDirectoryOffset = bin.length;

        //end of central dir signature    4 bytes  (0x06054b50)
        appendByteArray(bin,0x06054b50, 4);
        //number of this disk             2 bytes
        appendByteArray(bin,0, 2);
        //number of the disk with the
        //start of the central directory  2 bytes
        appendByteArray(bin,0, 2);
        //total number of entries in the
        //central directory on this disk  2 bytes
        appendByteArray(bin,members.length, 2);
        //total number of entries in
        //the central directory           2 bytes
        appendByteArray(bin,members.length, 2);
        //size of the central directory   4 bytes
        appendByteArray(bin,endOfCentralDirectoryOffset - centralDirectoryOffset, 4);
        //offset of start of central
        //directory with respect to
        //the starting disk number        4 bytes
        appendByteArray(bin,centralDirectoryOffset, 4);
        //.ZIP file comment length        2 bytes
        var commentData = stringToUTF8ByteArray(this.comment);
        appendByteArray(bin,commentData.length, 2);
        //.ZIP file comment       (variable size)
        arrayPush.apply(bin, commentData);
        return bin;
    },
    /**
     * 生成data协议url
     */
    toDataURL: function() {
    	if(typeof btoa == 'function' && btoa(1) == 'MQ=='){
    		return ['data:', this.mimeType, ';base64,', btoa(String.fromCharCode.apply(0,this.toByteArray()))].join('');
    	}else{
    		return null;
    	}
    },
    constructor: Zip
};
/**
 * 将字符串转化成UTF8字节数组
 * @private
 */
function stringToUTF8ByteArray(str) {
    for(var result=[],i=0,len=str.length;i<len;i++){
	    var n = str.charCodeAt(i);
        if (n < 0x80){
            result.push(n);
        }else if (n < 0x800){
            result.push(
                0xc0 | (n >>>  6),
                0x80 | (n & 0x3f));
        }else{
            result.push(
                0xe0 | ((n >>> 12) & 0x0f),
                0x80 | ((n >>>  6) & 0x3f),
                0x80 |  (n         & 0x3f));
        }
    }
    return result;
}
function appendMember(zip,member) {
    zip.members.push(member);
    return member;
}
var arrayPush = Array.prototype.push
var crc32Map  = [];
    var poly = 0xEDB88320;
    for (var i = 0,u; i < 256; i ++) {
        u = i;
        for (var j = 0; j < 8; j++) {
            if (u & 1){
                u = (u >>> 1) ^ poly;
            }else{
                u = u >>> 1;
            }
        }
        crc32Map[i] = u;
    }
function appendByteArray(data,value, bytes) {
    for (var i = 0; i < bytes; i ++){
        data.push(value >> (i * 8) & 0xFF);
    }
}

function initFieldDateTime(member,dt) {
    member.date = ((dt.getFullYear() - 1980) << 9) |
                ((dt.getMonth() + 1) << 5) |
                (dt.getDate());
    member.time = (dt.getHours() << 5) |
                (dt.getMinutes() << 5) |
                (dt.getSeconds() >> 1);
}
function toCrc32(bin) {
    var result = 0xFFFFFFFF;
    for (var i = 0; i < bin.length; i ++){
        result = (result >>> 8) ^ crc32Map[bin[i] ^ (result & 0xFF)];
    }
    return ~result;
};

function appendLocalFileData(bin,member) {
    arrayPush.apply(bin,member.data[1]);
}
function appendLocalFileHeader(bin,member) {
    //local file header signature     4 bytes  (0x04034b50)
    appendByteArray(bin,0x04034b50, 4);
    //version needed to extract       2 bytes
    appendByteArray(bin,10, 2);
    //general purpose bit flag        2 bytes
    appendByteArray(bin,0, 2);
    //compression method              2 bytes
    appendByteArray(bin,member.method, 2);
    //last mod file time              2 bytes
    appendByteArray(bin,member.time, 2);
    //last mod file date              2 bytes
    appendByteArray(bin,member.date, 2);
    //crc-32                          4 bytes
    appendByteArray(bin,member.crc32, 4);
    //compressed size                 4 bytes
    appendByteArray(bin,member.data[1].length, 4);
    //uncompressed size               4 bytes
    appendByteArray(bin,member.data[0].length, 4);
    //file path length                2 bytes
    appendByteArray(bin,member.path.length, 2);
    //extra field length              2 bytes
    appendByteArray(bin,member.extra.localFile.length, 2);
    //file path (variable size)
    arrayPush.apply(bin, member.path);
    //extra field (variable size)
    arrayPush.apply(bin, member.extra.localFile);
    return bin;
}

function appendCentralDirectoryFileHeader(bin,member,offset) {
    //central file header signature   4 bytes  (0x02014b50)
    appendByteArray(bin,0x02014b50, 4);
    //version made by                 2 bytes
    appendByteArray(bin,0x0317, 2);
    //version needed to extract       2 bytes
    appendByteArray(bin,10, 2);
    //general purpose bit flag        2 bytes
    appendByteArray(bin,0, 2);
    //compression method              2 bytes
    appendByteArray(bin,member.method, 2);
    //last mod file time              2 bytes
    appendByteArray(bin,member.time, 2);
    //last mod file date              2 bytes
    appendByteArray(bin,member.date, 2);
    //crc-32                          4 bytes
    appendByteArray(bin,member.crc32, 4);
    //compressed size                 4 bytes
    appendByteArray(bin,member.data[1].length, 4);
    //uncompressed size               4 bytes
    appendByteArray(bin,member.data[0].length, 4);
    //file path length                2 bytes
    appendByteArray(bin,member.path.length, 2);
    //extra field length              2 bytes
    appendByteArray(bin,member.extra.centralDirectory.length, 2);
    //file comment length             2 bytes
    appendByteArray(bin,0, 2);
    //disk number start               2 bytes
    appendByteArray(bin,0, 2);
    //internal file attributes        2 bytes
    appendByteArray(bin,0, 2);
    //external file attributes        4 bytes
    appendByteArray(bin,member.externalFileAttributes, 4);
    //relative offset of local header 4 bytes
    appendByteArray(bin,offset, 4);
    //file path (variable size)
    arrayPush.apply(bin, member.path);
    //extra field (variable size)
    arrayPush.apply(bin, member.extra.centralDirectory);
    //file comment (variable size)
    //arrayPush.apply(bin, []);
}

function StreamMember(path,data,compressedData,method) {
    this.path = path;
    compressedData = compressedData || data;
    this.crc32 = toCrc32(data);
    this.data = [data,compressedData]
    this.method = method || 0;
    this.externalFileAttributes = 0100644 << 16;
    this.extra = new ExtraField;
    initFieldDateTime(this,new Date);
}


function ExtraField() {
    this.localFile = [];
    this.centralDirectory = [];
}
ExtraField.prototype = {
    append: function(field) {
        arrayPush.apply(
            this.localFile,
            field.localFile
        );
        arrayPush.apply(
            this.centralDirectory,
            field.centralDirectory
        );
    },
    constructor: ExtraField
};
