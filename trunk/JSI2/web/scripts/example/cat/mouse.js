/**
 * 老鼠
 * @constructor
 */
function Mouse(){

}
/**
 * 逃跑函数
 * @param catcher 捕捉者
 * @return 是否逃脱
 */
Mouse.prototype.runAway = function(catcher){
  if(catcher instanceof Cat){
    if(Math.random()<0.2){//如果遇见猫，只有20%的机会逃脱
      return true;
    }
  }else{
    if(Math.random()<0.8){//80%的机会逃脱
      return true;
    }
  }
  return false;
}