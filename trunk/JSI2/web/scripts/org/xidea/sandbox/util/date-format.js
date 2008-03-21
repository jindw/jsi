/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: date-format.js,v 1.2 2008/02/19 13:30:13 jindw Exp $
 */

function DateFormatSymbols(patternOrReplaceMap){
  if(typeof patternOrReplace == 'string'){
    this.patternChars = patternOrReplaceMap
  }else{
    for(var n in patternOrReplaceMap){
      this[n] = patternOrReplaceMap[n];
    }
  }
}
DateFormatSymbols.prototype = {
  patternChars:'GyMdkHmsSEDFwWahKzZ',
  amPmStrings:['AM', 'PM'],
  eras:['BC', 'AD'],
  months:['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December', ''],
  shortMonths:['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec', ''],
  shortWeekdays:['', 'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
  weekdays:['', 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
  zoneStrings:[]
}
/*
DateFormatSymbols.prototype = {
  patternChars:'GanjkHmsSEDFwWxhKzZ',
  amPmStrings:['上午', '下午'],
  eras:['公元前', '公元'],
  months:['一月','二月','三月','四月','五月','六月','七月','八月','九月','十月','十一月','十二月'],
  shortMonths:['一','二','三','四','五','六','七','八','九','十','十一','十二'],
  shortWeekdays:['', '日','一','二','三','四','五','六'],
  weekdays:['', '星期日','星期一','星期二','星期三','星期四','星期五','星期六'],
  zoneStrings:[]
}*/

function DateFormat(pattern,dateFormatSymbols){
  this.pattern = pattern;
  this.symbols = dateFormatSymbols||DateFormatSymbols.prototype;
}
DateFormat.prototype.format = function(date){
  var p = this.pattern;
   
}
var formatFunctions = [];
var parseFunctions = [];
