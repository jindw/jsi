/*
 * 测试调试模式下的自动变量查找
 * 这里所有的文件变量都将公开
 */



/**
 * 这是一个公开对象，在JSI中存在注册，可以通过$import函数导入
 * @public
 */
var Guest = {
    /**
     * 这是一个公开方法，隶属于Guest对象。
     * @public
     */
    sayHello:function(){
        alert(buildMessage("Guest"))
    }
}
/**
 * 构建问候语
 * @public
 * @param <String> name 游客名字
 * @return <String> 问候消息
 */
function buildMessage(name){
    return "大家好，我是 [%1]".replace('%1',name);
}