/**
 * 利用jsidoc中定义的JSON库（org.xidea.jsidoc.util.JSON）实现的一个用于显示对象细节的函数。
 * <pre>
 * $import("example.dependence.*");
 * var person= {}
 * person.name='张三';
 * person.location='北京';
 * person.project='http://www.xidea.org/project/jsi/';
 * showDetail(person)
 * </pre>
 * @param <Object>object 想查看的对象
 */
function showDetail(object){
	var buf = ["对象信息如下：\n\n"];
	buf.push(JSON.encode(object));
	confirm(buf.join("\n"));
}
