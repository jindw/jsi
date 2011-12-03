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
 * $Id: base64.js,v 0.2 2008/06/18 08:01:50 dankogai Exp dankogai $
 */
// This source code is in the public domain.

/**
 * 创建一个Zip档案
 */
function Zip(comment) {
    this.members = [];
    /**
     * 档案文件注释
     */
    this.comment = comment ||''
}

Zip.prototype = {
	/**
	 * 档案文件MimeType
	 */
    mimeType: 'application/zip',
    stringEncoder:stringToUTF8ByteArray,
    setCompressMethod:function(compressImpl,compressMethod){
    	this.compressImpl = compressImpl;
    	this.compressMethod = compressMethod || (compressImpl?8:0);
    },
    /**
     * 添加纯文本内容（utf8）
     */
    addText: function(path,text) {
        var stream = this.stringEncoder(text);
        var compressStream = this.compressImpl?this.compressImpl(stream):stream
        var method = this.compressMethod || 0;
        var member = new StreamMember(this.stringEncoder(path),stream,compressStream,method);
        return appendMember(this,member);
    },
    /**
     * 添加空目录
     */
    addDirectory: function(path) {
        if (!/\/$/.test(path)) {
            path += '/';
        }
        return appendMember(this,new DirectoryMember(this.stringEncoder(path)));
    },
    /**
     * 添加网络文件
     */
    addFile: function(path,url) {
        if (!path) {
            var paths = url.split(/\/+/);
            path = paths.pop();
        }
        var stream = loadBinData(path)
        var member = new StreamMember(this.stringEncoder(path),stream)
        return appendMember(this,member);
    },
    /**
     * 添加网络文件（异步）
     */
    addFileAsync: function(path,url,callback) {
        if (!path) {
            var paths = url.split(/\/+/);
            path = paths.pop();
        }
        var pathData = this.stringEncoder(path);
        var zip = this;
        loadBinData(path,function(stream,success){
        	var compressStream = zip.compressImpl?zip.compressImpl(stream):stream
	        var method = zip.compressMethod || 0;
	        var member = new StreamMember(pathData,stream,compressStream,method);
        	appendMember(zip,member)
        	callback && callback.call(zip,member, success);
        })
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
        var commentData = this.stringEncoder(this.comment);
        appendByteArray(bin,commentData.length, 2);
        //.ZIP file comment       (variable size)
        arrayPush.apply(bin, commentData);
        return bin;
    },
    /**
     * 生成data协议url
     */
    toDataURL: function() {
        return ['data:', this.mimeType, ';base64,', byteArrayToBase64(this.toByteArray())].join('');
    },
    constructor: Zip
};
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
var iso8859ReplaceMap = {};
var iso8859data = "20ac 81 201a 192 201e 2026 2020 2021 2c6 2030 160 2039 152 8d 17d 8f 90 2018 2019 201c 201d 2022 2013 2014 2dc 2122 161 203a 153 9d 17e 178".split(" ")
var i=32;
while(i--){
    iso8859ReplaceMap[String.fromCharCode(parseInt(iso8859data[i],16))] = String.fromCharCode(0x80+i)
}
function iso8859Replacer(c){
    return iso8859ReplaceMap[c];
}
function toResponseStream(xhr){
	var contentType = xhr.getResponseHeader("Content-Type");
	if(!/\bcharset\b/.test(contentType) || /charset=ISO-8859-1/i.test(contentType)){
	    var result = String(xhr.responseText).replace(/[\u0100-\uffff]/g,iso8859Replacer);
	}else{
		var result = String(xhr.responseStream);
	}
	return stringToByteArray(result)
}
function loadBinData(url,callback){
    var xhr = new XMLHttpRequest();
    var isAsyn = !!callback;
    var overrideMimeType = xhr.overrideMimeType;

    xhr.open('GET', url, isAsyn);
    if(overrideMimeType){
    	xhr.overrideMimeType("text/html;charset=ISO-8859-1");
    }
    if(isAsyn){
    	xhr.onreadystatechange = function() {
            if (xhr.readyState == 4) {
                xhr.onreadystatechange = Function.prototype;
            	var success = xhr.status == 200 || xhr.status == 0;
                callback.call(xhr,toResponseStream(xhr),success);
            }
        };
        xhr.send(null);

    }else{
    	xhr.send(null);
    	return toResponseStream(xhr);
    }
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

function DirectoryMember() {
    StreamMember.apply(this,arguments)
    this.externalFileAttributes = (040755 << 16) | 0x10; // 0x10 bit for Windows Explorer's Directory
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
