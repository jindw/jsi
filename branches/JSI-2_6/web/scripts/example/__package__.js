/**
 * 这里是演示一个简单的托管脚本，直接使用*模式，
 * 自动注册指定脚本的全部变量（以后最好还是严谨一点，手动注册吧）
 */
this.addScript('hello-world.js','*');//测试调试模式下的自动变量查找
//this.addScript('hello-world.js',['sayHello','message']);