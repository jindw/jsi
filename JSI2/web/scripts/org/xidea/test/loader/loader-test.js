/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: loader-test.js,v 1.3 2008/02/19 13:39:03 jindw Exp $
 */

var scriptBase = this.scriptBase;
var LoaderTest= new Object();
$log.level = 0;
LoaderTest.test = function(packageList){
  this.packageList=packageList;this.packageIndex=0;this.objectIndex=0;
  this.pkg = this.nextPackage();
  this.doLoad();
}
LoaderTest.nextPackage = function(){
  if(this.packageIndex<this.packageList.length){
    var pkg = $import(this.packageList[this.packageIndex++]+':');
    this.objectNames = [];
    for(var n in pkg.objectScriptMap){
      this.objectNames.push(n);
    }
    return pkg;
  }else{
    return null;
  }
}
LoaderTest.nextObject = function(){
  if(this.pkg!=null){
    if(this.objectIndex<this.objectNames.length){
      return this.pkg.name+"@"+this.objectNames[this.objectIndex++];
    }else{
      this.pkg = this.nextPackage();
      //alert(this.pkg.objectNames);
    
      this.objectIndex = 0;
      if(this.pkg){
        return this.nextObject();
      }else{
        return null;
      }
    }
  }else{
    return null;
  }
}

LoaderTest.doLoad = function(){
  var clazz = this.nextObject();
  if(clazz){
    var src = scriptBase+"loader.html?"+clazz;
    //alert(loader.$JSI.globalContext+"/"+src);
    //var loader = document.getElementById("loader");
    //alert("$JSI.globalContext.open:"+src)
    window.open(src,"loader");
    //loader.contentWindow.location=src;
    //alert(clazz+"/"+src+"/"+loader.src);
  }
}
LoaderTest.loadNull = function(clazz){
  $log.warn("object is null:"+clazz);
  this.doLoad();
}
LoaderTest.loadSuccess = function(clazz){
//alert(clazz);
  $log.info("object load success:"+clazz);
  this.doLoad();
}
LoaderTest.loadFailure = function(clazz,e){
  var msg = "object load error:"+clazz+";e="+e;
  for(var n in e){
    msg+=",e."+n+"="+e[n];
  }
  $log.error(msg);
  this.doLoad();
}
LoaderTest.defaultPackageList = [
	              //"org.xidea.jsdoc",
"example",
"example.codedecorator",
"org.xidea.decorator",
"org.xidea.jsdoc",
"org.xidea.syntax",
"org.xidea.test.loader",
"org.xidea.util",
"org.xidea.xml"
];
