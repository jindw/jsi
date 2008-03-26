/**
 * 猫类
 * @constructor
 */
function Cat(){
}
/**
 * 猫类也是动物
 */
Cat.prototype = new Animal();
/**
 * 觅食,眼前是老鼠就抓
 * @param <Animal>animal 碰见的动物
 */
Cat.prototype.lookForFood = function(animal){
  if(animal instanceof Mouse){//使用到老鼠的引用，转载后依赖
    //do catch
    if(animal.runAway(this)){
      return "看见老鼠，没抓着"
    }else{
      return "看见老鼠，抓住了"
    }
  }else{
    return "不是老鼠"
  }
}
document.write("test");
