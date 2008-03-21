/*
 * 猫和老鼠的例子
 */

//添加动物(Animal)类
this.addScript("animal.js","Animal");
//添加猫(Cat)类
this.addScript("cat.js","Cat");
//添加鼠(Mouse)类
this.addScript("mouse.js","Mouse");


//给猫和鼠类添加对动物类的装载前依赖
this.addDependence("*","Animal");

//猫有抓老鼠的天性，要判断眼前的动物是否老鼠
//给猫类添加对鼠类的装载后依赖
this.addDependence("Cat","Mouse",false);


//老鼠看见猫后要逃跑，需要判断眼前的动物是否猫
//给鼠类添加对猫类的装载后依赖
this.addDependence("Mouse","Cat",false);

/*
//养猫
var cat = new Cat();
//有只老鼠
var mouse = new Mouse();
//打印猫抓到老鼠的结果
$log.info(cat.lookForFood(mouse))
*/