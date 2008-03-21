/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: writer.js,v 1.4 2008/02/19 14:46:39 jindw Exp $
 */

/**
 * 没用的东西,写着玩玩,切莫模仿
 * class for writing to character streams.
 * @public
 * @constructor
 * @author jindw
 */
function Writer(){
  this.buf = [];
}
/**
 * Write a string.
 * @public
 * @param  str  String to be written
 * @return <Writer> return itself
 */
Writer.prototype.print = function(str){
  for(var i=0;i<arguments.length;i++)this.buf.push(arguments[i]);
  return this;
}
/**
 * Write a string.
 * @public
 * @arguments  str...  String to be written
 * @return <Writer> return itself
 * @see #write
 */
Writer.prototype.write = function(){
  return this.print.apply(this,arguments);
}
/**
 * Write a string.
 * @public
 * @param  str  String to be written
 * @return <Writer> return itself
 */
Writer.prototype.println = function(str){
  for(var i=0;i<arguments.length;i++)this.buf.push(arguments[i]);
  this.buf.push('\r\n');
  return this;
}
/**
 * Write a string.
 * @public
 * @param  str  String to be written
 * @return <Writer> return itself
 * @see #write
 */
Writer.prototype.writeln = function(str){
  return this.println.apply(this,arguments);
}
/**
 * create a new String for the content
 * @public
 * @return <Writer> return itself
 */
Writer.prototype.clear = function(){
  this.buf.length = 0;
  return this;
}
/**
 * create a new String for the content
 * @public
 */
Writer.prototype.toString = function(){
  return this.buf.join('');
}