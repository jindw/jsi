/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: file.js,v 1.4 2008/02/19 14:46:39 jindw Exp $
 */

//for ie
/**
 * 基于JScriptFSO,参照java.io.File 实现的文件系统访问接口
 */
function File(){
  var file = arguments[1];
  if(file == null){
    file = (""+arguments[0]).replace(/\//g,'\\');
  }else{
    var dir = arguments[0]+"";
    dir = dir.replace(/\//g,'\\');
    if(dir.charAt(dir.length-1) != '\\'){
      dir += '\\';
    }
    file = dir +  ((file+"").replace(/\//g,'\\'));
  }
  this.path = file.replace(/\\+/g,'\\');
}
var fso;
function getFSO(){
  if(fso){
    return fso;
  }
  return fso = new ActiveXObject("Scripting.FileSystemObject");
}

function getFile(file){
  try{
    return getFSO().GetFile(file.path);
  }catch(e){
    return getFolder(file);
  }
}

function getFolder(file){
  return getFSO().GetFolder(file.path);
}
File.listRoots = function(){
   var e = new Enumerator(getFSO().Drives);
   var result = [];
   for (; !e.atEnd(); e.moveNext())
   {
      result.push(new File(e.item().DriveLetter+":\\"));
   }
   return result;
}
File.prototype._checkAttribute = function(bit){
  return (getFile(this).attributes & bit) >0
}
File.prototype.canRead = function(){
  throw new Error("UnsupportedOperation:canRead");
}
File.prototype.canWrite = function(){
  throw new Error("UnsupportedOperation:canWrite");
}
File.prototype.compareTo = function(destFile){
  throw new Error("UnsupportedOperation:compareTo");
}
File.prototype.createNewFile = function(){
  throw new Error("UnsupportedOperation:createNewFile");
}
File.prototype['delete'] = function(){
  throw new Error("UnsupportedOperation:delete");
}
File.prototype.deleteOnExit = function(){
  throw new Error("UnsupportedOperation:deleteOnExit");
}
File.prototype.equals = function(file){
  return this.path = file.path;
}
File.prototype.exists = function(){
  return getFSO().FileExists(this.path);
}
File.prototype.getAbsoluteFile = function(){
  throw new Error("UnsupportedOperation:getAbsoluteFile");
}
File.prototype.getAbsolutePath = function(){
  throw new Error("UnsupportedOperation:getAbsolutePath");
}
File.prototype.getCanonicalFile = function(){
  throw new Error("UnsupportedOperation:getCanonicalFile");
}
File.prototype.getCanonicalPath = function(){
  throw new Error("UnsupportedOperation:getCanonicalPath");
}
File.prototype.getName = function(){
  return this.path.substring(this.path.lastIndexOf('\\',this.path.length-2)+1);
}
File.prototype.getContent = function(){
  var f = getFile(this);
  var ts = f.OpenAsTextStream(1,-2);
  return ts.ReadAll();
}
File.prototype.getParent = function(){
  return this.path.substring(0,this.path.lastIndexOf('\\',this.path.length-2)+1);
}
File.prototype.getParentFile = function(){
  return new File(this.getParent());
}
File.prototype.getPath = function(){
  return this.path;
}
File.prototype.isAbsolute = function(){
  return this.path.search(/$([A-Za-z]{1}:\\)||(\\\\)/)>=0;
}

File.prototype.isDirectory = function(){
  return this._checkAttribute(16);
}
File.prototype.isFile = function(){
  return !this.isDirectory();
}
File.prototype.isHidden = function(){
  return this._checkAttribute(2);
}
File.prototype.lastModified = function(){
  return Date.parse(getFile(this).DateLastModified);
}
File.prototype.length = function(){
  return getFile(this).Size;
}
File.prototype.list = function(filter){
  var f = getFolder(file);
  var result = [];
  var it = new Enumerator(f.SubFolders);
  for (;!it.atEnd(); it.moveNext()){
    var item = it.item()+"";
    item = item.substr(item.lastIndexOf('\\'));
    if(!filter || filter(item)){
      result.push(item);
    }
  }
  var it = new Enumerator(f.files);
  for (;!it.atEnd(); it.moveNext()){
    var item = it.item()+"";
    item = item.substr(item.lastIndexOf('\\'));
    if(!filter || filter(item)){
      result.push(item);
    }
  }
  return result;
}
File.prototype.listFiles = function(filter){
  var f = getFolder(file);
  var result = [];
  var it = new Enumerator(f.SubFolders);
  for (;!it.atEnd(); it.moveNext()){
    var item = it.item();
    item = new File(item);
    if(!filter || filter(item)){
      result.push(item);
    }
  }
  var it = new Enumerator(f.files);
  for (;!it.atEnd(); it.moveNext()){
    var item = it.item();
    item = new File(item);
    if(!filter || filter(item)){
      result.push(item);
    }
  }
  return result;
}
File.prototype.mkdir = function(){
  var fso = getFSO();
  if(fso.FolderExists(this.path)){
    return false;
  }else{
    fso.CreateFolder(this.path);
    return true;
  }
}
File.prototype.mkdirs = function(){
  var fso = getFSO();
  if(fso.FolderExists(this.path)){
    return false;
  }else{
    var parent = this.getParentFile();
    //alert(parent.path+"："+fso.FolderExists(parent.path));
    if(fso.FolderExists(parent.path)){
      fso.CreateFolder(this.path);
      return true;
    }else{
      parent.mkdirs();
      fso.CreateFolder(this.path);
      return true;
    }
  }
}
File.prototype.renameTo = function(){
  throw new Error("UnsupportedOperation:renameTo");
}
File.prototype.setLastModified = function(){
  throw new Error("UnsupportedOperation:setLastModified");
}
File.prototype.setReadOnly = function(){
  throw new Error("UnsupportedOperation:setReadOnly");
}
File.prototype.toURI = function(){
  throw new Error("UnsupportedOperation:toURI");
}
File.prototype.toURL = function(){
  throw "file:///"+this.path.replace('\\','/');;
}
File.prototype.toString = function(){
  return this.path;
}

