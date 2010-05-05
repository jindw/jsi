/**
 * 测试Hello World
 * @jsiparser
 * 
 * @import org.xidea.jsidoc.util.$log
 * @import org.xidea.jsidoc.util.JSON
 * //喜欢的话，也可一通过文件申明依赖
 * //@import org/xidea/jsidoc/util/log.js
 * //@import org/xidea/jsidoc/util/json.js
 * @export
 */
function sayHello(msg){
  $log(title,JSON.stringify(msg));
}

var title = "Hello\n";
