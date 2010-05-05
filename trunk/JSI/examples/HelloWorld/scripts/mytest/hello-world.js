/**
 * 测试Hello World
 * @jsiparser
 * @import say-hello.js
 * @export
 */
function HelloWorld(name,msg){
    this.name = name;
    this.msg = msg
}
HelloWorld.prototype.hello = function(){
    sayHello({
        name:this.name,
        msg :this.msg
    });
}

