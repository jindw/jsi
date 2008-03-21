/**
 * 测试装载单元内程序运行效率
 */
function test(i,j){
  testInternal(i,j);
}
var testVar1 = 0,testVar2 = 0;
/*
 * 内部函数
 */
function testInternal(i,j){
  if(j-->=0){
    testInternal(i,j);
  }
  for(;i>0;i--){
    testVar1++;testVar2++;
  }
}