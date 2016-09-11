

exports.test = function(list){
	return <div c:for="${item : list}" style="border:solid 1px ;display:block">
					<h3>${item.name}</h3>
					<p>${item.description}</p>
					<hr/>
				</div>;
}


/**
 * optimize to simple string
 */
exports.test1 = function(params){
	return <div a='${item}' c:for="${itemx : [{name:'name',description:'desc...'}]}" style="border:solid 1px ;display:block">
					<h3>${item.name}</h3>
					<p>${item.description}</p>
					<hr/>
				</div>;
}
/**
 * optimize to simple join
 */
exports.test1 = function(arg1,arg2){
	return <div  style="border:solid 1px ;display:block">
					${arg1}/${arg2}
				</div>;
}
/**
 * load from file
 * optimize to simple string
 */
exports.test2 = require('./example-tpl.xhtml')


var items = [{name:'name',description:'desc...'}];
/**
 * inline template
 */
exports.test2 = <div a='${item}' c:for="${item : items}" style="border:solid 1px ;display:block">
					<h3>${item.name}</h3>
					<p>${item.description}</p>
					<hr/>
				</div>
/*
exports.test3 = <p>${a+b}</p>;

exports.test4 = function(a,b){
	<p>${a+b}</p>
};
*/

