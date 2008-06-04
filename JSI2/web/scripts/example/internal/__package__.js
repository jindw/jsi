/*
 * 这个包中，我们重点演示JSI隔离冲突的一种方式--内部元素(脚本内私有对象)。
 * 在JSI中，我们隔离冲突主要有两种方式：
 * 一种就是脚本隔离－－即该实例中的内部元素；
 * 另外一种是包之间的隔离－－相对更容易理解，但是不好示例，留给读者自信体会吧...。
 */
this.addScript('jindw.js','*');//['Jindw','message',]//测试调试模式下的自动变量查找
this.addScript('guest.js',['Guest']);


/*
$import("example.internal.Jindw");
$import("example.internal.Guest");
Jindw.sayHello();
Guest.sayHello();
*/