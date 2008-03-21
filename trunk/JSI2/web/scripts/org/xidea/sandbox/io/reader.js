/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: reader.js,v 1.4 2008/02/19 14:46:39 jindw Exp $
 */

/**
 * 没用的东西,写着玩玩,切莫模仿
 * class for reading character from a string.
 * @public
 * @constructor
 * @author jindw
 */
function Reader(value){
  this.value = value;
  this.position = 0;
  this.line = 0;
}
/**
 * Reade a char.
 * @public
 * @return next char or null(end of stream)
 */
Reader.prototype.read = function(){
  var c = this.value.charAt(this.position++);
  if( c == '\n'){
    if(this.isNextEnd){
      this.line++;
      this.isNextEnd = false;
    }else if(this.value.charAt(this.position) == '\r'){
      this.isNextEnd = true;
    }else {
      this.line++;
    }
  }else if(c == '\r'){
    if(this.isNextEnd){
      this.line++;
      this.isNextEnd = false;
    }else if(this.value.charAt(this.position) == '\n'){
      this.isNextEnd = true;
    }else {
      this.line++;
    }
  }
  return c == ''?null:c;
}

/**
 * Reade a line.
 * @public
 * @return current line or null(end of stream)
 * @see #read
 */
Reader.prototype.readLine = function(){
  if(this.position>=this.value.length){
    return null;
  }
  if(this.isNextEnd){
    this.position++;
    this.line++;
    this.isNextEnd = false;
    return '';
  }
  for(var p=this.position;p<this.value.length;p++){
    var c = this.value.charAt(p);
    if(c == '\r' || c == '\n'){
      var line = this.value.substring(this.position,p);
      this.position = p+1;
      if(c == '\r'){
        if(this.value[p+1] == '\n'){
          this.position++;
        }
      }else{
        if(this.value[p+1] == '\r'){
          this.position++;
        }
      }
      this.line++;
      return line;
    }
  }
  p = this.position;
  this.position = this.value.length;
  return this.value.substr(p);
}
/**
 * @public
 * @return <Reader> return itself
 */
Reader.prototype.reset = function(){
  this.position = 0;
  this.line = 0;
  return this;
}
/**
 * create a new String for the content
 * @public
 */
Reader.prototype.toString = function(){
  return this.value +"";
}
/* test 
alert("12".charAt(5))
var r = new Reader("1\r\n2\n3\r\n\n4444\r\r\n\r\n\n\r6\n\r7\n");
var result = ""
while((l = r.readLine())!=null){
result +=l+"-"+r.line+"\n";
}
result+="\r\n\r\n";
r.reset();
var line = 0;
while((l = r.read())!=null){
  if(r.line>line){
    line = r.line;
    result +="-"+(line)+"\n";
  }
  result +=l;
}
alert(r.line+"\r\n"+result);*/