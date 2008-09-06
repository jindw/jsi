/*
 * 这个包中，我们重点演示JSI隔离冲突的一种方式--内部元素(脚本内私有对象)。
 * 在JSI中，我们隔离冲突主要有两种方式：
 * 一种就是脚本隔离－－即该实例中的内部元素；
 * 另外一种是包之间的隔离－－相对更容易理解，但是不好示例，留给读者自己去体会吧...。
 */
this.addScript('jindw.js','Jindw');//还有两个内部变量未公开：buildMessage，message
this.addScript('guest.js','*');