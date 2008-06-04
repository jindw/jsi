/*
 * 测试调试模式下的自动变量查找
 */

/**
 * 这是一个公开对象，在JSI中存在注册，可以通过$import函数导入
 * @public
 */
var Jindw = {
    /**
     * 这是一个公开方法，隶属于Guest对象。
     * @public
     */
    sayHello:function(){
        alert(buildHelloMessage())
    }
}
var message = "大家好，我是 [%1]";
/**
 * 这是一个内部函数（文件内私有）
 * @internal
 */
function buildHelloMessage(){
    var name = 'Jindw'
    return message.replace('%1',name);
}