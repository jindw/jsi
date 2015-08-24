

exports.test  = function(list){
<div c:for="${item : list}" style="border:solid 1px ;display:block">
	<h3>${item.name}</h3>
	<p>${item.description}</p>
	<hr/>
</div>
};

/*
exports.test2 = <p>${a+b}</p>;


exports.test2 = function(){
	<p>${a+b}</p>
};
*/

