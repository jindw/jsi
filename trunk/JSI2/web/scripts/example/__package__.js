this.addScript("helloworld.js","sayHello");

/*
 * 重用aculo effect 脚本实例
 */
//添加slidePanel（滑动面板控制）函数
//给effect.js脚本添加对us.aculo.script包中effects.js脚本的装载期依赖
this.addScript("effect.js","slidePanel"
                 ,"us.aculo.script.Effect");


