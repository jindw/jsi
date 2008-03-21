/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: runtime-performance-test.js,v 1.3 2008/02/19 13:39:03 jindw Exp $
 */

/**
 * 测试装载单元内程序运行效率
 */
function test(i,j){
  internal(i,j);
}
var a = 0,b = 0;
/*
 * 内部函数
 */
function internal(i,j){
  if(j-->=0){
    internal(i,j);
  }
  for(;i>0;i--){
    a++;b++;
  }
}