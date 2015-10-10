

exports.test = <div c:for="${item : list}" style="border:solid 1px ;display:block">
					<h3>${item.name}</h3>
					<p>${item.description}</p>
					<hr/>
				</div>

/*

exports.test1 = liteXML('./example.tpl')

exports.test2 = liteXML('./example.tpl#header')

exports.test3 = <p>${a+b}</p>;

exports.test4 = function(a,b){
	<p>${a+b}</p>
};
*/

